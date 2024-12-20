package com.example.datingsmephi

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role.Companion.Checkbox
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    trigger: MutableState<Boolean>,
    navController: NavController,
) {
    val context = LocalContext.current
    val sph = SharedPreferencesHelper(context)

    // Используем LaunchedEffect для извлечения фильтров из SharedPreferences при первом рендере
    val userFilters = remember { mutableStateOf(sph.getFilters(context) ?: UserFilters(courseStart = 1, courseEnd = 9, genderMan = false, genderWoman = false)) }

    val courseRangeMin = remember { mutableStateOf(userFilters.value.courseStart.toFloat()) } // Минимальное значение диапазона
    val courseRangeMax = remember { mutableStateOf(userFilters.value.courseEnd.toFloat()) } // Максимальное значение диапазона
    val genderMan = remember { mutableStateOf(userFilters.value.genderMan) }
    val genderWoman = remember { mutableStateOf(userFilters.value.genderWoman) }
    val userRepository = UserRepository(context)
    val userDataCount by userRepository.getUserDataCount().collectAsState(initial = -1)


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Фильтры") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ползунок для диапазона
                Text(text = "Курс")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("От ${courseRangeMin.value.toInt()}")
                    Text("До ${courseRangeMax.value.toInt()}")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Slider(
                        value = courseRangeMin.value,
                        onValueChange = { value ->
                            if (value <= courseRangeMax.value) {
                                courseRangeMin.value = value
                                // Логируем изменения
                                Log.d("FiltersScreen", "Min course set to: ${courseRangeMin.value.toInt()}")
                            }
                        },
                        valueRange = 1f..9f,
                        modifier = Modifier.weight(1f)
                    )
                    Slider(
                        value = courseRangeMax.value,
                        onValueChange = { value ->
                            if (value >= courseRangeMin.value) {
                                courseRangeMax.value = value
                                // Логируем изменения
                                Log.d("FiltersScreen", "Max course set to: ${courseRangeMax.value.toInt()}")
                            }
                        },
                        valueRange = 1f..9f,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки с галочками
                Text(text = "Кто тебе интересен?")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = genderMan.value,
                        onCheckedChange = { isChecked ->
                            genderMan.value = isChecked
                            // Логируем изменения
                            Log.d("FiltersScreen", "Gender Man set to: $isChecked")
                        }
                    )
                    Text("Парни")

                    Checkbox(
                        checked = genderWoman.value,
                        onCheckedChange = { isChecked ->
                            genderWoman.value = isChecked
                            // Логируем изменения
                            Log.d("FiltersScreen", "Gender Woman set to: $isChecked")
                        }
                    )
                    Text("Девушки")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Кнопка сохранить
                Button(
                    onClick = {
                        // Создаем объект фильтра
                        val coursesStartEnd = Pair(courseRangeMin.value.toInt(), courseRangeMax.value.toInt())

                        // Обновляем состояние фильтров
                        userFilters.value = UserFilters(
                            courseStart = coursesStartEnd.first,
                            courseEnd = coursesStartEnd.second,
                            genderMan = genderMan.value,
                            genderWoman = genderWoman.value
                        )

                        // Логируем перед отправкой фильтров
                        Log.d("FiltersScreen", "Saving filters: ${userFilters.value}")

                        // Сохраняем фильтры
                        sph.saveFilters(context, userFilters.value)

                        val UUID = sph.getUUID(context) ?: ""
                        val (accessToken, refreshToken) = sph.getTokens(context)
                        val database = AppDatabase.getInstance(context)

                        kotlinx.coroutines.GlobalScope.launch {
                            userRepository.deleteAllData()
                            trigger.value = !trigger.value
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


                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить")
                }
            }
        }
    )
}



