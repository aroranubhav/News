package com.maxi.news.data.source.remote.dto

import com.maxi.news.data.common.DataConstants.Keys
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsSourcesResponseDto(
    @SerialName(Keys.STATUS)
    val status: String,
    @SerialName(Keys.SOURCES)
    val sources: List<NewsSourceDto>
)
