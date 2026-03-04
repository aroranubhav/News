package com.maxi.news.domain.usecase

import com.maxi.news.common.Resource
import com.maxi.news.domain.model.NewsSource
import com.maxi.news.domain.repository.NewsSourcesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNewsSourcesUseCase @Inject constructor(
    private val repository: NewsSourcesRepository
) {

    operator fun invoke(): Flow<Resource<List<NewsSource>>> =
        repository.observeNewsSources()
}