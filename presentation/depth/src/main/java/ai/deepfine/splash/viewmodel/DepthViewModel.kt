package ai.deepfine.splash.viewmodel

import ai.deepfine.presentation.base.BaseViewModelImpl
import ai.deepfine.presentation.coroutine.BaseCoroutineScope
import ai.deepfine.splash.util.DepthEvent
import ai.deepfine.utility.extensions.cameraManager
import ai.deepfine.utility.utils.EventFlow
import ai.deepfine.utility.utils.L
import ai.deepfine.utility.utils.MutableEventFlow
import ai.deepfine.utility.utils.asEventFlow
import android.content.Context
import android.hardware.camera2.CameraDevice
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import com.google.ar.core.Config
import com.google.ar.core.ImageFormat
import com.google.ar.core.Session
import com.google.ar.core.SharedCamera
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.EnumSet
import javax.inject.Inject

/**
 * @Description SplashViewModel
 * @author yc.park (DEEP.FINE)
 * @since 2022-04-14
 * @version 1.0.0
 */
@HiltViewModel
class DepthViewModel @Inject constructor(
  scope: BaseCoroutineScope,
) : BaseViewModelImpl(), BaseCoroutineScope by scope {

  //================================================================================================
  // Properties
  //================================================================================================
  private val _depthEvent = MutableEventFlow<DepthEvent>()
  val depthEvent: EventFlow<DepthEvent>
    get() = _depthEvent.asEventFlow()


  override fun clearViewModel() {
    releaseCoroutine()
  }


  //================================================================================================
  // Companion
  //================================================================================================
}