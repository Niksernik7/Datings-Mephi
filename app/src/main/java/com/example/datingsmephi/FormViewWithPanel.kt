package com.example.datingsmephi

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.datingsmephi.InfoRequirementsInterestsCard
import com.example.datingsmephi.SharedPreferencesHelper
import com.example.datingsmephi.SportMusicCard

@Composable
fun FormViewWithPanel(
    userData: UserData,
    currentImageIndex: MutableState<Int>,
    isPanelVisible: MutableState<Boolean>,
    placeholderImages: List<Bitmap>
) {

    var info: Array<String> = emptyArray()
    if (userData.zodiacSignHidden != true && !(userData.zodiacSign.isNullOrEmpty())) info =
        info.plus(userData.zodiacSign!!)
    if (userData.ageHidden != true && !(userData.age.isNullOrEmpty())) info =
        info.plus("Возраст:${userData.age!!}")
    if (userData.heightHidden != true && !(userData.height.isNullOrEmpty())) info =
        info.plus("Рост:${userData.height!!}")
    if (userData.groupHidden != true && !(userData.group.isNullOrEmpty())) info =
        info.plus("Группа:${userData.group!!}")
    if (userData.averageGradeHidden != true && !(userData.averageGrade.isNullOrEmpty())) info =
        info.plus("Средний балл:${userData.averageGrade!!}")
    if (!(userData.tg.isNullOrEmpty())) info =
        info.plus("Telegram:${userData.tg!!}")
    if (userData.isDrinkingHidden != true && (userData.isDrinking != false && userData.isDrinking != null)) info = info.plus("Пью")
    if (userData.isSmokingHidden != true && (userData.isSmoking != false && userData.isSmoking != null)) info = info.plus("Курю")

    // LazyListState для отслеживания прокрутки
    val lazyListState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(bottom = 6.dp)
            .nestedScroll(object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // Проверяем, если прокручено до самого верха
                    if (lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0) {
                        if (available.y < 0) { // Свайп вверх
                            isPanelVisible.value = true
                            Log.e("Panel", "${isPanelVisible.value}")
                        } else if (available.y > 0) { // Свайп вниз
                            isPanelVisible.value = false
                            currentImageIndex.value = 0
                            Log.e("Panel", "${isPanelVisible.value}")
                        }
                    }
                    return Offset.Zero
                }
            }),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
            ) {

                // Изображение
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val currentBitmap = placeholderImages.getOrNull(currentImageIndex.value)
                    if (currentBitmap != null) {
                        Image(
                            painter = BitmapPainter(currentBitmap.asImageBitmap()),
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(550.dp)
                                .clipToBounds()
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.default_icon),
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(550.dp)
                                .clipToBounds()
                        )
                    }
                    // Левая и правая кнопки
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(550.dp) // Совпадает с высотой изображения
                    ) {
                        // Левая кнопка
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    if (currentImageIndex.value > 0) {
                                        currentImageIndex.value -= 1
                                    }
                                }
                        )

                        // Правая кнопка
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    if (currentImageIndex.value < placeholderImages.size - 1) {
                                        currentImageIndex.value += 1
                                    }
                                }
                        )
                    }
                    // Затемнение сверху
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp) // Высота затемнения сверху
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
                                        if (index == currentImageIndex.value) Color.White else Color.Gray.copy(
                                            alpha = 0.3f
                                        ),
                                        RoundedCornerShape(50)
                                    )
                            )
                        }
                    }
                }

                // Текст
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                                    fontWeight = MaterialTheme.typography.headlineLarge.fontWeight,
                                    color = Color.Black
                                )
                            ) {
                                append("${userData.firstName}, ${userData.course}\n") // Первая строка (большой текст)
                            }
                            if (userData.aboutMe != null) {
                                withStyle(
                                    style = SpanStyle(
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                        fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                                        color = Color.Black
                                    )
                                ) {
                                    append("О себе: ")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                                        color = Color.Black
                                    )
                                ) {
                                    append(userData.aboutMe)
                                }
                            }
                        },
                        modifier = Modifier,
                        style = MaterialTheme.typography.bodyMedium, // Основной стиль текста
                        textAlign = TextAlign.Left,
                    )
                }
            }
        }
        item {
            if (!(info.isNullOrEmpty()))
                InfoRequirementsInterestsCard(info, "Информация")
        }
        item {
            if (!(userData.goals.isNullOrEmpty()))
                InfoRequirementsInterestsCard(userData.interests, "Цели")
        }
        item {
            if (!(userData.interests.isNullOrEmpty()))
                InfoRequirementsInterestsCard(userData.goals, "Интересы")
        }
        item {
            if (!(userData.sports.isNullOrEmpty()) && (userData.sportsHidden == false))
                SportMusicCard(userData.sports!!, "Спорт")
        }
        item {
            if (!(userData.music.isNullOrEmpty()) && (userData.musicHidden == false))
                SportMusicCard(userData.music!!, "Музыка")
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun FormViewWithPanelPreview() {
//    var currentImageIndex = remember { mutableStateOf(0) } // Текущий индекс изображения
//    var isPanelVisible = remember { mutableStateOf(false) } // Состояние панели
//
//    FormViewWithPanel(currentImageIndex.value, isPanelVisible)
//}
