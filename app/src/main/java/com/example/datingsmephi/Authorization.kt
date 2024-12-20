package com.example.datingsmephi

import DatingReminderWorker
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class Authorization : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //WorkManager.getInstance(this).cancelAllWork()
        // Настроим задачу на выполнение каждый день
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isWorkScheduled = sharedPreferences.getBoolean("is_work_scheduled", false)
        val context = this@Authorization

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

        val sph = SharedPreferencesHelper(context)
        val UUID = sph.getUUID(context) ?: ""
        val (accessToken, refreshToken) = sph.getTokens(context)

        val userRepository = UserRepository(this@Authorization) // Передайте context вашей активности или приложения
        val database = AppDatabase.getInstance(applicationContext)
        kotlinx.coroutines.GlobalScope.launch {
            userRepository.deleteAllData()
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.DeleteForms(
                    bearer = "Bearer $accessToken",
                    UUID = UUID
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Успешное выполнение запроса
                        println("Пользователь успешно удалён")
                    } else {
                        // Обработка ошибки
                        println("Ошибка: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Обработка исключений
                    println("Исключение: ${e.message}")
                }
            }
        }
        sph.setUserImagesPaths(UserImagesPaths(emptyList()))



        sph.saveTokens(context, "", "")
        if (accessToken != null && refreshToken != null) {
            CoroutineScope(Dispatchers.IO).launch {
                validateAccessToken(context)
            }
        } else {
            setContent {
                AuthorizationNavigation()
            }
        }
    }

    private suspend fun validateAccessToken(context: Context) {
        val sph = SharedPreferencesHelper(context)
        val (accessToken, refreshToken) = sph.getTokens(context)
        val UUID = sph.getUUID(context)
        Log.d("Tokens", "Sended accessToken: $accessToken")
        Log.d("Tokens", "Sended refreshToken: $refreshToken")
        try {
            // Проверка действительности access_token (запрос к серверу)
            val response = RetrofitInstance.api.validateToken("Bearer $accessToken", UUID)
            if (response.isSuccessful) {
                // Токен действителен, можно продолжить
                withContext(Dispatchers.Main) {
                    val intent = Intent(context, AppActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }
            } else if (response.code() == 401) {
                refreshAccessToken(context)
                validateAccessToken(context)
            } else {
                // Ошибка (например, сервер недоступен)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Авторизируйтесь или зарегистрируйтесь",
                        Toast.LENGTH_LONG
                    ).show()
                    setContent {
                        AuthorizationNavigation()
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Ошибка сети: ${e.message}", Toast.LENGTH_LONG).show()
                setContent {
                    AuthorizationNavigation()
                }
            }
        }
    }
}