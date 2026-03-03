package com.maxi.news.data.repository

import com.maxi.news.common.CacheStatus
import com.maxi.news.common.DBSyncMetaData
import com.maxi.news.common.ErrorType
import com.maxi.news.common.Resource
import com.maxi.news.data.common.safeApiCall
import com.maxi.news.data.common.safeDbCall
import com.maxi.news.data.mapper.toDomainList
import com.maxi.news.data.mapper.toEntityList
import com.maxi.news.data.source.local.LocalDataSource
import com.maxi.news.data.source.remote.RemoteDataSource
import com.maxi.news.domain.model.NewsSource
import com.maxi.news.domain.repository.NewsSourcesRepository
import com.maxi.news.data.sync.BackgroundSyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DefaultNewsSourcesRepository @Inject constructor(
    private val remote: RemoteDataSource,
    private val local: LocalDataSource,
    private val backgroundSyncScheduler: BackgroundSyncScheduler
) : NewsSourcesRepository {

    override suspend fun fetchAndCacheNewsSources(): Resource<List<NewsSource>> {
        val apiResult = safeApiCall {
            remote.getNewsSources(true)
        }

        return when (apiResult) {
            is Resource.Success -> {
                val sourcesEntities = apiResult.data.sources.toEntityList()
                val sources = apiResult.data.sources.toDomainList()

                val dbResult = safeDbCall {
                    local.insertNewsSources(sourcesEntities)
                }

                when (dbResult) {
                    is Resource.Success -> {
                        Resource.Success(
                            sources,
                            syncMetaData = DBSyncMetaData(
                                isCached = true,
                                cacheStatus = CacheStatus.SYNCED
                            )
                        )
                    }

                    is Resource.Error -> {
                        //schedule background retry
                        backgroundSyncScheduler.scheduleNewsSourcesSync(sourcesEntities)

                        Resource.Success(
                            sources,
                            syncMetaData = DBSyncMetaData(
                                isCached = false,
                                cacheStatus = CacheStatus.SYNCING,
                                cacheError = "Syncing in background ${dbResult.message}"
                            )
                        )
                    }

                    is Resource.Loading,
                    is Resource.NoChange -> {
                        Resource.Error(
                            ErrorType.UNKNOWN,
                            "Unexpected error occurred!"
                        )
                    }
                }
            }

            is Resource.Error -> {
                apiResult
            }

            is Resource.NoChange -> {
                handleNoChange()
            }

            is Resource.Loading -> {
                Resource.Error(
                    ErrorType.UNKNOWN,
                    "Unexpected error occurred!"
                )
            }
        }
    }

    override fun observeNewsSources(): Flow<Resource<List<NewsSource>>> = flow {
        emit(Resource.Loading)

        local.getNewsSources()
            .catch { e ->
                emit(
                    Resource.Error(
                        ErrorType.DATABASE,
                        e.message
                    )
                )
            }.collect {
                val sources = it.toDomainList()
                emit(Resource.Success(sources))
            }
    }.catch { e -> //catch any uncaught exceptions in the flow
        emit(
            Resource.Error(
                ErrorType.UNKNOWN,
                e.message
            )
        )
    }

    private fun handleNoChange(): Resource<List<NewsSource>> {
        return Resource.Success(
            data = emptyList(),
            syncMetaData = DBSyncMetaData(
                isCached = true,
                cacheStatus = CacheStatus.SYNCED,
                notModified = true
            )
        )
    }
}