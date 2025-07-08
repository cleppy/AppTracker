package com.example.apptracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(appUsage: AppUsage)

    // show selected dates usage data
    // Flow<> automatic update
    @Query("SELECT * FROM app_usage_stats WHERE date = :date ORDER BY usageTimeInMillis DESC")
    fun getUsageForDate(date: Long): Flow<List<AppUsage>>
}