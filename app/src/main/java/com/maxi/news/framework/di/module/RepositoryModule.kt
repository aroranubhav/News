package com.maxi.news.framework.di.module

import com.maxi.news.data.repository.DefaultNewsSourcesRepository
import com.maxi.news.data.source.local.LocalDataSource
import com.maxi.news.data.source.remote.RemoteDataSource
import com.maxi.news.domain.repository.NewsSourcesRepository
import com.maxi.news.data.sync.BackgroundSyncScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNewsSourcesRepository(
        remote: RemoteDataSource,
        local: LocalDataSource,
        backgroundSyncScheduler: BackgroundSyncScheduler
    ): NewsSourcesRepository =
        DefaultNewsSourcesRepository(
            remote,
            local,
            backgroundSyncScheduler
        )
}