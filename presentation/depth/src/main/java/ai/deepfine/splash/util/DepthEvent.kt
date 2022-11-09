package ai.deepfine.splash.util

sealed interface DepthEvent {

  interface Observer {
    fun observeEvent(event: DepthEvent) {
    }
  }
}