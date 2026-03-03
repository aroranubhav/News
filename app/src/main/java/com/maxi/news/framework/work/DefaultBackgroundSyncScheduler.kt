package com.maxi.news.framework.work

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.maxi.news.common.Constants.KEY_NEWS_SOURCES
import com.maxi.news.common.DispatcherProvider
import com.maxi.news.common.SyncState
import com.maxi.news.data.source.local.entity.NewsSourceEntity
import com.maxi.news.data.sync.BackgroundSyncScheduler
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class DefaultBackgroundSyncScheduler(
    private val workManager: WorkManager,
    private val json: Json,
    private val dispatchers: DispatcherProvider
) : BackgroundSyncScheduler {

    companion object {
        const val TAG_SYNC_NEWS_SOURCES = "sync_news_sources"
        const val WORK_NAME_SYNC_NEWS_SOURCES = "sync_news_sources_unique"
        const val KEY_SYNC_TIMESTAMP = "key_sync_timestamp"
    }

    override fun scheduleNewsSourcesSync(sources: List<NewsSourceEntity>) {
        val inputData = workDataOf(
            KEY_NEWS_SOURCES to json.encodeToString(
                ListSerializer(NewsSourceEntity.serializer()),
                sources
            )
        )

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncNewsSourcesWorker>()
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(TAG_SYNC_NEWS_SOURCES)
            .build()

        workManager
            .enqueueUniqueWork(
                WORK_NAME_SYNC_NEWS_SOURCES,
                ExistingWorkPolicy.REPLACE,
                syncWorkRequest
            )
    }

    override suspend fun checkSyncState(): SyncState = withContext(dispatchers.io) {
        val workInfos = workManager.getWorkInfosByTag(TAG_SYNC_NEWS_SOURCES).await()

        return@withContext when {
            workInfos.any { it.state == WorkInfo.State.RUNNING } -> SyncState.SYNCING
            workInfos.any { it.state == WorkInfo.State.ENQUEUED } -> SyncState.PENDING
            workInfos.any { it.state == WorkInfo.State.FAILED } -> SyncState.FAILED
            else -> SyncState.SYNCED
        }
    }

    override suspend fun getLastSyncTime(): Long = withContext(dispatchers.io) {
        val workInfos = workManager.getWorkInfosByTag(TAG_SYNC_NEWS_SOURCES).await()

        workInfos
            .filter {
                it.state == WorkInfo.State.SUCCEEDED
            }
            .maxOfOrNull {
                it.outputData.getLong(KEY_SYNC_TIMESTAMP, 0L)
            }
            ?: 0L
    }

    override fun cancelSync() {
        workManager.cancelUniqueWork(WORK_NAME_SYNC_NEWS_SOURCES)
    }
}