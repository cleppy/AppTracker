// UsageStatsWorker.kt
package com.example.apptracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.apptracker.data.AppUsageDatabase
import com.example.apptracker.data.AppUsageRepository
import com.example.apptracker.utils.isUsageStatsPermissionGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsageStatsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // background work
        return withContext(Dispatchers.IO) {
            // permission control
            if (!isUsageStatsPermissionGranted(applicationContext)) {
                // if permission false
                return@withContext Result.failure()
            }

            val database = AppUsageDatabase.getDatabase(applicationContext)
            val repository = AppUsageRepository(database.appUsageDao(), applicationContext)

            try {
                repository.refreshUsageStatsForToday()
                Result.success() // success
            } catch (e: Exception) {
                // if gives error retry
                Result.retry()
            }
        }
    }
}
    