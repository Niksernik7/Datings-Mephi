package com.example.datingsmephi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import android.util.Log

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }

    // Функция для отправки POST-запроса с логином и паролем
    private fun sendLoginRequest(login: String, password: String, onResponse: (Boolean) -> Unit) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()

        // Создание JSON-объекта с логином и паролем
        val jsonObject = JSONObject().apply {
            put("login", login)
            put("password", password)
        }

        // Тело запроса
        val body = RequestBody.create(mediaType, jsonObject.toString())
        val request = Request.Builder()
            .url("http://10.0.2.2:5002/api/users/login") // Замените на ваш реальный URL
            .post(body)
            .build()
        // Выполнение запроса в отдельном потоке
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@LoginActivity,
                        "Ошибка при отправке данных: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                onResponse(false)
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    when (response.code) {
                        201 -> {
                            // Хэши паролей сошлись
                            Toast.makeText(this@LoginActivity, "Успешный вход!", Toast.LENGTH_LONG)
                                .show()
                            onResponse(true)
                            // Переход на WelcomeActivity после успешного логина
                            val intent = Intent(this@LoginActivity, AppActivity::class.java).apply {
                                putExtra("login", login)
                            }
                            startActivity(intent)
                            finish() // Закрываем текущую активность
                        }

                        400 -> {
                            // Хэши паролей не сошлись
                            onResponse(false)
                            Toast.makeText(
                                this@LoginActivity,
                                "Хэши паролей не сошлись",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        500 -> {
                            // Ошибка на стороне сервера
                            onResponse(false)
                            val errorMessage =
                                response.body?.string() ?: "Неизвестная ошибка сервера"
                            Toast.makeText(
                                this@LoginActivity,
                                "Ошибка на стороне сервера: $errorMessage",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        else -> {
                            // Другая ошибка
                            onResponse(false)
                            Toast.makeText(
                                this@LoginActivity,
                                "Ошибка: ${response.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginScreen() {
        var login by remember { mutableStateOf(TextFieldValue()) }
        var password by remember { mutableStateOf(TextFieldValue()) }
        var isLoading by remember { mutableStateOf(false) }
        var passwordVisibility by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Login") },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Вход", fontSize = 24.sp)

                    // Поле ввода логина
                    OutlinedTextField(
                        value = login,
                        onValueChange = { login = it },
                        label = { Text("Логин") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Поле ввода пароля
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(
                                    imageVector = if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Кнопка входа
                    Button(
                        onClick = {
                            if (login.text.isNotEmpty() && password.text.isNotEmpty()) {

                                isLoading = true
                                sendLoginRequest(login.text, password.text) { success ->
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading && login.text.isNotEmpty() && password.text.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Войти")
                        }
                    }
                }
            }
        )
    }
}

