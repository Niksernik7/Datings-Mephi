package com.example.datingsmephi

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


suspend fun getImagesFromServer(
    context: Context,
) {
    val sph = SharedPreferencesHelper(context)
    val (accessToken, refreshToken) = sph.getTokens(context)
    val user_id = sph.getUUID(context)
    val UUID = if (user_id == null) "" else user_id

    if (accessToken.isNullOrEmpty() || UUID.isEmpty()) {
        Log.e("FetchError", "Access token or UUID is missing")
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Данные для запроса отсутствуют", Toast.LENGTH_LONG).show()
        }
        return
    }

    // Инициализация Retrofit API
    val apiService = RetrofitInstance.api

    try {
        // Выполняем запрос к серверу
        val response = apiService.getUserImagesPaths("Bearer $accessToken", UUID)

        when (response.code()) {
            200 -> {
                // Парсим JSON и получаем массив ссылок
                val photoNames = response.body()?.photo_name ?: emptyList()
                withContext(Dispatchers.Main) {
                    val UserImagesPaths = UserImagesPaths(photoNames)
                    sph.setUserImagesPaths(UserImagesPaths)
                }

                if (photoNames.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Пути получены",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "У вас нет фоток",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            401 -> {
                refreshAccessToken(context)
                getImagesFromServer(context)
            }

            500 -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Ошибка сервера 500",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            else -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Ответ не положительный",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    } catch (e: Exception) {
        Log.e("FetchError", "Error fetching images: ${e.message}")
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Ошибка получения путей",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}




@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun AppScreen() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Survey", "Likes", "Profile")
    Log.e("Survey", "Вызвался AppScreen")
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .height(56.dp)
            ) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxSize(),
                    containerColor = Color.LightGray
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                when (item) {
                                    "Likes" -> Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "Likes"
                                    )

                                    "Survey" -> Icon(
                                        imageVector = Icons.Filled.Assessment,
                                        contentDescription = "Survey"
                                    )

                                    "Profile" -> Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "Profile"
                                    )
                                }
                            },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                                when (index) {
                                    0 -> navController.navigate("survey_screen") {
                                        navController.popBackStack()
                                    }

                                    1 -> navController.navigate("likes_screen") {
                                        navController.popBackStack()
                                    }

                                    2 -> navController.navigate("profile_screen") {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavigation(navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScreenPreview() {
    AppScreen()
}
