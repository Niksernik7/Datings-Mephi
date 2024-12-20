package com.example.datingsmephi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.*



@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowProfile(navController: NavController) {
    val context = LocalContext.current
    val sph = SharedPreferencesHelper(context)
    val showProfile = true

    val userData = sph.loadProfile()
    val userPhotos = sph.getUserImages()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Анкета",
                        modifier = Modifier.fillMaxSize(), // Легкий сдвиг вниз
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    ) // Корректный стиль
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.Black
                        )
                    }
                },
                actions = { // Добавляем действия справа
                    IconButton(onClick = {
                        navController.navigate("edit_profile_screen") // Переход на экран редактирования
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit, // Иконка редактирования
                            contentDescription = "Редактировать",
                            tint = Color.Black
                        )
                    }
                },
                modifier = Modifier.height(48.dp), // Установка высоты
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.LightGray)
            )
        },
        content = { paddingValues ->
            ProfileView(paddingValues, userData, userPhotos, showProfile)
        }
    )
}



@Preview(showBackground = true)
@Composable
fun ProfileViewPreview() {
    ShowProfile(
        navController = NavController(context = LocalContext.current)
    )
}




