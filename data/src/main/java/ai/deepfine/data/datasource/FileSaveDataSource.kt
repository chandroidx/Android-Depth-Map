package ai.deepfine.data.datasource

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-11-11
 * @version 1.0.0
 */

interface FileSaveDataSource {
  suspend fun saveBitmapImage(bitmap: Bitmap, file: File): Flow<Unit>
}

class FileSaveDataSourceImpl @Inject constructor() : FileSaveDataSource {
  override suspend fun saveBitmapImage(bitmap: Bitmap, file: File): Flow<Unit> = flow {
    if (!file.exists()) {
      file.createNewFile()
    }

    FileOutputStream(file).use { outputStream ->
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    }

    bitmap.recycle()
    emit(Unit)
  }.flowOn(Dispatchers.IO)
}