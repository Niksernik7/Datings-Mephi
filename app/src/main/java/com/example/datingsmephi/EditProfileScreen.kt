package com.example.datingsmephi

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


@Composable
fun ZodiacSelection(selectedZodiac: String?, onZodiacSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    val zodiacSigns = listOf(
        "Овен", "Телец", "Близнецы", "Рак", "Лев", "Дева", "Весы", "Скорпион",
        "Стрелец", "Козерог", "Водолей", "Рыбы"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Заголовок "Зодиак"
        Text(
            text = "Зодиак",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Овальная плашка с надписью, похожая на TextField
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Высота, как у TextField
                .background(
                    Color.LightGray,
                    shape = RoundedCornerShape(8.dp)
                ) // Цвет и скругление
                .clickable { expanded = !expanded } // При клике открывается список
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            var displayText = ""
            if (selectedZodiac != "Овен" && selectedZodiac != "Телец" && selectedZodiac != "Близнецы" && selectedZodiac != "Рак" &&
                selectedZodiac != "Лев" && selectedZodiac != "Дева" && selectedZodiac != "Весы" && selectedZodiac != "Скорпион" &&
                selectedZodiac != "Стрелец" && selectedZodiac != "Козерог" && selectedZodiac != "Водолей" && selectedZodiac != "Рыбы") {
                displayText = "Не выбрано"
            } else {
                displayText = selectedZodiac
            }
            val textColor = Color.Black

            Text(
                text = displayText,
                fontSize = 16.sp,
                color = textColor
            )
        }

        // Показываем список знаков зодиака, если раскрыто меню
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                zodiacSigns.forEach { zodiac ->
                    Text(
                        text = zodiac,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onZodiacSelect(zodiac) // Обновляем выбранный знак
                                expanded = false // Закрытие списка после выбора
                            }
                            .padding(16.dp)
                            .background(
                                color = if (selectedZodiac == zodiac) MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.1f
                                ) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SectionWithClickableGrid(
    title: String,
    items: List<String>,
    selectedItems: MutableMap<String, Boolean>
){
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Ограничение высоты
        ) {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2), // Две строки
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items.size) { index ->
                    val item = items[index]
                    Button(
                        onClick = { selectedItems[item] = !(selectedItems[item] ?: false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedItems[item] == true) Color.Blue else Color.Gray
                        ),
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth(0.45f)
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun EditProfileScreen(
    onClose: () -> Unit,
    context: Context,
    login: String?,
) {
    val coroutineScope = rememberCoroutineScope() // Скоуп корутин для этого Composable
    // Создаем экземпляр SharedPreferencesHelper
    val sharedPreferencesHelper = remember { SharedPreferencesHelper(context) }
    // Загружаем данные, если они есть
    val profileData = sharedPreferencesHelper.loadProfile()

    var user_changebale_data by remember { mutableStateOf(profileData) }

    val savedGoals by remember { mutableStateOf(user_changebale_data.goals) }
    val savedInterests by remember { mutableStateOf(user_changebale_data.interests) }

    // Состояния для списков целей и интересов
    val goals = listOf(
        "партнер по секции",
        "команда для хакатона",
        "совместная учеба",
        "совместный проект",
        "набор на работу"
    )
    val interests = listOf(
        "программирование",
        "математика",
        "самбо",
        "рыбалка",
        "футбол"
    )

    val selectedGoals = remember { mutableStateMapOf<String, Boolean>().apply {
        goals.forEach { goal -> put(goal, savedGoals.contains(goal)) }
    } }

    val selectedInterests = remember { mutableStateMapOf<String, Boolean>().apply {
        interests.forEach { interest -> put(interest, savedInterests.contains(interest)) }
    } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Белый фон
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Неизменяемые плашки
            item {
                InfoCard(label = "Группа", value = user_changebale_data.group.toString())
            }
            item {
                InfoCard(label = "Курс", value = user_changebale_data.course.toString())
            }
            item {
                InfoCard(label = "Средний балл", value = user_changebale_data.averageGrade.toString())
            }

            // Список целей
            item {
                SectionWithClickableGrid(
                    title = "Цели",
                    items = goals,
                    selectedItems = selectedGoals
                )
            }

            // Список интересов
            item {
                SectionWithClickableGrid(
                    title = "Интересы",
                    items = interests,
                    selectedItems = selectedInterests
                )
            }

            // Редактируемые поля профиля
            item {
                TextField(
                    value = user_changebale_data.lastName.toString(),
                    onValueChange = { newValue ->
                        user_changebale_data = user_changebale_data.copy(lastName = newValue) },
                    label = { Text("Фамилия") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                TextField(
                    value = user_changebale_data.firstName.toString(),
                    onValueChange = { newValue ->
                        user_changebale_data = user_changebale_data.copy(firstName = newValue) },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                TextField(
                    value = user_changebale_data.middleName.toString(),
                    onValueChange = { newValue ->
                        user_changebale_data = user_changebale_data.copy(middleName = newValue) },
                    label = { Text("Отчество") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Пол", style = MaterialTheme.typography.bodyMedium)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = user_changebale_data.gender == "true",
                            onClick = { user_changebale_data = user_changebale_data.copy(gender = "true") } // Используем copy
                        )
                        Text("Мужской", modifier = Modifier.padding(start = 8.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = user_changebale_data.gender == "false",
                            onClick = { user_changebale_data = user_changebale_data.copy(gender = "false") } // Используем copy
                        )
                        Text("Женский", modifier = Modifier.padding(start = 8.dp))
                    }
                    if (user_changebale_data.gender == null) {
                        Text(
                            text = "Пожалуйста, выберите пол",
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.Gray
                        )
                    }

                }

            }

            item {
                TextField(
                    value = user_changebale_data.age.toString(),
                    onValueChange = { newValue ->
                        user_changebale_data = user_changebale_data.copy(age = newValue) },
                    label = { Text("Возраст") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                TextField(
                    value = user_changebale_data.height.toString(),
                    onValueChange = { newValue ->
                        user_changebale_data = user_changebale_data.copy(height = newValue) },
                    label = { Text("Рост") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Курю")
                    Checkbox(checked = user_changebale_data.isSmoking!!, onCheckedChange = { isChecked ->
                        user_changebale_data = user_changebale_data.copy(isSmoking = isChecked)
                        sharedPreferencesHelper.saveProfile(user_changebale_data)})
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Пью")
                    Checkbox(checked = user_changebale_data.isDrinking!!, onCheckedChange = { isChecked ->
                        user_changebale_data = user_changebale_data.copy(isDrinking = isChecked)
                        sharedPreferencesHelper.saveProfile(user_changebale_data)})
                }
            }

            item {
                Text("Знак зодиака", style = MaterialTheme.typography.bodyMedium)
                ZodiacSelection(
                    selectedZodiac = user_changebale_data.zodiacSign,
                    onZodiacSelect = { newValue ->
                        user_changebale_data = user_changebale_data.copy(zodiacSign = newValue) },
                )
            }

            item {
                TextField(
                    value = user_changebale_data.sports.toString(),
                    onValueChange = { newValue ->
                        user_changebale_data = user_changebale_data.copy(sports = newValue) },
                    label = { Text("Спорт") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                TextField(
                    value = user_changebale_data.music.toString(),
                    onValueChange = { newValue ->
                        user_changebale_data = user_changebale_data.copy(music = newValue) },
                    label = { Text("Музыка") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                TextField(
                    value = user_changebale_data.aboutMe.toString(),
                    onValueChange = { newValue ->
                        user_changebale_data = user_changebale_data.copy(aboutMe = newValue) },
                    label = { Text("О себе") },
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = {

                        val selectedGoalsArray = selectedGoals.filter { it.value }.keys.toList().toTypedArray()
                        val selectedInterestsArray = selectedInterests.filter { it.value }.keys.toList().toTypedArray()

                        user_changebale_data = user_changebale_data.copy(goals = selectedGoalsArray)
                        user_changebale_data = user_changebale_data.copy(interests = selectedInterestsArray)
                        user_changebale_data = user_changebale_data.copy(UUID = login)

                        sharedPreferencesHelper.saveProfile(user_changebale_data)

                        coroutineScope.launch {
                            try {

                                val response = RetrofitInstance.api.updateUserData(user_changebale_data)

                                if (response.isSuccessful) {
                                    onClose()
                                    Toast.makeText(context, "Данные успешно обновлены!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Ошибка при обновлении данных: ${response.message()}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}
