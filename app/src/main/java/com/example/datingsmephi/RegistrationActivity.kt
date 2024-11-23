package com.example.datingsmephi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


class RegistrationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val login = intent.getStringExtra("login") ?: "Unknown"  // Получаем логин из Intent
        setContent {
            RegistrationScreen(login = login)
        }
    }

    // Функция для отправки POST-запроса
    private fun sendRegistrationRequest(
        tag: String,
        login: String,
        password: String,
        context: Context,
        onResponse: (Boolean) -> Unit
    ) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        // Создание JSON-объекта
        val jsonObject = JSONObject().apply {
            put("tag", tag)
            put("login", login)
            put("password", password)
        }

        // Тело запроса
        val body = RequestBody.create(mediaType, jsonObject.toString())
        val request = Request.Builder()
            .url("http://10.0.2.2:5002/api/users/tag") // Ваш серверный URL
            .post(body)
            .build()

        // Выполнение запроса в отдельном потоке
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        context,
                        "Ошибка при отправке данных: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                onResponse(false)
            }

            override fun onResponse(call: Call, response: Response) {
                onResponse(response.isSuccessful)
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(context, "Регистрация успешна!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            context,
                            "Ошибка при регистрации: ${response.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }


    @Composable
    fun RegistrationScreen(login: String) {
        var telegramTag by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isPasswordValid by remember { mutableStateOf(true) }
        var isTagValid by remember { mutableStateOf(true) }
        var isLoading by remember { mutableStateOf(false) }
        val context = LocalContext.current  // Получаем контекст

        // Проверка на валидность пароля и тега
        val isFormValid = password.length >= 8 && telegramTag.matches(Regex("^[a-zA-Z0-9]+$"))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ваш логин такой же как на home.mephi.ru!",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Ваш Логин: $login",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Blue,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Поле для ввода Telegram тега
            OutlinedTextField(
                value = telegramTag,
                onValueChange = {
                    telegramTag = it
                    isTagValid =
                        it.matches(Regex("^[a-zA-Z0-9]+$"))  // Проверка на латинские буквы и цифры
                },
                label = { Text("Telegram Tag(без @)") },
                isError = !isTagValid,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )
            if (!isTagValid) {
                Text(
                    text = "Тег может содержать только латинские буквы и цифры",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }

            // Поле для ввода пароля
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    isPasswordValid = it.length >= 8
                },
                label = { Text("Пароль") },
                isError = !isPasswordValid,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation()
            )
            if (!isPasswordValid) {
                Text(
                    text = "Пароль должен содержать не менее 8 символов",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }

            // Кнопка регистрации
            Button(
                onClick = {
                    if (isFormValid) {
                        isLoading = true
                        sendRegistrationRequest(telegramTag, login, password, context) { success ->
                            isLoading = false
                            if (success) {
                                // Переход на экран входа после успешной регистрации
                                val intent = Intent(context, MainActivity::class.java)
                                context.startActivity(intent)
                            }
                        }
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Зарегистрироваться")
                }
            }
        }
    }
}

