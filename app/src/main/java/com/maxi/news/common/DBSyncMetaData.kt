package com.maxi.news.common

data class DBSyncMetaData(
    val isCached: Boolean = true,
    val cacheStatus: CacheStatus = CacheStatus.SYNCED,
    val cacheError: String? = null,
    val notModified: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
