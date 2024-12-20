package com.example.datingsmephi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.positionChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun ProfileView(
    paddingValues: PaddingValues,
    userData: UserData,
    userImagesPaths: UserImagesPaths?,
    showProfile: Boolean,
) {
    val context = LocalContext.current
    val userRepository = UserRepository(context)
    val iterator by userRepository.getUserDataCount().collectAsState(initial = 0)
    val bitmapsState = remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    val loadingState = remember { mutableStateOf(true) } // Состояние загрузки
    var bitmaps: List<Bitmap> = emptyList()

    // Загружаем изображения асинхронно
    LaunchedEffect(userImagesPaths) {
        loadingState.value = true
        if (!userImagesPaths?.photo_name.isNullOrEmpty()) {
            // Загружаем изображения в фоне
            val Bitmaps = fetchAllImages(userData.UUID, userImagesPaths?.photo_name ?: emptyList(), context)

            // Фильтруем только валидные битмапы (не null)
            val validBitmaps = Bitmaps.filterNotNull()

            // Обновляем состояние с валидными битмапами
            bitmapsState.value = validBitmaps
            loadingState.value = false
        } else {
            Log.e("ImageFetch", "No images to load")
            loadingState.value = false
        }
        loadingState.value = false
    }


    bitmaps = bitmapsState.value

    var isPanelVisible = remember { mutableStateOf(false) } // Состояние панели
    val swipeState = remember { mutableStateOf(0f) } // Состояние свайпа
    // Состояние для выезжающей панели
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = isPanelVisible.value
    var currentImageIndex = remember { mutableStateOf(0) } // Текущий индекс изображения

    if (loadingState.value) {
        // Показываем индикатор загрузки
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        swipeState.value += dragAmount // Отслеживаем движение по горизонтали
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            val dx = change.positionChange().x
                            val dy = dragAmount


                            val angle = atan2(abs(dy), abs(dx)) * (180 / PI)
                            if (angle >= 65) {
                                isPanelVisible.value = when {
                                    dy > 0 -> false // Свайп вниз
                                    dy < 0 -> true   // Свайп вверх
                                    else -> isPanelVisible.value
                                }
                            }
                        }
                    }
            ) {
                Crossfade(
                    targetState = isPanelVisible.value,
                    animationSpec = tween(durationMillis = 600) // Длительность в миллисекундах
                ) { panelVisible ->
                    if (!panelVisible) {
                        if (iterator % 2 == 0) {
                            FormView(userData, currentImageIndex, bitmaps, showProfile)

                        } else {
                            FormView(userData, currentImageIndex, bitmaps, showProfile)
                        }
                    } else {
                        if (iterator % 2 == 0) {
                            FormViewWithPanel(
                                userData,
                                currentImageIndex,
                                isPanelVisible = isPanelVisible,
                                bitmaps
                            )
                        } else {
                            FormViewWithPanel(
                                userData,
                                currentImageIndex,
                                isPanelVisible = isPanelVisible,
                                bitmaps
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = !isPanelVisible.value,
                    enter = fadeIn(),
                ) {
                    FormView(userData, currentImageIndex, bitmaps, showProfile)
                }
            }
        }
    }
}