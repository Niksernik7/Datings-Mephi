package com.example.datingsmephi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType

class LoginActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Включаем кнопку "Назад" в ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val loginEditText: EditText = findViewById(R.id.editTextLogin)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val loginButton: Button = findViewById(R.id.buttonLogin)

        loginButton.setOnClickListener {
            val login = loginEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (login.isNotEmpty() && password.isNotEmpty()) {
                // Выполняем POST-запрос
                sendLoginRequest(login, password)
            } else {
                Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Функция для отправки POST-запроса с логином и паролем
    private fun sendLoginRequest(login: String, password: String) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()

        // Создание JSON-объекта с логином и паролем
        val jsonObject = JSONObject()
        jsonObject.put("login", login)
        jsonObject.put("password", password)

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
                    Toast.makeText(this@LoginActivity, "Ошибка при отправке данных: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    when (response.code) {
                        201 -> {
                            // Хэши паролей сошлись
                            Toast.makeText(this@LoginActivity, "Хэши паролей сошлись!", Toast.LENGTH_LONG).show()

                            // Переход на WelcomeActivity после успешного логина
                            val intent = Intent(this@LoginActivity, WelcomeActivity::class.java)
                            startActivity(intent)
                            finish() // Закрываем текущую активность
                        }
                        400 -> {
                            // Хэши паролей не сошлись
                            Toast.makeText(this@LoginActivity, "Хэши паролей не сошлись", Toast.LENGTH_LONG).show()
                        }
                        500 -> {
                            // Ошибка на стороне сервера
                            val errorMessage = response.body?.string() ?: "Неизвестная ошибка сервера"
                            Toast.makeText(this@LoginActivity, "Ошибка на стороне сервера: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            // Другая ошибка
                            Toast.makeText(this@LoginActivity, "Ошибка: ${response.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }

    // Обработка нажатия на кнопку "Назад"
    override fun onSupportNavigateUp(): Boolean {
        finish() // Закрываем текущую активность и возвращаемся назад
        return true
    }
}
