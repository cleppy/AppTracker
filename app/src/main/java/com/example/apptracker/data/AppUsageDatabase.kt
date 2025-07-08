package com.example.apptracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AppUsage::class], version = 1, exportSchema = false)
abstract class AppUsageDatabase : RoomDatabase() {

    abstract fun appUsageDao(): AppUsageDao

    companion object {
        @Volatile
        private var INSTANCE: AppUsageDatabase? = null

        fun getDatabase(context: Context): AppUsageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppUsageDatabase::class.java,
                    "app_usage_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}