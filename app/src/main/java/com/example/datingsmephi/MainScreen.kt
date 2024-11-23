package com.example.datingsmephi

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen() {
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
        // Кнопка для перехода на страницу логина
        Button(onClick = {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(text = "Войти")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка для перехода на ConfirmActivity
        Button(onClick = {
            val intent = Intent(context, ConfirmActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(text = "Зарегистрироваться")
        }
    }
}