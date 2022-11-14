package ai.deepfine.splash.util

import ai.deepfine.splash.util.helper.CameraPermissionHelper
import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-11-09
 * @version 1.0.0
 */
class ARCoreSessionLifecycleHelper(
  val activity: Activity,
  val features: Set<Session.Feature> = setOf()
) : DefaultLifecycleObserver {
  var installRequested = false
  var session: Session? = null
    private set

  var exceptionCallback: ((Exception) -> Unit)? = null
  var beforeSessionResume: ((Session) -> Unit)? = null

  private fun tryCreateSession(): Session? {
    if (!CameraPermissionHelper.hasCameraPermission(activity)) {
      CameraPermissionHelper.requestCameraPermission(activity)
      return null
    }

    return try {
      when (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
        ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
          installRequested = true
          return null
        }
        ArCoreApk.InstallStatus.INSTALLED -> {
        }
      }
      Session(activity, setOf())
    } catch (e: Exception) {
      exceptionCallback?.invoke(e)
      null
    }
  }

  override fun onResume(owner: LifecycleOwner) {
    val session = this.session ?: tryCreateSession() ?: return
    try {
      beforeSessionResume?.invoke(session)
      session.resume()
      this.session = session
    } catch (e: CameraNotAvailableException) {
      exceptionCallback?.invoke(e)
    }
  }

  override fun onPause(owner: LifecycleOwner) {
    session?.pause()
  }

  override fun onDestroy(owner: LifecycleOwner) {
    super.onDestroy(owner)
    session?.close()
    session = null
  }
}