package ai.deepfine.splash.util.renderer

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Size
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.properties.Delegates

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-11-07
 * @version 1.0.0
 */
class BackgroundRenderer {
  companion object {
    private val TAG = BackgroundRenderer::class.java.simpleName
    private const val CAMERA_VERTEX_SHADER_NAME = "shaders/screenquad.vert"
    private const val CAMERA_FRAGMENT_SHADER_NAME = "shaders/screenquad.frag"

    private const val DEPTH_VERTEX_SHADER_NAME = "shaders/background_show_depth_map.vert"
    private const val DEPTH_FRAGMENT_SHADER_NAME = "shaders/background_show_depth_map.frag"

    private const val COORDS_PER_VERTEX = 2
    private const val TEXCOORDS_PER_VERTEX = 2
    private const val FLOAT_SIZE = 4

    private val QUAD_COORDS = floatArrayOf(-1F, -1F, 1F, -1F, -1F, 1F, 1F, 1F)
  }

  private lateinit var quadCoords: FloatBuffer
  private lateinit var quadTexCoords: FloatBuffer

  private var cameraProgram by Delegates.notNull<Int>()
  private var cameraPositionAttrib by Delegates.notNull<Int>()
  private var cameraTexCoordAttrib by Delegates.notNull<Int>()
  private var cameraTextureUniform by Delegates.notNull<Int>()
  var cameraTextureId = -1
    private set

  var suppressTimestampZeroRendering = true

  private var depthProgram by Delegates.notNull<Int>()
  private var depthTextureParam by Delegates.notNull<Int>()
  private var depthTextureId = -1
  private var depthQuadPositionParam by Delegates.notNull<Int>()
  private var depthQuadTexCoordParam by Delegates.notNull<Int>()

  var surfaceSize: Size? = null

  fun createDepthShaders(context: Context, depthTextureId: Int) {
    val vertexShader = ShaderUtil.loadGLShader(
      context, GLES20.GL_VERTEX_SHADER, DEPTH_VERTEX_SHADER_NAME
    )

    val fragmentShader = ShaderUtil.loadGLShader(
      context, GLES20.GL_FRAGMENT_SHADER, DEPTH_FRAGMENT_SHADER_NAME
    )

    depthProgram = GLES20.glCreateProgram()
    GLES20.glAttachShader(depthProgram, vertexShader)
    GLES20.glAttachShader(depthProgram, fragmentShader)
    GLES20.glLinkProgram(depthProgram)
    GLES20.glUseProgram(depthProgram)
    ShaderUtil.checkGLError("Program creation")

    depthTextureParam = GLES20.glGetUniformLocation(depthProgram, "u_Depth")
    ShaderUtil.checkGLError("Program parameters")

    depthQuadPositionParam = GLES20.glGetAttribLocation(depthProgram, "a_Position")
    depthQuadTexCoordParam = GLES20.glGetAttribLocation(depthProgram, "a_TexCoord")

    this.depthTextureId = depthTextureId
  }

  fun drawDepth(frame: Frame, onBitmapRendered: (Bitmap?) -> Unit = {}) {
    if (frame.hasDisplayGeometryChanged()) {
      frame.transformCoordinates2d(
        Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
        quadCoords,
        Coordinates2d.TEXTURE_NORMALIZED,
        quadTexCoords
      )
    }

    if (frame.timestamp == 0.toLong() || depthTextureId == -1) return

    quadTexCoords.position(0)

    GLES20.glDisable(GLES20.GL_DEPTH_TEST)
    GLES20.glDepthMask(false)

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthTextureId)
    GLES20.glUseProgram(depthProgram)
    GLES20.glUniform1i(depthTextureParam, 0)

    GLES20.glVertexAttribPointer(depthQuadPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadCoords)
    GLES20.glVertexAttribPointer(depthQuadTexCoordParam, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadTexCoords)

    GLES20.glEnableVertexAttribArray(depthQuadPositionParam)
    GLES20.glEnableVertexAttribArray(depthQuadTexCoordParam)
    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    GLES20.glDisableVertexAttribArray(depthQuadPositionParam)
    GLES20.glDisableVertexAttribArray(depthQuadTexCoordParam)

    GLES20.glDepthMask(true)

    onBitmapRendered(savePixels())

    GLES20.glEnable(GLES20.GL_DEPTH_TEST)
  }

  private fun savePixels(): Bitmap? {
    if (surfaceSize == null) return null

    val width = surfaceSize!!.width
    val height = surfaceSize!!.height

    val b = IntArray(width * height)
    val bt = IntArray(width * height)

    val intBuffer = IntBuffer.wrap(b)
    intBuffer.position(0)
    GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer)

    var i = 0
    var k = 0
    while (i < height) {
      for (j in 0 until width) {
        val pix = b[i * width + j]
        val pb = (pix shr 16) and 0xff
        val pr = (pix shl 16) and 0x00ff0000
        val pix1 = (pix and 0xff00ff00.toInt()) or pr or pb
        bt[(height - k - 1) * width + j] = pix1
      }
      i++
      k++
    }

    return Bitmap.createBitmap(bt, width, height, Bitmap.Config.ARGB_8888)
  }

  fun createOnGlThread(context: Context) {
    val textures = IntArray(1)
    GLES20.glGenTextures(1, textures, 0)
    cameraTextureId = textures.first()
    val textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
    GLES20.glBindTexture(textureTarget, cameraTextureId)
    GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
    GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

    val numVertices = 4
    if (numVertices != QUAD_COORDS.size / COORDS_PER_VERTEX) throw RuntimeException("Unexpected number of vertices in BackgroundRenderer")

    val bbCoords = ByteBuffer.allocateDirect(QUAD_COORDS.size * FLOAT_SIZE)
    bbCoords.order(ByteOrder.nativeOrder())
    quadCoords = bbCoords.asFloatBuffer()
    quadCoords.put(QUAD_COORDS)
    quadCoords.position(0)

    val bbTexCoordsTransformed = ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE)
    bbTexCoordsTransformed.order(ByteOrder.nativeOrder())
    quadTexCoords = bbTexCoordsTransformed.asFloatBuffer()

    val vertexShader = ShaderUtil.loadGLShader(context, GLES20.GL_VERTEX_SHADER, CAMERA_VERTEX_SHADER_NAME)
    val fragmentShader = ShaderUtil.loadGLShader(context, GLES20.GL_FRAGMENT_SHADER, CAMERA_FRAGMENT_SHADER_NAME)

    cameraProgram = GLES20.glCreateProgram()
    GLES20.glAttachShader(cameraProgram, vertexShader)
    GLES20.glAttachShader(cameraProgram, fragmentShader)
    GLES20.glLinkProgram(cameraProgram)
    GLES20.glUseProgram(cameraProgram)
    cameraPositionAttrib = GLES20.glGetAttribLocation(cameraProgram, "a_Position")
    cameraTexCoordAttrib = GLES20.glGetAttribLocation(cameraProgram, "a_TexCoord")
    ShaderUtil.checkGLError("Program creation")

    cameraTextureUniform = GLES20.glGetUniformLocation(cameraProgram, "sTexture")
    ShaderUtil.checkGLError("Program parameters")
  }

  fun draw(frame: Frame, onBitmapRendered: (Bitmap?) -> Unit = {}) {
    if (frame.hasDisplayGeometryChanged()) {
      frame.transformCoordinates2d(
        Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
        quadCoords,
        Coordinates2d.TEXTURE_NORMALIZED,
        quadTexCoords
      )
    }

    if (frame.timestamp == 0.toLong() && suppressTimestampZeroRendering) return

    quadTexCoords.position(0)

    GLES20.glDisable(GLES20.GL_DEPTH_TEST)
    GLES20.glDepthMask(false)

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureId)
    GLES20.glUseProgram(cameraProgram)
    GLES20.glUniform1i(cameraTextureUniform, 0)

    // Set the vertex positions and texture coordinates.

    // Set the vertex positions and texture coordinates.
    GLES20.glVertexAttribPointer(
      cameraPositionAttrib, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadCoords
    )
    GLES20.glVertexAttribPointer(
      cameraTexCoordAttrib, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadTexCoords
    )
    GLES20.glEnableVertexAttribArray(cameraPositionAttrib)
    GLES20.glEnableVertexAttribArray(cameraTexCoordAttrib)

    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

    GLES20.glDisableVertexAttribArray(cameraPositionAttrib)
    GLES20.glDisableVertexAttribArray(cameraTexCoordAttrib)

    // Restore the depth state for further drawing.

    // Restore the depth state for further drawing.
    GLES20.glDepthMask(true)
    GLES20.glEnable(GLES20.GL_DEPTH_TEST)

    onBitmapRendered(savePixels())

    ShaderUtil.checkGLError("BackgroundRendererDraw")
  }
}