package com.maxi.news.data.source.remote.api

import com.maxi.news.data.common.DataConstants.EndPoints
import com.maxi.news.data.common.DataConstants.Headers
import com.maxi.news.data.source.remote.dto.NewsSourcesResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface NetworkApiService {

    @GET(EndPoints.SOURCES)
    suspend fun getNewsSources(
        @Header(Headers. X_FORCE_REFRESH) forceRefresh: String? = null
    ): Response<NewsSourcesResponseDto>
}