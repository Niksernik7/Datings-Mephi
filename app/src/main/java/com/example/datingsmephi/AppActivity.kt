package com.example.datingsmephi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.internal.composableLambdaInstance
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AppActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //ОТСЮДА ДО СЛЕДУЮЩЕЙ ПОМЕТКИ ДОЛЖНО ЛЕЖАТЬ В DOMAIN?
        //ВЫЗЫВАТЬСЯ ДОЛЖНО ПРИ НАЖАТИИ КНОПКИ РЕДАКТИРОВАТЬ

        val context = this@AppActivity
        val sph = SharedPreferencesHelper(context)
        var userData = sph.loadProfile()
        val UUID = sph.getUUID(context)
        userData = userData.copy(UUID = UUID)
        sph.saveProfile(userData, context)

        CoroutineScope(Dispatchers.IO).launch {
            sendGetUdRequest(context)
        }


        // ДО ЭТОГО МЕСТА
        setContent {
            AppScreen()
        }
    }

    suspend fun sendGetUdRequest(
        context: Context,
    ) {
        val sph = SharedPreferencesHelper(context)
        val (accessToken, refreshToken) = sph.getTokens(context)
        var userData: UserData? = null
        val user_id = sph.getUUID(context)
        val UUID = if (user_id != null) user_id else ""

        try {
            val response = RetrofitInstance.api.getUserData("Bearer $accessToken", UUID)
            userData = response.body()
            Log.e("TG", "${userData?.tg}")
            when (response.code()) {
                200 -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Данные успешно получены!!", Toast.LENGTH_LONG)
                            .show()
                        sph.saveProfile(userData, context)
                    }
                }

                401 -> {
                    refreshAccessToken(context)
                    sendGetUdRequest(context)
                }

                500 -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Ошибка сервера 500: Попробуйте позже",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                else -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Ошибка: ${response.code()}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}


