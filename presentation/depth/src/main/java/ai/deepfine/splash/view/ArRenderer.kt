package ai.deepfine.splash.view

import ai.deepfine.splash.util.helper.DisplayRotationHelper
import ai.deepfine.splash.util.renderer.BackgroundRenderer
import ai.deepfine.splash.util.renderer.DepthTextureHandler
import ai.deepfine.utility.utils.L
import android.opengl.GLSurfaceView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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

    backgroundRenderer.draw(frame)
    depthTexture.update(frame)
    backgroundRenderer.drawDepth(frame)
  }
}