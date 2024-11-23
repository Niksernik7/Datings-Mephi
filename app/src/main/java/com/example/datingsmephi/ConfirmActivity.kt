package com.example.datingsmephi

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.ui.viewinterop.AndroidView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.ArrowBack
import android.util.Log
import android.content.Intent
import android.content.Context

class ConfirmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConfirmScreen(
                onBackClick = {
                    onBackPressed()  // Возврат к предыдущему экрану
                }
            )
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun ConfirmScreen(onBackClick: () -> Unit) {
        var isWebViewVisible by remember { mutableStateOf(false) }
        var login by remember { mutableStateOf("") }
        var cookies by remember { mutableStateOf("") }
        var showToast by remember { mutableStateOf("") }

        val context = LocalContext.current // Получаем контекст активности через Compose

        if (showToast.isNotEmpty()) {
            Toast.makeText(context, showToast, Toast.LENGTH_SHORT).show()
            showToast = ""
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush() // Применяем изменения

        

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Подтверждение регистрации") },
                    navigationIcon = {
                        IconButton(onClick = { onBackClick() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),  // Основной контейнер с padding
                    contentAlignment = Alignment.Center // Центрируем элементы внутри Box
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Подтвердите, что вы мифист",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        if (!isWebViewVisible) {
                            Button(
                                onClick = { isWebViewVisible = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text(text = "Подтвердить")
                            }
                        } else {
                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        settings.javaScriptEnabled = true
                                        clearCache(true)
                                        val defaultUserAgent = settings.userAgentString
                                        webViewClient = object : WebViewClient() {
                                            override fun onPageFinished(
                                                view: WebView?,
                                                url: String?
                                            ) {
                                                super.onPageFinished(view, url)
                                                if (url != null && url.contains("/sessions")) {
                                                    evaluateJavascript(
                                                        "(function() { " +
                                                                "var boldElements = document.getElementsByTagName('b'); " +
                                                                "for (var i = 0; i < boldElements.length; i++) { " +
                                                                "if (/\\w+\\d+/g.test(boldElements[i].innerText)) { " +
                                                                "return boldElements[i].innerText; } " +
                                                                "} " +
                                                                "var strongElements = document.getElementsByTagName('strong'); " +
                                                                "for (var i = 0; i < strongElements.length; i++) { " +
                                                                "if (/\\w+\\d+/g.test(strongElements[i].innerText)) { " +
                                                                "return strongElements[i].innerText; } " +
                                                                "} " +
                                                                "return ''; })();"
                                                    ) { loginResult ->
                                                        val extractedLogin =
                                                            loginResult.replace("\"", "")
                                                        if (extractedLogin.isNotEmpty()) {
                                                            login = extractedLogin
                                                            cookies = CookieManager.getInstance()
                                                                .getCookie(url) ?: ""
                                                            val tgtToken = extractTgtToken(cookies)

                                                            // Передаем контекст активности

                                                            sendPostRequest(context, login, tgtToken, defaultUserAgent) { responseMessage, isSuccess ->
                                                                if (isSuccess) {
                                                                    // Переход в RegistrationActivity
                                                                    val intent = Intent(
                                                                        context,
                                                                        RegistrationActivity::class.java
                                                                    ).apply {
                                                                        putExtra(
                                                                            "login",
                                                                            login
                                                                        ) // логин - это переменная, которую вы получаете
                                                                    }
                                                                    context.startActivity(intent)
                                                                } else {
                                                                    // Переход в MainActivity
                                                                    val intent = Intent(
                                                                        context,
                                                                        MainActivity::class.java
                                                                    )
                                                                    context.startActivity(intent)
                                                                }
                                                            }


                                                            /*

                                                        val intent = Intent(
                                                            context,
                                                            RegistrationActivity::class.java
                                                        ).apply {
                                                            putExtra(
                                                                "login",
                                                                login
                                                            )  // Передаем логин через Intent
                                                        }
                                                        context.startActivity(intent)

                                                             */

                                                        } else {
                                                            showToast = "Логин не найден"
                                                        }
                                                    }
                                                }
                                            }

                                            override fun shouldOverrideUrlLoading(
                                                view: WebView?,
                                                request: WebResourceRequest?
                                            ): Boolean {
                                                return false
                                            }
                                        }
                                        loadUrl("https://auth.mephi.ru/login")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        )
    }


    private fun extractTgtToken(cookies: String?): String {
        return cookies?.let {
            val tgtPattern = Regex("tgt=([^;]+)")
            val matchResult = tgtPattern.find(it)
            matchResult?.groupValues?.get(1) ?: "TGT токен не найден"
        } ?: "Куки не найдены"
    }

    private fun sendPostRequest(
        context: Context,
        login: String,
        tgtToken: String,
        defaultUserAgent: String,
        onResponse: (String, Boolean) -> Unit
    ) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()


        val jsonObject = JSONObject()
        jsonObject.put("login", login)  // Сохранение без кавычек
        jsonObject.put("tgt", tgtToken)
        jsonObject.put("user_agent", defaultUserAgent)

        val body = RequestBody.create(mediaType, jsonObject.toString())
        val request = Request.Builder()
            .url("http://10.0.2.2:5002/api/users/registration")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResponse("Ошибка при отправке данных: ${e.message}", false)
            }

            override fun onResponse(call: Call, response: Response) {
                val message = when (response.code) {
                    201 -> {
                        onResponse("Успех!", true)  // Переход в RegistrationActivity
                        runOnUiThread {
                            Toast.makeText(context, "Успех!", Toast.LENGTH_LONG).show()
                        }
                    }

                    400 -> {
                        onResponse("Провал", false)
                        runOnUiThread {
                            Toast.makeText(context, "Пользователь с таким логином уже существует", Toast.LENGTH_LONG).show()
                        }
                    }

                    500 -> {
                        onResponse("Провал", false)
                        runOnUiThread {
                            Toast.makeText(context, "Ошибка сервера 500: Попробуйте позже", Toast.LENGTH_LONG).show()
                        }
                    }

                    else -> {
                        onResponse("Провал", false)
                        runOnUiThread {
                            Toast.makeText(context, "Ошибка: ${response.code}", Toast.LENGTH_LONG).show()
                        }
                    }
                } // Переход в MainActivity
            }
        })
    }
}



