package ai.deepfine.splash.viewmodel

import ai.deepfine.presentation.base.BaseViewModelImpl
import ai.deepfine.presentation.coroutine.BaseCoroutineScope
import ai.deepfine.splash.util.SplashEvent
import ai.deepfine.utility.utils.EventFlow
import ai.deepfine.utility.utils.MutableEventFlow
import ai.deepfine.utility.utils.asEventFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * @Description SplashViewModel
 * @author yc.park (DEEP.FINE)
 * @since 2022-04-14
 * @version 1.0.0
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
  scope: BaseCoroutineScope,
) : BaseViewModelImpl(), BaseCoroutineScope by scope {

  //================================================================================================
  // Properties
  //================================================================================================
  private val _splashEvent = MutableEventFlow<SplashEvent>()
  val splashEvent: EventFlow<SplashEvent>
    get() = _splashEvent.asEventFlow()

  fun showLoginAfterMillis(timeMillis: Long = SPLASH_MILLIS) {
    viewModelScope.launch {
      withContext(coroutineContext) {
        delay(timeMillis)
        _splashEvent.emit(SplashEvent.Login)
      }
    }
  }

  override fun clearViewModel() {
    releaseCoroutine()
  }

  //================================================================================================
  // Companion object
  //================================================================================================
  companion object {
    private const val SPLASH_MILLIS = 1500L
  }
}