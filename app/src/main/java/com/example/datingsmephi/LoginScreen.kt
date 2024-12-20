package com.example.datingsmephi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, onClose: () -> Unit, onBackClick: () -> Unit) {
    var login by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisibility by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
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
                            sendLoginRequest(context, login.text, password.text) { success ->
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


@SuppressLint("SuspiciousIndentation")
private fun sendLoginRequest(
    context: Context,
    login: String,
    password: String,
    onResponse: (Boolean) -> Unit
) {
    val userData = UserDataForRegistration(null, null, login, null, null, null, null, password)
    val api = RetrofitInstance.api
    val sharedPreferences = SharedPreferencesHelper(context)
    var UUID: String? = null
    val beforeUUID = sharedPreferences.getUUID(context)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = api.loginUser(userData)
            val response1 = response.body()
            Log.e("UUID", "$beforeUUID")
            Log.e("UUID", "${response1?.UUID}")
            if (UUID != response1?.UUID) {
                sharedPreferences.saveFilters(context, UserFilters(1, 9, true, true))
            }

                when (response.code()) {
                    201 -> {
                        // Хэши паролей сошлись
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Успешный вход!", Toast.LENGTH_LONG).show()
                            onResponse(true)
                            response1?.let {
                                sharedPreferences.saveTokens(context, it.accessToken.toString(),
                                    it.refreshToken.toString()
                                )
                                UUID = response1.UUID
                                sharedPreferences.saveUUID(context, UUID)
                                Log.d("UUID", "$UUID")// Получаем переданный логин

                                Log.d("Tokens", "Access Token: ${response1.accessToken}")
                                Log.d("Tokens", "Refresh Token: ${response1.refreshToken}")
                            }
                            // Переход на WelcomeActivity после успешного логина
                            val intent = Intent(context, AppActivity::class.java).apply {
                                putExtra("uuid", UUID)
                            }
                            context.startActivity(intent)
                            (context as? Activity)?.finish()
                        }
                    }
                    400 -> {
                        // Хэши паролей не сошлись
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Хэши паролей не сошлись", Toast.LENGTH_LONG)
                                .show()
                            onResponse(false)
                        }
                    }
                    500 -> {
                        // Ошибка на стороне сервера
                        withContext(Dispatchers.Main) {
                            val errorMessage =
                                response.errorBody()?.string() ?: "Неизвестная ошибка сервера"
                            Toast.makeText(
                                context,
                                "Ошибка на стороне сервера: $errorMessage",
                                Toast.LENGTH_LONG
                            ).show()
                            onResponse(false)
                        }
                    }
                    else -> {
                        // Другая ошибка
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Ошибка: ${response.message()}",
                                Toast.LENGTH_LONG
                            )
                                .show()
                            onResponse(false)
                        }
                    }
                }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Ошибка при отправке данных: ${e.message}", Toast.LENGTH_LONG)
                    .show()
                onResponse(false)
            }
        }
    }
}
