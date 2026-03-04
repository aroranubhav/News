package com.maxi.news.domain.usecase

import com.maxi.news.common.Resource
import com.maxi.news.domain.model.NewsSource
import com.maxi.news.domain.repository.NewsSourcesRepository
import javax.inject.Inject

class FetchNewsSourcesUseCase @Inject constructor(
    private val repository: NewsSourcesRepository
) {

    suspend operator fun invoke(): Resource<List<NewsSource>> =
        repository.fetchAndCacheNewsSources()


}