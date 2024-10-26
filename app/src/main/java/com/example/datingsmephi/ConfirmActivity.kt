package com.example.datingsmephi

import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType

class ConfirmActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var extractedLogin: String? = null // Переменная для хранения логина

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm)

        // Включаем стрелку "Назад" в ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Инициализируем кнопку "Подтвердить"
        val confirmButton: Button = findViewById(R.id.confirmButton)

        confirmButton.setOnClickListener {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            // Очищаем куки перед открытием WebView
            clearCookies()

            // Открытие WebView для авторизации
            webView = WebView(this)
            setContentView(webView)

            // Настраиваем WebView
            webView.settings.javaScriptEnabled = true // Включаем JavaScript
            webView.clearCache(true) // Очищаем кеш

            // Получаем текущий User-Agent по умолчанию
            val defaultUserAgent = webView.settings.userAgentString

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    if (url != null && url.contains("/sessions")) {
                        // Выполняем JavaScript для получения текста, выделенного тегами <b> или <strong>
                        webView.evaluateJavascript(
                            "(function() { " +
                                    "var boldElements = document.getElementsByTagName('b'); " +
                                    "for (var i = 0; i < boldElements.length; i++) { " +
                                    "if (/\\w+\\d+/g.test(boldElements[i].innerText)) { " + // Логин с буквами и цифрами
                                    "return boldElements[i].innerText; } " +
                                    "} " +
                                    "var strongElements = document.getElementsByTagName('strong'); " +
                                    "for (var i = 0; i < strongElements.length; i++) { " +
                                    "if (/\\w+\\d+/g.test(strongElements[i].innerText)) { " + // Логин с буквами и цифрами
                                    "return strongElements[i].innerText; } " +
                                    "} " +
                                    "return ''; })();"
                        ) { loginWithQuotes ->
                            // Убираем кавычки из логина
                            val login = loginWithQuotes.replace("\"", "")

                            if (login.isNotEmpty()) {
                                extractedLogin = login // Сохраняем логин
                                Toast.makeText(
                                    this@ConfirmActivity,
                                    "Логин: $login",
                                    Toast.LENGTH_LONG
                                ).show()

                                // Получаем куки для текущего URL
                                val cookieManager = CookieManager.getInstance()
                                val cookies = cookieManager.getCookie(url)

                                // Отфильтровываем только TGT-токен из всех куки
                                val tgtToken = extractTgtToken(cookies)

                                // Добавляем User-Agent
                                val tokenWithUserAgent = "$tgtToken; User-Agent=$defaultUserAgent"

                                // Создаем JSON-объект с логином, TGT-токеном и User-Agent
                                val jsonObject = JSONObject()
                                jsonObject.put("login", login)  // Сохранение без кавычек
                                jsonObject.put("tgt", tgtToken)
                                jsonObject.put("user_agent", defaultUserAgent)

                                // Выполняем POST-запрос с этим JSON-объектом
                                sendPostRequest(jsonObject)
                            } else {
                                Toast.makeText(
                                    this@ConfirmActivity,
                                    "Ошибка: логин не найден",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false // Продолжаем загружать URL
                }
            }

            // Загружаем страницу авторизации
            webView.loadUrl("https://auth.mephi.ru/login")
        }
    }

    // Функция для очистки куки
    private fun clearCookies() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush() // Применяем изменения
    }

    // Функция для извлечения только TGT-токена из всех куки
    private fun extractTgtToken(cookies: String?): String {
        // Ищем куки, которые содержат tgt=
        if (cookies != null) {
            val tgtPattern = Regex("tgt=([^;]+)") // Паттерн для нахождения tgt токена
            val matchResult = tgtPattern.find(cookies)
            return matchResult?.groupValues?.get(0) ?: "TGT токен не найден"
        }
        return "Куки не найдены"
    }

    // Функция для отправки POST-запроса с JSON-объектом
    private fun sendPostRequest(jsonObject: JSONObject) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = RequestBody.create(mediaType, jsonObject.toString())
        val request = Request.Builder()
            .url("http://10.0.2.2:5002/api/users/registration") // Ваш URL для отправки POST-запроса
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@ConfirmActivity,
                        "Ошибка при отправке данных: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    when (response.code) {
                        201 -> {
                            // Успешно добавлен пользователь, переходим на страницу регистрации и передаем логин
                            val intent =
                                Intent(this@ConfirmActivity, RegistrationActivity::class.java)
                            intent.putExtra("login", extractedLogin) // Передаем логин
                            startActivity(intent)
                        }

                        400 -> {
                            // Пользователь с таким логином уже существует, возвращаемся на страницу подтверждения
                            Toast.makeText(
                                this@ConfirmActivity,
                                "Пользователь с таким логином уже существует",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this@ConfirmActivity, ConfirmActivity::class.java)
                            startActivity(intent)
                        }

                        500 -> {
                            // Ошибка на сервере при извлечении данных, возвращаемся на страницу подтверждения
                            Toast.makeText(
                                this@ConfirmActivity,
                                "Произошла ошибка при извлечении данных",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this@ConfirmActivity, ConfirmActivity::class.java)
                            startActivity(intent)
                        }

                        else -> {
                            Toast.makeText(
                                this@ConfirmActivity,
                                "Неизвестная ошибка: ${response.code}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        })
    }

    // Обработка нажатия на стрелку "Назад" в ActionBar
    override fun onSupportNavigateUp(): Boolean {
        // Всегда переходим на страницу ConfirmActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Закрываем текущую активность
        return true
    }

    // Обработка нажатия на системную кнопку "Назад"
    override fun onBackPressed() {
        super.onBackPressed()
        // Всегда переходим на страницу ConfirmActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Закрываем текущую активность
    }
}
