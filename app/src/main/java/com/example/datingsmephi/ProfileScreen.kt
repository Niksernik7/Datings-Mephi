package com.example.datingsmephi

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage


@Composable
fun ProfileScreen(
    login: String?,
    context: Context,
    viewModel: ImageSelectionViewModel = viewModel(),
) {
    var isChecking by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var isImageScreenVisible by remember { mutableStateOf(false) }

    // Получаем актуальные изображения из ViewModel
    val imageUris by viewModel.imageUris.collectAsState()

    // Находим первое ненулевое изображение
    val profileImageUri = imageUris.firstOrNull { it != Uri.EMPTY } ?: Uri.EMPTY

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isChecking) {
            Text(
                text = "Профиль",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )

            // Кнопка "Посмотреть профиль"
            Button(
                onClick = { isChecking = true }, // Открываем новый экран
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 210.dp) // Размещаем над картинкой
            ) {
                Text("Посмотреть профиль")
            }

            // Картинка пользователя (кружок)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable { isImageScreenVisible = true } // Делаем круг кликабельным
            ) {
                // Если есть изображение, показываем его, если нет - показываем дефолтное
                if (profileImageUri != Uri.EMPTY) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "User Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_icon),
                        contentDescription = "User Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Кнопка для редактирования
            Button(
                onClick = { isEditing = true },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 200.dp)
            ) {
                Text("Редактировать")
            }
        }

        // Показываем экран редактирования, если isEditing = true
        if (isEditing) {
            EditProfileScreen(
                onClose = { isEditing = false },
                LocalContext.current,
                login,
            )
        }
        // Показываем экран редактирования, если isChecking = true
        if (isChecking) {
            ProfileView(
                login,
                context,
                onClose = { isChecking = false })
        }

        // Показываем экран выбора изображений, если isImageScreenVisible = true
        if (isImageScreenVisible) {
            FullScreenImageSelection(
                onClose = { isImageScreenVisible = false },
                context,
                login
            )
        }
    }
}

