package com.example.datingsmephi

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun RegistrationScreen(UserDataForReg: UserDataForRegistration, navController: NavController) {

    var telegramTag by remember { mutableStateOf(UserDataForReg.tag) }
    if (telegramTag == null) telegramTag = ""
    var password by remember { mutableStateOf(UserDataForReg.password) }
    if (password == null) password = ""
    var isPasswordValid by remember { mutableStateOf(true) }
    var isTagValid by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current  // Получаем контекст

    // Проверка на валидность пароля и тега
    val isFormValid = password!!.length >= 8 && telegramTag!!.matches(Regex("^[a-zA-Z0-9]+$"))
    Log.d("Reg", "$UserDataForReg")

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
            text = "Ваш Логин: ${UserDataForReg.login}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Поле для ввода Telegram тега
        OutlinedTextField(
            value = telegramTag.toString(),
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
            value = password.toString(),
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

                    val coroutineScope = CoroutineScope(Dispatchers.IO)
                    UserDataForReg.password = password
                    UserDataForReg.tag = telegramTag
                    Log.d("Reg", "$UserDataForReg")
                    coroutineScope.launch {
                        try {
                            val response =
                                RetrofitInstance.api.postUserData(UserDataForReg)

                            if (response.isSuccessful) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Данные успешно добавлены!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("main_screen") {
                                        popUpTo("registration_screen") { inclusive = true }
                                        navController.popBackStack()
                                    }
                                    isLoading = false
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Ошибка при добавлении данных: ${response.message()}", Toast.LENGTH_LONG).show()
                                    isLoading = false
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Ошибка(данные не добавлены): ${e.message}", Toast.LENGTH_LONG).show()
                                isLoading = false
                            }
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