package ai.deepfine.splash.view

import ai.deepfine.presentation.base.BaseActivity
import ai.deepfine.presentation.extensions.repeatOnStarted
import ai.deepfine.splash.R
import ai.deepfine.splash.databinding.ActivityDepthBinding
import ai.deepfine.splash.util.ARCoreSessionLifecycleHelper
import ai.deepfine.splash.util.DepthEvent
import ai.deepfine.splash.viewmodel.DepthViewModel
import androidx.activity.viewModels
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * @Description SplashActivity
 * @author yc.park (DEEP.FINE)
 * @since 2021-12-13
 * @version 1.0.0
 */
@AndroidEntryPoint
class DepthActivity : BaseActivity<ActivityDepthBinding, DepthViewModel>(R.layout.activity_depth), DepthEvent.Observer {
  override val viewModel: DepthViewModel by viewModels()

  val arCoreSessionHelper: ARCoreSessionLifecycleHelper by lazy {
    ARCoreSessionLifecycleHelper(this)
  }

  private val renderer: ArRenderer by lazy {
    ArRenderer(this, binding.glSurfaceView)
  }

  //================================================================================================
  // Initialize
  //================================================================================================
  override fun onBind() {
    with(binding) {
      lifecycleOwner = this@DepthActivity
      viewModel = this@DepthActivity.viewModel
    }

    repeatOnStarted {
      viewModel.depthEvent.collect(::observeEvent)
    }
  }

  override fun initView() {
    configureArCoreSessionHelper()
    configureRenderer()
  }

  //================================================================================================
  // Functions
  //================================================================================================
  private fun configureArCoreSessionHelper() {
    arCoreSessionHelper.exceptionCallback = { exception ->
      val message =
        when (exception) {
          is UnavailableUserDeclinedInstallationException ->
            "Please install Google Play Services for AR"
          is UnavailableApkTooOldException -> "Please update ARCore"
          is UnavailableSdkTooOldException -> "Please update this app"
          is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
          is CameraNotAvailableException -> "Camera not available. Try restarting the app."
          else -> "Failed to create AR session: $exception"
        }
    }
    arCoreSessionHelper.beforeSessionResume = ::configureSession
    lifecycle.addObserver(arCoreSessionHelper)
  }

  private fun configureRenderer() {
    lifecycle.addObserver(renderer)
    renderer.saveBitmap = viewModel::addFrame
  }

  private fun configureSession(session: Session) {
    val config = session.config.apply {
      depthMode = Config.DepthMode.AUTOMATIC
      focusMode = Config.FocusMode.AUTO
    }

    session.configure(config)
  }
}