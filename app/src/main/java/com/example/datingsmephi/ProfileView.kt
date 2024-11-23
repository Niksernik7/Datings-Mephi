package com.example.datingsmephi

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileView(
    login: String?,
    context: Context,
    onClose: () -> Unit) {

    var isHere by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Кнопка "Назад" (стрелочка влево)
        IconButton(
            onClick = { onClose() }, // Возврат на предыдущий экран
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack, // Стандартная стрелка из Material Icons
                contentDescription = "Back",
                tint = Color.Black // Цвет стрелки
            )
        }

        if (!isHere) {
            ProfileScreen(login, context)
        }

        // Текст на экране
        Text(
            text = "Экран ProfileView",
            modifier = Modifier.align(Alignment.Center),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
