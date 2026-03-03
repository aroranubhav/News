package com.maxi.news.data.source.remote.dto

import com.maxi.news.data.common.DataConstants.Keys
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsSourceDto(
    @SerialName(Keys.ID)
    val id: String,
    @SerialName(Keys.NAME)
    val name: String,
    @SerialName(Keys.DESCRIPTION)
    val description: String?,
    @SerialName(Keys.URL)
    val url: String
)
