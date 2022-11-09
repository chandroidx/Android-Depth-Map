package ai.deepfine.splash.view

import ai.deepfine.presentation.base.BaseActivity
import ai.deepfine.presentation.extensions.repeatOnStarted
import ai.deepfine.splash.R
import ai.deepfine.splash.databinding.ActivityDepthBinding
import ai.deepfine.splash.util.DepthEvent
import ai.deepfine.splash.viewmodel.DepthViewModel
import androidx.activity.viewModels
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
  }

  //================================================================================================
  // Observe
  //================================================================================================
}