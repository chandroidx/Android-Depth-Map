package ai.deepfine.domain.usecase.download

import ai.deepfine.domain.repository.FileSaveRepository
import ai.deepfine.domain.utils.UseCase
import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @Description
 * @author yc.park (DEEP.FINE)
 * @since 2022-11-11
 * @version 1.0.0
 */
class SaveDepthImagePair @Inject constructor(private val repository: FileSaveRepository) : UseCase<Unit, SaveDepthImagePair.Params>() {
  override suspend fun execute(params: Params): Flow<Unit> = repository.saveDepthImagePair(params.cameraBitmap, params.depthBitmap, params.timestamp)

  data class Params(val cameraBitmap: Bitmap, val depthBitmap: Bitmap, val timestamp: Long)
}