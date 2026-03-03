package com.maxi.news.framework.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.maxi.news.common.Constants.DB_SYNC_TIME
import com.maxi.news.common.Constants.KEY_NEWS_SOURCES
import com.maxi.news.data.source.local.dao.NewsSourcesDao
import com.maxi.news.data.source.local.entity.NewsSourceEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.json.Json
import java.lang.Exception

@HiltWorker
class SyncNewsSourcesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val json: Json,
    private val dao: NewsSourcesDao
) : CoroutineWorker(
    context,
    workerParams
) {
    override suspend fun doWork(): Result {
        return try {
            val newsSourcesJson = inputData.getString(KEY_NEWS_SOURCES)
                ?: return Result.failure()
            val newsSources = json.decodeFromString<List<NewsSourceEntity>>(newsSourcesJson)
            dao.insertNewsSources(newsSources)

            val outputData = workDataOf(
                DB_SYNC_TIME to System.currentTimeMillis()
            )
            Result.success(outputData)
        } catch (e: Exception) {
            if (runAttemptCount < 5) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}