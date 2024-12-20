package com.example.datingsmephi

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffectScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.*
import kotlin.math.roundToInt
import kotlin.time.measureTime


suspend fun fetchAllImages(
    UUID: String?,
    userImagesPaths: List<String>,
    context: Context
): List<Bitmap?> {
    return withContext(Dispatchers.IO) {
        // Запускаем асинхронные вызовы для каждой фотографии
        userImagesPaths.map { photoName ->
            async {
                fetchImagesFromServer(UUID, photoName, context)
            }
        }.awaitAll() // Ожидаем завершения всех задач
    }
}


@Composable
fun FormView(
    userData: UserData,
    currentImageIndex: MutableState<Int>,
    placeholderImages: List<Bitmap>,
    showProfile: Boolean,
) {
    val context = LocalContext.current
    val userRepository = UserRepository(context)
    val offsetX = remember { Animatable(0f) }
    val cardWidth = LocalDensity.current.run { 300.dp.toPx() } // Ширина карточки в пикселях
    val isDragging =
        remember { mutableStateOf(false) } // Состояние, отслеживающее активность перетаскивания
    val isCardVisible = remember { mutableStateOf(true) } // Состояние для отображения карточки
    val scope = rememberCoroutineScope()
    var isPopupVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Плавно возвращаем или свайпим карточку только после окончания жеста
    LaunchedEffect(isDragging.value) {
        if (!isDragging.value) { // Когда палец убран, запускаем анимацию
            if (offsetX.value > cardWidth / 3) {
                offsetX.animateTo(cardWidth) // Плавно уводим за экран вправо
                isCardVisible.value = false // Скрываем карточку после свайпа
                currentImageIndex.value = 0
                CoroutineScope(Dispatchers.IO).launch {
                    val status = 1
                    userRepository.deleteAllByUuid(userData.UUID.toString())
                    sendPostDataForm(context, userData.UUID.toString(), status, null)
                }
                return@LaunchedEffect
            } else if (offsetX.value < -cardWidth / 3) {
                offsetX.animateTo(-cardWidth) // Плавно уводим за экран влево
                isCardVisible.value = false // Скрываем карточку после свайпа
                currentImageIndex.value = 0

                CoroutineScope(Dispatchers.IO).launch {
                    val status = 0
                    userRepository.deleteAllByUuid(userData.UUID.toString())
                    sendPostDataForm(context, userData.UUID.toString(), status, null)
                }
                return@LaunchedEffect
            } else {
                currentImageIndex.value = 0
                offsetX.animateTo(0f) // Возвращаем карточку в исходное положение
            }
        }
    }

    // Если карточка скрыта, то не отображаем ее
    if (isCardVisible.value) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging.value = true // Активируем перетаскивание
                        },
                        onDragEnd = {
                            isDragging.value = false // Завершаем перетаскивание
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            // Обновляем смещение карточки в реальном времени внутри корутины
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount) // Обновление смещения в реальном времени
                            }
                            change.consumeAllChanges() // Останавливаем дальнейшее распространение события
                        }
                    )
                }
                .graphicsLayer {
                    translationX = offsetX.value
                },
            shape = RoundedCornerShape(25.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val currentBitmap = placeholderImages.getOrNull(currentImageIndex.value)
                if (currentBitmap != null) {
                    Image(
                        painter = BitmapPainter(currentBitmap.asImageBitmap()),
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.default_icon),
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Затемнение сверху
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp) // Высота затемнения сверху
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.TopStart)
                )

                // Затемнение снизу
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Высота затемнения снизу
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f)
                                )
                            )
                        )
                        .align(Alignment.BottomStart)
                )

                // Черточки сверху изображения
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp) // Отступ сверху
                        .align(Alignment.TopCenter) // Расположение черточек сверху
                ) {
                    placeholderImages.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(40.dp, 5.dp)
                                .padding(horizontal = 4.dp)
                                .background(
                                    if (index == currentImageIndex.value) Color.White else Color.LightGray.copy(
                                        alpha = 0.3f
                                    ),
                                    RoundedCornerShape(50)
                                )
                        )
                    }
                }

                // Горизонтальное распределение кнопок для смены изображения
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Левая кнопка

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)  // Заполняет левую часть
                            .clickable(
                                indication = null, // Убираем эффект при нажатии
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (currentImageIndex.value > 0) {
                                    currentImageIndex.value -= 1 // Переход на предыдущее изображение
                                }
                            },
                    )

                    // Правая кнопка
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)  // Заполняет правую часть
                            .clickable(
                                indication = null, // Убираем эффект при нажатии
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (currentImageIndex.value < placeholderImages.size - 1) {
                                    currentImageIndex.value += 1 // Переход на следующее изображение
                                }
                            }
                    )
                }

                // Текст на изображении
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                                fontWeight = MaterialTheme.typography.headlineLarge.fontWeight,
                                color = Color.White
                            )
                        ) {
                            append("${userData.firstName}, ${userData.course}\n") // Первая строка (большой текст)
                        }
                        if (userData.aboutMe != null) {

                            withStyle(
                                style = SpanStyle(
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                                    color = Color.White
                                )
                            ) {
                                append("О себе: ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                                    color = Color.White
                                )
                            ) {
                                append(userData.aboutMe)
                            }
                        }
                    },
                    modifier = if (showProfile) {
                        Modifier
                            .align(Alignment.BottomStart) // Выравнивание внизу слева
                            .padding(horizontal = 10.dp, vertical = 16.dp)
                    } else {
                        Modifier
                            .align(Alignment.BottomStart) // Выравнивание внизу слева
                            .padding(horizontal = 10.dp, vertical = 66.dp)
                    },

                    style = MaterialTheme.typography.bodyMedium, // Основной стиль текста (можно игнорировать, т.к. задаётся локально)
                    textAlign = TextAlign.Left,
                    maxLines = 3, // Максимум 3 строки
                    overflow = TextOverflow.Ellipsis // Троеточие для длинного текста
                )

                // Кнопки под текстом
                if (!showProfile) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Кнопка с крестиком
                        Card(
                            onClick = {
                                // Логика свайпа влево при нажатии на кнопку с крестиком
                                scope.launch {
                                    offsetX.animateTo(-cardWidth) // Плавно уводим за экран влево
                                    isCardVisible.value = false // Скрываем карточку после свайпа
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val status = 0
                                        userRepository.deleteAllByUuid(userData.UUID.toString())
                                        sendPostDataForm(context, userData.UUID.toString(), status, null)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .size(56.dp, 40.dp) // Унифицированный размер
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Red) // Если нужны цвета
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

                        // Кнопка с конвертиком и сердечком
                        Card(
                            onClick = { isPopupVisible = true },
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

                        // Кнопка с сердечком (Лайк)
                        Card(
                            onClick = {
                                // Логика свайпа вправо при нажатии на кнопку с сердечком
                                scope.launch {
                                    offsetX.animateTo(cardWidth) // Плавно уводим за экран вправо
                                    isCardVisible.value = false // Скрываем карточку после свайпа
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val status = 1
                                        userRepository.deleteAllByUuid(userData.UUID.toString())
                                        sendPostDataForm(context, userData.UUID.toString(), status, null)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .size(56.dp, 40.dp) // Унифицированный размер
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Green) // Если нужны цвета
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
        if (isPopupVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Заголовок и кнопка закрытия
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Напишите сообщение",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.Black
                            )
                            IconButton(onClick = { isPopupVisible = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Закрыть"
                                )
                            }
                        }

                        // Поле для ввода текста
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("Сообщение") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            maxLines = 1
                        )

                        // Кнопка "Сохранить"
                        Button(
                            onClick = {
                                isPopupVisible = false
                                // Логика свайпа вправо
                                scope.launch {
                                    offsetX.animateTo(cardWidth)
                                    isCardVisible.value = false
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val status = 1
                                        userRepository.deleteAllByUuid(userData.UUID.toString())
                                        sendPostDataForm(context, userData.UUID.toString(), status, message)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = message.isNotBlank()
                        ) {
                            Text("Сохранить")
                        }
                    }
                }
            }
        }
    }
}

suspend fun sendPostDataForm (
    context: Context,
    UUID_form: String,
    status: Int,
    message: String?
) {
    val sph = SharedPreferencesHelper(context)
    var (accessToken, _) = sph.getTokens(context)
    val UUID = sph.getUUID(context)
    val send: Like = Like(UUID.toString(), UUID_form, status, message)

    try {
        val response =
            RetrofitInstance.api.postUserDataForm("Bearer $accessToken", send)

        when (response.code()) {
            200 -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Данные успешно обновлены!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            401 -> {
                refreshAccessToken(context)
                sendPostDataForm(context, UUID_form, status, message)
            }

            500 -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Ошибка при работе сервера 500",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            else -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Ошибка при обновлении данных: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Ошибка: ${e.message}",
                Toast.LENGTH_LONG
            )
                .show()
            Log.e("ERROR", "${e.message}")
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun FormViewPreview() {
//    FormView(1)
//}
