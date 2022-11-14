package ai.deepfine.splash.util

import android.graphics.Bitmap

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-11-11
 * @version 1.0.0
 */
data class DepthFrame(
  val cameraBitmap: Bitmap,
  val depthBitmap: Bitmap,
  val timeStamp: Long
)