package ai.deepfine.androidBase.di

import ai.deepfine.domain.repository.FileDownloadRepository
import ai.deepfine.domain.repository.FileSaveRepository
import ai.deepfine.domain.usecase.download.DownloadFile
import ai.deepfine.domain.usecase.download.SaveDepthImagePair
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @Description UseCaseModule
 * @author yc.park (DEEP.FINE)
 * @since 2022-04-14
 * @version 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {
  @Provides
  @Singleton
  fun provideDownloadFileUseCase(repository: FileDownloadRepository) = DownloadFile(repository)

  @Provides
  @Singleton
  fun provideSaveDepthImagePairUseCase(repository: FileSaveRepository) = SaveDepthImagePair(repository)
}