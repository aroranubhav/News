package com.maxi.news.domain.repository

import com.maxi.news.common.Resource
import com.maxi.news.domain.model.NewsSource
import kotlinx.coroutines.flow.Flow

interface NewsSourcesRepository {

    suspend fun fetchAndCacheNewsSources() : Resource<List<NewsSource>>

    fun observeNewsSources(): Flow<Resource<List<NewsSource>>>
}