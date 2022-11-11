package ai.deepfine.splash.util.helper

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-11-09
 * @version 1.0.0
 */
object CameraPermissionHelper {
  fun hasCameraPermission(activity: Activity) = ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED

  fun requestCameraPermission(activity: Activity) {
    ActivityCompat.requestPermissions(
      activity, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_CODE
    )
  }

  fun shouldShowRequestPermissionRationale(activity: Activity) = ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION)

  private const val CAMERA_PERMISSION_CODE = 0
  private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
}