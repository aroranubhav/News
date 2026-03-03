package com.maxi.news.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maxi.news.data.common.DataConstants.Keys
import com.maxi.news.data.common.DataConstants.Tables

@Entity(tableName = Tables.NEWS_SOURCES)
data class NewsSourceEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(Keys.ID)
    val id: String,
    @ColumnInfo(Keys.NAME)
    val name: String,
    @ColumnInfo(Keys.DESCRIPTION)
    val description: String?,
    @ColumnInfo(Keys.URL)
    val url: String
)
