package ai.deepfine.domain.repository

import ai.deepfine.data.datasource.FileSaveDataSource
import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.zip
import java.io.File
import javax.inject.Inject

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-11-11
 * @version 1.0.0
 */
interface FileSaveRepository {
  suspend fun saveDepthImagePair(cameraBitmap: Bitmap, depthBitmap: Bitmap, timeStamp: Long): Flow<Unit>
}

class FileSaveRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val dataSource: FileSaveDataSource
) : FileSaveRepository {
  override suspend fun saveDepthImagePair(cameraBitmap: Bitmap, depthBitmap: Bitmap, timeStamp: Long): Flow<Unit> {
    val cameraFile = File(context.getExternalFilesDir(CAMERA_FILE_DIR), "${timeStamp}_1.png")
    val depthFile = File(context.getExternalFilesDir(DEPTH_FILE_DIR), "${timeStamp}_2.png")

    val saveCameraBitmap = dataSource.saveBitmapImage(cameraBitmap, cameraFile)
    val saveDepthBitmap = dataSource.saveBitmapImage(depthBitmap, depthFile)

    return saveCameraBitmap.zip(saveDepthBitmap) { _, _ -> }
  }

  companion object {
    const val CAMERA_FILE_DIR = "color"
    const val DEPTH_FILE_DIR = "depth"
  }
}