package com.maxi.news.data.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.maxi.news.data.source.local.dao.NewsSourcesDao
import com.maxi.news.data.source.local.entity.NewsSourceEntity

@Database(
    entities = [NewsSourceEntity::class],
    version = 1,
    exportSchema = true
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun newsSourcesDao(): NewsSourcesDao
}