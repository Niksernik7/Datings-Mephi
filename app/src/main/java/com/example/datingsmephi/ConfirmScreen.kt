package com.example.datingsmephi

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.internal.userAgent
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ConfirmScreen(navController: NavController, onBackClick: () -> Unit) {
    var isWebViewVisible by remember { mutableStateOf(false) }
    var login by remember { mutableStateOf("") }
    var cookies by remember { mutableStateOf("") }
    var UserDataForReg = UserDataForRegistration(
        accessToken = null,
        refreshToken = null,
        login = login,
        tgt = null,
        userAgent = null,
        UUID = null,
        tag = null,
        password = null
    )



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
                                                        UserDataForReg.login = login
                                                        UserDataForReg.tgt = tgtToken
                                                        UserDataForReg.userAgent = defaultUserAgent
                                                        UserDataForReg.tag = ""
                                                        UserDataForReg.password = ""
                                                        UserDataForReg.UUID = ""
                                                        sendPostRequest(
                                                            context,
                                                            UserDataForReg
                                                        ) { message, isSuccess, response ->
                                                            if (isSuccess) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Данные успешно добавлены!",
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                // Навигация к RegistrationScreen
                                                                response?.login = login
                                                                val json =
                                                                    kotlinx.serialization.json.Json.encodeToString(
                                                                        response
                                                                    )
                                                                val encodedJson =
                                                                    URLEncoder.encode(json, "UTF-8")
                                                                navController.navigate("registration_screen/$encodedJson") {
                                                                    popUpTo("confirm_screen") {
                                                                        inclusive = true
                                                                    }
                                                                }
                                                            } else {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Произошла ошибка!!!!!!!!",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                                navController.navigate("main_screen") {
                                                                    popUpTo("confirm_screen") { inclusive = true }
                                                                }
                                                            }
                                                        }
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

fun extractTgtToken(cookies: String?): String {
    return cookies?.let {
        val tgtPattern = Regex("tgt=([^;]+)")
        val matchResult = tgtPattern.find(it)
        matchResult?.groupValues?.get(1) ?: "TGT токен не найден"
    } ?: "Куки не найдены"
}

@SuppressLint("SuspiciousIndentation")
fun sendPostRequest(
    context: Context,
    userDataForRegistration: UserDataForRegistration,
    onResponse: (String, Boolean, UserDataForRegistration?) -> Unit
) {
    // Используем CoroutineScope, чтобы работать с корутинами
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    coroutineScope.launch {
        try {
            val response = RetrofitInstance.api.registerUser(userDataForRegistration)
            val response1 = response.body()

                when (response.code()) {
                    201 -> {
                        withContext(Dispatchers.Main) {
                            onResponse("Успех!", true, response1)
                            Toast.makeText(context, "Успех!", Toast.LENGTH_LONG).show()
                        }
                    }
                    400 -> {
                        withContext(Dispatchers.Main) {
                            onResponse("Провал", false, response1)
                            Toast.makeText(
                                context,
                                "Пользователь с таким логином уже существует",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    203 -> {
                        withContext(Dispatchers.Main) {
                            onResponse("Нужно закончить", true, response1)
                            Toast.makeText(
                                context,
                                "Закончите регистрацию!!!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    500 -> {
                        withContext(Dispatchers.Main) {
                            onResponse("Провал", false, response1)
                            Toast.makeText(
                                context,
                                "Ошибка сервера 500: Попробуйте позже",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    else -> {
                        withContext(Dispatchers.Main) {
                            onResponse("Провал", false, response1)
                            Toast.makeText(context, "Ошибка: ${response.code()}", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResponse("Ошибка: ${e.message}", false, null)
                Toast.makeText(context, " Post: Ошибка при отправке данных: ${e.message}", Toast.LENGTH_LONG).show()
                Log.d("Ошибка", "${e.message}")
            }
        }
    }
}

