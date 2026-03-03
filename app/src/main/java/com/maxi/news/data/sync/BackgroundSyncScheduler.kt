package com.maxi.news.data.sync

import com.maxi.news.common.SyncState
import com.maxi.news.data.source.local.entity.NewsSourceEntity

interface BackgroundSyncScheduler {

    fun scheduleNewsSourcesSync(sources: List<NewsSourceEntity>)
    suspend fun checkSyncState(): SyncState
    suspend fun getLastSyncTime(): Long
    fun cancelSync()
}