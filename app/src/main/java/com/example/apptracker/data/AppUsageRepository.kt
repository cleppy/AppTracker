// AppUsageRepository.kt
package com.example.apptracker.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class AppUsageRepository(private val appUsageDao: AppUsageDao, private val context: Context) {

    // Provides usage data for the selected date as a Flow.
    // ViewModel will observe this Flow.
    fun getUsageForDate(date: Long) = appUsageDao.getUsageForDate(date)

    // Main function to fetch real-time data from the system and update the database
    suspend fun refreshUsageStatsForToday() {
        withContext(Dispatchers.IO) {
            val today = getTodayStartTimestamp()
            val usageStatsMap = queryAndAggregateUsageStats(today, System.currentTimeMillis()) // Get aggregated stats

            if (usageStatsMap.isEmpty()) {
                // No usage stats found for today.
            }

            usageStatsMap.forEach { (packageName, usageStat) ->
                val appName = getAppNameFromPackage(packageName)
                val appUsage = AppUsage(
                    appName = appName,
                    packageName = packageName,
                    usageTimeInMillis = usageStat.totalTimeInForeground,
                    date = today
                )
                try {
                    appUsageDao.insertUsage(appUsage)
                } catch (e: Exception) {
                    // Error inserting/updating usage for app.
                    // You might want to log this error in a production app for monitoring.
                }
            }
        }
    }

    private fun queryAndAggregateUsageStats(startTime: Long, endTime: Long): Map<String, android.app.usage.UsageStats> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
    }

    private fun getAppNameFromPackage(packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            // If the app is uninstalled but still in logs
            packageName
        }
    }

    private fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
