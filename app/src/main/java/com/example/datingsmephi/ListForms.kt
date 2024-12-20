package com.example.datingsmephi

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ListForms(
    selectedButton: Int,
    isPanelVisible: MutableState<Boolean>,
    profiles: MutableState<List<UserData>>,
    photos: MutableState<List<UserImagesPaths>>,
) {
    var currentImageIndex = remember { mutableStateOf(0) } // Текущий индекс изображения
    val context = LocalContext.current
    var profileIndex = remember { mutableStateOf(0) }
    var deleteMode = remember { mutableStateOf(false) }
    var status = remember { mutableStateOf(0) }
    var bitmaps: MutableState<List<Bitmap>> = remember { mutableStateOf(emptyList()) }
    var ready = remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isPopupVisible = remember { mutableStateOf(false) }


    LaunchedEffect(deleteMode.value) {
        if (deleteMode.value) {
            // Выполняем запрос и ждем его завершения
            withContext(Dispatchers.IO) {
                sendPostDataForm(
                    context,
                    profiles.value[profileIndex.value].UUID.toString(),
                    status.value,
                    null
                )
            }

            // Удаляем элемент из списка профилей
            profiles.value = profiles.value.toMutableList().apply {
                removeAt(profileIndex.value)
            }

            // Удаляем элемент из списка фото
            photos.value = photos.value.toMutableList().apply {
                removeAt(profileIndex.value)
            }

            // Сбрасываем deleteMode после завершения операции
            deleteMode.value = false
        }
    }


    if (isPanelVisible.value) {
        val bitmapsState = remember { mutableStateOf<List<Bitmap>>(emptyList()) }

        LaunchedEffect(isPanelVisible.value) {
            if (!photos.value[profileIndex.value].photo_name.isNullOrEmpty()) {
                // Загружаем изображения в фоне
                val Bitmaps = fetchAllImages(
                    profiles.value[profileIndex.value].UUID,
                    photos.value[profileIndex.value].photo_name,
                    context
                )

                // Фильтруем только валидные битмапы (не null)
                val validBitmaps = Bitmaps.filterNotNull()

                // Обновляем состояние с валидными битмапами
                bitmapsState.value = validBitmaps
                bitmaps.value = bitmapsState.value
                Log.e("BTMP", "${profiles.value[profileIndex.value].UUID}")
                ready.value = true
            } else {
                Log.e("ImageFetch", "No images to load")
                bitmaps.value = emptyList()
                ready.value = true
            }
        }
        if (ready.value) {
            FormViewWithPanel(
                profiles.value[profileIndex.value],
                currentImageIndex,
                isPanelVisible,
                bitmaps.value
            )
        } else {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Две колонки
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(profiles.value.size) { index ->
                UserProfileCard(status, deleteMode, profiles.value[index], selectedButton, index, profileIndex, isPanelVisible, photos.value[index], isPopupVisible)
            }
        }
    }
    if (isPopupVisible.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)), // Закрытие по всему экрану убрано
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight() // Контейнер будет расширяться по высоте в зависимости от контента
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Заголовок с кнопкой закрытия
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!(profiles.value[profileIndex.value].messageLiked.isNullOrEmpty()) ||
                            !(profiles.value[profileIndex.value].message.isNullOrEmpty())) {
                            Text(
                                text = "Сообщения",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.Black
                            )
                            IconButton(onClick = { isPopupVisible.value = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Закрыть"
                                )
                            }
                        } else {
                            Text(
                                text = "Сообщений Нет",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.Black
                            )
                            IconButton(onClick = { isPopupVisible.value = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Закрыть"
                                )
                            }
                        }
                    }

                    // Сообщение для Вас
                    if (!(profiles.value[profileIndex.value].messageLiked.isNullOrEmpty())) {
                        Text(
                            text = "Сообщение для Вас:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                        Text(
                            text = profiles.value[profileIndex.value].messageLiked!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            maxLines = Int.MAX_VALUE, // Не ограничиваем количество строк
                            overflow = TextOverflow.Visible // Не обрезаем текст
                        )
                    }
                    // Сообщение от Вас
                    if (!(profiles.value[profileIndex.value].message.isNullOrEmpty())) {
                        Text(
                            text = "Сообщение от Вас:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                        Text(
                            text = profiles.value[profileIndex.value].message!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            maxLines = Int.MAX_VALUE, // Не ограничиваем количество строк
                            overflow = TextOverflow.Visible // Не обрезаем текст
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun UserProfileCard(
    status: MutableState<Int>,
    deleteMode: MutableState<Boolean>,
    userData: UserData,
    selectedButton: Int,
    index: Int,
    profileIndex: MutableState<Int>,
    isPanelVisible: MutableState<Boolean>,
    photos: UserImagesPaths,
    isPopupVisible: MutableState<Boolean>
) {
    val context = LocalContext.current

    Card(
        onClick = {
            isPanelVisible.value = true
            profileIndex.value = index
        },
        modifier = Modifier
            .aspectRatio(0.6f) // Высота больше ширины (150/120 ~ 0.8)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            if (photos.photo_name != null && photos.photo_name.size != 0) {
                val photo = photos.photo_name.get(0)
                var imageBitmap = remember { mutableStateOf<Bitmap?>(null) }
                LaunchedEffect(photo) {
                    imageBitmap.value =
                        fetchImagesFromServer(userData.UUID, photos.photo_name.get(0), context)
                }
                imageBitmap.value?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Server Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: run {
                    // Показать индикатор загрузки или заглушку
                    CircularProgressIndicator()
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_icon),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop, // Обрезка для заполнения
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Кнопки поверх картинки
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                //0-nолько лайк  1-только дизлайк  2-ноль кнопок   3-обе кнопки
                when (selectedButton) {
                    3 -> {
                        // Кнопка "Лайк"
                        Card(
                            onClick = {
                                profileIndex.value = index
                                deleteMode.value = true
                                status.value = 1
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .size(56.dp, 40.dp)
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Green)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Лайк",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    2 -> {
                        // Кнопка "Отказ"
                        Card(
                            onClick = {
                                profileIndex.value = index
                                deleteMode.value = true
                                status.value = 0
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .size(56.dp, 40.dp)
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Red)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Отказ",
                                    tint = Color.White
                                )
                            }
                        }
                        Card(
                            onClick = {
                                isPopupVisible.value = true
                                profileIndex.value = index
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .size(56.dp, 40.dp)
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            // Размер кнопки
                            colors = CardDefaults.cardColors(containerColor = Color.Unspecified) // Если нужны цвета
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_heart_envelope),
                                    contentDescription = "Сообщение",
                                )
                            }
                        }
                    }

                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                                .padding(bottom = 0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Кнопка
                            Card(
                                onClick = {
                                    isPopupVisible.value = true
                                    profileIndex.value = index
                                },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .size(56.dp, 40.dp), // Размер кнопки
                                colors = CardDefaults.cardColors(containerColor = Color.Unspecified) // Если нужны цвета
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_heart_envelope),
                                        contentDescription = "Сообщение",
                                    )
                                }
                            }

                            // Отступ между кнопкой и текстом
                            Spacer(modifier = Modifier.height(2.dp))

                            // Бокс с текстом Telegram
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White) // Убираем фон или оставляем, если нужен
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            style = SpanStyle(
                                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                                fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                                                color = Color.Black
                                            )
                                        ) {
                                            append("TG: ")
                                        }
                                        withStyle(
                                            style = SpanStyle(
                                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                                fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                                                color = Color.Black
                                            )
                                        ) {
                                            append(userData.tg)
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }

                    0 -> {
                        // Кнопки "Лайк" и "Отказ"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Кнопка "Отказ"
                            Card(
                                onClick = {
                                    profileIndex.value = index
                                    deleteMode.value = true
                                    status.value = 0
                                },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .size(56.dp, 40.dp)
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Red)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Отказ",
                                        tint = Color.White
                                    )
                                }
                            }

                            Card(
                                onClick = {
                                    isPopupVisible.value = true
                                    profileIndex.value = index
                                },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .size(56.dp, 40.dp) // Унифицированный размер
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Unspecified) // Если нужны цвета
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_heart_envelope),
                                        contentDescription = "Сообщение",
                                    )
                                }
                            }

                            // Кнопка "Лайк"
                            Card(
                                onClick = {
                                    profileIndex.value = index
                                    deleteMode.value = true
                                    status.value = 1
                                },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .size(56.dp, 40.dp)
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Green)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Лайк",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}


//// Пример вызова
//@Preview(showBackground = true)
//@Composable
//fun PreviewUserProfilesScreen() {
//    val profiles = listOf(
//        UserProfile("Аня", "https://example.com/image1.jpg"),
//        UserProfile("Иван", "https://example.com/image2.jpg"),
//        UserProfile("Мария", "https://example.com/image3.jpg")
//    )
//    UserProfilesScreen(profiles = profiles)
//}