package com.example.datingsmephi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.google.accompanist.flowlayout.FlowRow

@Composable
fun SportMusicCard(sportOrMusic: String, theme: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth() // Подстроить ширину под родительский контейнер
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 5.dp
            ) // Отступы внутри карточки
        ) {
            // Основной текст
            Text(
                text = theme,
                modifier = Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 3.dp
                ), // Отступ снизу от заголовка
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                textAlign = TextAlign.Left
            )
            Text(
                text = sportOrMusic, // Используем значение цели для текста
                modifier = Modifier
                    .padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ) // Добавляем внутренние отступы для текста
                    .wrapContentSize(Alignment.Center), // Центрируем текст
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
    }
}




@Preview(showBackground = true)
@Composable
fun SportMusicCardPreview() {
    val context = LocalContext.current
    val sph = SharedPreferencesHelper(context)
    val userData = sph.loadProfile()
    SportMusicCard(
        "Я самый веселый человек на свете", "Музыка")
}