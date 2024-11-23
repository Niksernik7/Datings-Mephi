package com.example.datingsmephi

import DatingReminderWorker
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.content.Context

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //WorkManager.getInstance(this).cancelAllWork()
        // Настроим задачу на выполнение каждый день
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isWorkScheduled = sharedPreferences.getBoolean("is_work_scheduled", false)

        if (!isWorkScheduled) {
            // Настроим задачу на выполнение каждый день
            val constraints = Constraints.Builder()
                .setRequiresCharging(false) // Не требует зарядки
                .setRequiresBatteryNotLow(true) // Не запускается при низком уровне заряда
                .build()

            val dailyWorkRequest = PeriodicWorkRequest.Builder(
                DatingReminderWorker::class.java,
                24, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            // Планирование задачи
            WorkManager.getInstance(this).enqueue(dailyWorkRequest)

            // Установить флаг
            sharedPreferences.edit().putBoolean("is_work_scheduled", true).apply()
        }

        // Устанавливаем контент с использованием Compose
        setContent {
            MainScreen()
        }
    }
}
