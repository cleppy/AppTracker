// MainActivity.kt
package com.example.apptracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.apptracker.data.AppUsageDatabase
import com.example.apptracker.data.AppUsageRepository
import com.example.apptracker.gui.main.MainScreen
import com.example.apptracker.gui.main.MainViewModel
import com.example.apptracker.gui.main.MainViewModelFactory
import com.example.apptracker.ui.theme.AppTrackerTheme
import com.example.apptracker.worker.UsageStatsWorker

import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppUsageDatabase.getDatabase(applicationContext)
        val repository = AppUsageRepository(database.appUsageDao(), applicationContext)
        val viewModelFactory = MainViewModelFactory(repository)
        val viewModel: MainViewModel by lazy {
            viewModelFactory.create(MainViewModel::class.java)
        }

        // start WorkManager
        setupPeriodicWork()

        enableEdgeToEdge()
        setContent {
            AppTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun setupPeriodicWork() {
        // work every 15 minutes
        val usageStatsWorkRequest = PeriodicWorkRequestBuilder<UsageStatsWorker>(
            15, TimeUnit.MINUTES
        )
            .addTag("UsageStatsWorkerTag")
            .build()


        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "UsageStatsWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            usageStatsWorkRequest
        )
    }
}
    