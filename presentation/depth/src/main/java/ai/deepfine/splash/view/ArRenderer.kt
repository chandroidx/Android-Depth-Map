package ai.deepfine.splash.view

import ai.deepfine.splash.util.DepthFrame
import ai.deepfine.splash.util.helper.DisplayRotationHelper
import ai.deepfine.splash.util.renderer.BackgroundRenderer
import ai.deepfine.splash.util.renderer.DepthTextureHandler
import ai.deepfine.utility.utils.L
import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.GLSurfaceView
import android.util.Size
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.TrackingState
import java.io.IOException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-11-10
 * @version 1.0.0
 */
class ArRenderer(private val activity: DepthActivity, glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer, DefaultLifecycleObserver {
  init {
    with(glSurfaceView) {
      preserveEGLContextOnPause = true
      setEGLContextClientVersion(2)
      setEGLConfigChooser(8, 8, 8, 8, 16, 0)
      setRenderer(this@ArRenderer)
      renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
  }

  //================================================================================================
  // Helper
  //================================================================================================
  private val displayRotationHelper = DisplayRotationHelper(activity)

  //================================================================================================
  // Renderer
  //================================================================================================
  private val backgroundRenderer: BackgroundRenderer by lazy {
    BackgroundRenderer()
  }

  private val depthTexture: DepthTextureHandler by lazy {
    DepthTextureHandler()
  }

  //================================================================================================
  // Properties
  //================================================================================================
  private val session
    get() = activity.arCoreSessionHelper.session

  private var hasSetTextureNames = false

  var saveBitmap: ((DepthFrame) -> Unit)? = null

  //================================================================================================
  // Lifecycle
  //================================================================================================
  override fun onResume(owner: LifecycleOwner) {
    displayRotationHelper.onResume()
    hasSetTextureNames = false
  }

  override fun onPause(owner: LifecycleOwner) {
    displayRotationHelper.onPause()
  }

  override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
    try {
      depthTexture.createOnGlThread()
      backgroundRenderer.createOnGlThread(activity)
      backgroundRenderer.createDepthShaders(activity, depthTexture.depthTexture)

    } catch (e: IOException) {
      e.printStackTrace()
      L.e("Failed to read a required asset file")
    }
  }

  override fun onSurfaceChanged(renderer: GL10?, width: Int, height: Int) {
    displayRotationHelper.onSurfaceChanged(width, height)
    backgroundRenderer.surfaceSize = Size(width, height)
  }

  override fun onDrawFrame(p0: GL10?) {
    val session = session ?: return

    if (!hasSetTextureNames) {
      session.setCameraTextureNames(intArrayOf(backgroundRenderer.cameraTextureId))
      hasSetTextureNames = true
    }

    displayRotationHelper.updateSessionIfNeeded(session)

    val frame = try {
      session.update()
    } catch (e: Exception) {
      e.printStackTrace()
      L.e("Camera not available during onDrawFrame")
      return
    }

    val camera = frame.camera

    var cameraBitmap: Bitmap? = null
    var depthBitmap: Bitmap? = null

    backgroundRenderer.draw(frame) { bitmap ->
      cameraBitmap = bitmap
    }

    if (camera.trackingState == TrackingState.TRACKING) {
      depthTexture.update(frame)
      backgroundRenderer.drawDepth(frame) { bitmap ->
        depthBitmap = bitmap
      }
    }

    compareAndSaveBitmaps(cameraBitmap, depthBitmap, frame.androidCameraTimestamp)
  }

  private fun compareAndSaveBitmaps(cameraBitmap: Bitmap?, depthBitmap: Bitmap?, timeStamp: Long) {
    if (cameraBitmap == null || depthBitmap == null) {
      cameraBitmap?.recycle()
      depthBitmap?.recycle()
      return
    }

    saveBitmap?.invoke(DepthFrame(cameraBitmap.resize(480, 640), depthBitmap.resize(480, 640), timeStamp))
  }

  private fun Bitmap.resizeTo(maxSize: Int): Bitmap {
    val newWidth: Int
    val newHeight: Int
    val rate: Float

    if (width > height) {
      if (maxSize < width) {
        rate = maxSize / width.toFloat()
        newHeight = (height * rate).toInt()
        newWidth = maxSize
      } else {
        return this
      }
    } else {
      if (maxSize < height) {
        rate = maxSize / height.toFloat()
        newWidth = (width * rate).toInt()
        newHeight = maxSize
      } else {
        return this
      }
    }

    return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
  }

  fun Bitmap.resize(newWidth: Int, newHeight: Int): Bitmap {
    val scaledWidth = newWidth.toFloat() / width
    val scaledHeight = newHeight.toFloat() / height

    val matrix = Matrix()
    matrix.postScale(scaledWidth, scaledHeight)

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
  }
}