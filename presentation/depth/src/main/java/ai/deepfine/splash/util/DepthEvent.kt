package ai.deepfine.splash.util

sealed interface DepthEvent {
  data class CameraError(val error: Int? = null) : DepthEvent

  interface Observer {
    fun observeEvent(event: DepthEvent) {
    }
  }
}