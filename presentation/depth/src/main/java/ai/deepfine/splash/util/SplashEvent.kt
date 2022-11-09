package ai.deepfine.splash.util

sealed interface SplashEvent {
  object Login : SplashEvent

  interface Observer {
    fun observeEvent(event: SplashEvent) {
      when (event) {
        Login -> observeLogin()
      }
    }

    fun observeLogin()
  }
}