package com.example.datingsmephi

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MainScreen(navController: NavController) {
    // Получаем контекст
    val context = LocalContext.current

    // Контейнер для верстки с отступами
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Заголовок
        Text(
            text = "Добро пожаловать в \"Познакомься, МИФИ\"",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp) // Отступ от текста до кнопок
        )

        // Кнопка для перехода на страницу логина
        Button(onClick = {
            navController.navigate("login_screen")
        }) {
            Text(text = "Войти")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка для перехода на ConfirmScreen
        Button(onClick = {
            navController.navigate("confirm_screen")
        }) {
            Text("Зарегистрироваться")
        }
    }
}
