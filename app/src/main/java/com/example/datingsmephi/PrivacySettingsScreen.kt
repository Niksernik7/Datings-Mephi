package com.example.datingsmephi

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun PrivacySettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val sph = remember { SharedPreferencesHelper(context) }
    // Загружаем данные, если они есть
    val profileData = sph.loadProfile()

    var userData by remember { mutableStateOf(profileData) }

    var hiddenFields by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }

    // Инициализация состояния скрытых полей
    LaunchedEffect(userData) {
        hiddenFields = mutableMapOf(
            "Группа" to (userData.groupHidden ?: false),
            "Средний балл" to (userData.averageGradeHidden ?: false),
            "Фамилия" to (userData.lastNameHidden ?: false),
            "Отчество" to (userData.middleNameHidden ?: false),
            "Возраст" to (userData.ageHidden ?: false),
            "Рост" to (userData.heightHidden ?: false),
            "Курение" to (userData.isSmokingHidden ?: false),
            "Употребление алкоголя" to (userData.isDrinkingHidden ?: false),
            "Знак зодиака" to (userData.zodiacSignHidden ?: false),
            "Любимые виды спорта" to (userData.sportsHidden ?: false),
            "Любимая музыка" to (userData.musicHidden ?: false),
        )
    }

    val scrollState = rememberScrollState() // Запоминаем состояние прокрутки

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Устанавливаем белый фон
            .verticalScroll(scrollState) // Добавляем прокрутку
            .padding(16.dp)
    ) {
        Text(
            text = "Поставьте галочку, чтобы скрыть от пользователей поле",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        hiddenFields.keys.forEach { fieldName ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = hiddenFields[fieldName] == true,
                    onCheckedChange = { isChecked ->
                        hiddenFields = hiddenFields.toMutableMap().apply {
                            this[fieldName] = isChecked
                        }
                    }
                )
                Text(
                    text = fieldName,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                userData = userData.copy(
                    groupHidden = hiddenFields["Группа"],
                    averageGradeHidden = hiddenFields["Средний балл"],
                    lastNameHidden = hiddenFields["Фамилия"],
                    middleNameHidden = hiddenFields["Отчество"],
                    ageHidden = hiddenFields["Возраст"],
                    heightHidden = hiddenFields["Рост"],
                    isSmokingHidden = hiddenFields["Курение"],
                    isDrinkingHidden = hiddenFields["Употребление алкоголя"],
                    zodiacSignHidden = hiddenFields["Знак зодиака"],
                    sportsHidden = hiddenFields["Любимые виды спорта"],
                    musicHidden = hiddenFields["Любимая музыка"]
                )
                sph.saveProfile(userData, context)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }
    }
}

