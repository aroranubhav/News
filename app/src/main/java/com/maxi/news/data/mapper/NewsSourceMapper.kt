package com.maxi.news.data.mapper

import com.maxi.news.data.source.local.entity.NewsSourceEntity
import com.maxi.news.data.source.remote.dto.NewsSourceDto
import com.maxi.news.domain.model.NewsSource

fun NewsSourceDto.toEntity(): NewsSourceEntity =
    NewsSourceEntity(id, name, description, url)

fun List<NewsSourceDto>.toEntityList(): List<NewsSourceEntity> = map {
    it.toEntity()
}

fun NewsSourceEntity.toDomain(): NewsSource =
    NewsSource(
        id,
        name,
        description ?: "",
        url
    )

fun List<NewsSourceEntity>.toDomainList(): List<NewsSource> = map {
    it.toDomain()
}

fun NewsSourceDto.toDomain(): NewsSource =
    NewsSource(
        id,
        name,
        description ?: "",
        url
    )

fun List<NewsSourceDto>.toDomainList(): List<NewsSource> = map {
    it.toDomain()
}