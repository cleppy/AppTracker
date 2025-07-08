package com.example.apptracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "app_usage_stats",
    indices = [Index(value = ["packageName", "date"], unique = true)] // Bu satır eklendi
)
data class AppUsage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val packageName: String,
    val appName: String,
    val usageTimeInMillis: Long,
    val date: Long
)
