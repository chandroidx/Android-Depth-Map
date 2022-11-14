package ai.deepfine.splash.viewmodel

import ai.deepfine.domain.usecase.download.SaveDepthImagePair
import ai.deepfine.presentation.base.BaseViewModelImpl
import ai.deepfine.presentation.coroutine.BaseCoroutineScope
import ai.deepfine.splash.util.DepthEvent
import ai.deepfine.splash.util.DepthFrame
import ai.deepfine.utility.utils.EventFlow
import ai.deepfine.utility.utils.MutableEventFlow
import ai.deepfine.utility.utils.asEventFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @Description SplashViewModel
 * @author yc.park (DEEP.FINE)
 * @since 2022-04-14
 * @version 1.0.0
 */
@HiltViewModel
class DepthViewModel @Inject constructor(
  private val saveDepthImagePair: SaveDepthImagePair,
  scope: BaseCoroutineScope,
) : BaseViewModelImpl(), BaseCoroutineScope by scope {

  //================================================================================================
  // Properties
  //================================================================================================
  private val _depthEvent = MutableEventFlow<DepthEvent>()
  val depthEvent: EventFlow<DepthEvent>
    get() = _depthEvent.asEventFlow()

  private var isSaveRunning = false
  private val frameQueue = ArrayDeque<DepthFrame>()

  fun addFrame(depthFrame: DepthFrame? = null) {
    depthFrame?.let(frameQueue::add)

    if (!isSaveRunning) {
      saveFrame()
    }
  }

  private fun saveFrame() {
    isSaveRunning = true

    viewModelScope.launch {
      val frameForSave = frameQueue.removeFirstOrNull() ?: return@launch
      saveDepthImagePair.execute(SaveDepthImagePair.Params(frameForSave.cameraBitmap, frameForSave.depthBitmap, frameForSave.timeStamp))
        .collect {
          if (frameQueue.isEmpty()) {
            isSaveRunning = false
          } else {
            saveFrame()
          }
        }
    }
  }

  override fun clearViewModel() {
    releaseCoroutine()
  }
}