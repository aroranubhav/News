package com.maxi.news.data.source.local

import com.maxi.news.data.source.local.dao.NewsSourcesDao
import com.maxi.news.data.source.local.entity.NewsSourceEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val dao: NewsSourcesDao
) {

    suspend fun insertNewsSources(sources: List<NewsSourceEntity>) =
        dao.insertNewsSources(sources)

    fun getNewsSources(): Flow<List<NewsSourceEntity>> =
        dao.getNewsSources()


    suspend fun clearNewsSources() =
        dao.clearNewsSources()
}