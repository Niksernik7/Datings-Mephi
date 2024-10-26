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
import java.security.MessageDigest

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Получаем переданный логин из Intent
        val login = intent.getStringExtra("login")

        // Находим EditText для логина и устанавливаем текст
        val loginEditText: EditText = findViewById(R.id.loginEditText)
        loginEditText.setText(login)

        // Инициализация полей и кнопки
        val telegramTagEditText: EditText = findViewById(R.id.telegramTagEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val registerButton: Button = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            val telegramTag = telegramTagEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Проверка, что поля не пустые и пароль содержит не менее 8 символов
            if (telegramTag.isNotEmpty() && password.length >= 8) {
                // Отправка POST-запроса с данными
                sendRegistrationRequest(telegramTag, login!!, password)
            } else if (password.length < 8) {
                Toast.makeText(this, "Пароль должен содержать не менее 8 символов", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Введите все данные", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Функция для отправки POST-запроса на сервер
    private fun sendRegistrationRequest(tag: String, login: String, password: String) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        // Создание JSON-объекта
        val jsonObject = JSONObject()
        jsonObject.put("tag", tag)
        jsonObject.put("login", login)
        jsonObject.put("password", password)

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
                    Toast.makeText(this@RegistrationActivity, "Ошибка при отправке данных: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegistrationActivity, "Регистрация успешна!", Toast.LENGTH_LONG).show()

                        // Переход на экран входа после успешной регистрации
                        val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Закрываем текущую активность
                    } else {
                        Toast.makeText(this@RegistrationActivity, "Ошибка при регистрации: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
