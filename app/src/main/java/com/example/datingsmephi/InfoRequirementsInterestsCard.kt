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
fun InfoRequirementsInterestsCard(goalsOrInterests: Array<String>, theme: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth() // Подстроить ширину под родительский контейнер
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp) // Отступы внутри карточки
        ) {
            // Основной текст
            Text(
                text = theme,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), // Отступ снизу от заголовка
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                textAlign = TextAlign.Left
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(), // Заполнение по ширине
                mainAxisSpacing = 0.dp, // Отступ между карточками по горизонтали
                crossAxisSpacing = 0.dp // Отступ между строками
            ) {
                // Перебор списка целей и создание карточек для каждой цели
                goalsOrInterests.forEach { goalOrInterest ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.LightGray
                        ),
                        modifier = Modifier
                            .padding(4.dp) // Отступ между карточками
                            .wrapContentSize() // Автоматический размер по содержимому
                    ) {
                        Text(
                            text = goalOrInterest, // Используем значение цели для текста
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp) // Добавляем внутренние отступы для текста
                                .wrapContentSize(Alignment.Center), // Центрируем текст
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun InfoRequirementsInterestsCardPreview() {
    val context = LocalContext.current
    val sph = SharedPreferencesHelper(context)
    val userData = sph.loadProfile()
    InfoRequirementsInterestsCard(
        arrayOf("list", "goal", "like", "baby", "match", "inte", "hahahahahaha", "hehehehe"), "Цели")
}