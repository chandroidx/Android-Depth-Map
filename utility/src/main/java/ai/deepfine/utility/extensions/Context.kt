package ai.deepfine.utility.extensions

import android.app.DownloadManager
import android.content.Context
import android.hardware.camera2.CameraManager
import android.view.LayoutInflater

val Context.downloadManager: DownloadManager
  get() = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

val Context.layoutInflater: LayoutInflater
  get() = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

val Context.cameraManager: CameraManager
  get() = getSystemService(Context.CAMERA_SERVICE) as CameraManager