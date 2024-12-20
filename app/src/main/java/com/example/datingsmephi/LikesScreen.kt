package com.example.datingsmephi

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


suspend fun sendGetListsRequest(
    context: Context,
    status: Int,
    onResponse: (Result<Forms>) -> Unit
) {
    val sph = SharedPreferencesHelper(context)
    val (accessToken, _) = sph.getTokens(context)
    val userId = sph.getUUID(context)
    val UUID = userId ?: ""
    Log.e("SURVEY", "OPENED")
    try {

        val response = RetrofitInstance.api.getLists("Bearer $accessToken", UUID, status)

        if (response.isSuccessful) {
            val forms = response.body()
            if (forms != null) {
                onResponse(Result.success(forms))
            } else {
                // Если forms равно null, можно передать ошибку
                onResponse(Result.failure(Exception("Ответ от сервера пустой")))
            }
        } else {
            if (response.code() == 401) {
                refreshAccessToken(context)
                sendGetFormsRequest(context, onResponse)
            }
        }
    } catch (e: Exception) {
        onResponse(Result.failure(e))
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikesScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedButton = remember { mutableStateOf(0) }
    var isPanelVisible = remember { mutableStateOf(false) } // Состояние панели
    var isGot = remember { mutableStateOf(false) }

    var listDatas: MutableState<List<UserData>> = remember { mutableStateOf(emptyList()) }
    var listPhotos: MutableState<List<UserImagesPaths>> = remember { mutableStateOf(emptyList()) }
    var readyToLoad = remember { mutableStateOf(false)}

    LaunchedEffect(Unit) {
        sendGetListsRequest(context, 3) { result ->
            when {
                result.isSuccess -> {
                    val forms = result.getOrNull() ?: Forms(emptyArray(), emptyArray())
                    listDatas.value = forms.usersData.toList()
                    listPhotos.value = forms.usersPhoto.toList()
                }
            }
        }
        readyToLoad.value = true
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Активность",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedButton.value = 0
                            isPanelVisible.value = false
                            readyToLoad.value = false

                            CoroutineScope(Dispatchers.IO).launch {
                                sendGetListsRequest(context, 3) { result ->
                                    when {
                                        result.isSuccess -> {
                                            val forms = result.getOrNull() ?: Forms(emptyArray(), emptyArray())
                                            listDatas.value = forms.usersData.toList()
                                            listPhotos.value = forms.usersPhoto.toList()
                                        }
                                    }
                                }
                                readyToLoad.value = true
                            }
                        },//нас лайкнули
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (selectedButton.value == 0) Color.Red else Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Red Heart"
                        )
                    }
                    IconButton(
                        onClick = {
                            selectedButton.value = 1
                            isPanelVisible.value = false
                            readyToLoad.value = false

                            CoroutineScope(Dispatchers.IO).launch {
                                sendGetListsRequest(context, 2) { result ->
                                    when {
                                        result.isSuccess -> {
                                            val forms = result.getOrNull() ?: Forms(emptyArray(), emptyArray())
                                            listDatas.value = forms.usersData.toList()
                                            listPhotos.value = forms.usersPhoto.toList()
                                        }
                                    }
                                }
                                readyToLoad.value = true
                            }
                        },//взаимные лайки
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (selectedButton.value == 1) Color.Red else Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Black Heart"
                        )
                    }
                    IconButton(
                        onClick = {
                            selectedButton.value = 2
                            isPanelVisible.value = false
                            readyToLoad.value = false

                            CoroutineScope(Dispatchers.IO).launch {
                                sendGetListsRequest(context, 1) { result ->
                                    when {
                                        result.isSuccess -> {
                                            val forms = result.getOrNull() ?: Forms(emptyArray(), emptyArray())
                                            listDatas.value = forms.usersData.toList()
                                            listPhotos.value = forms.usersPhoto.toList()
                                        }
                                    }
                                }
                                readyToLoad.value = true
                            }
                        }, //мы лайкнули
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (selectedButton.value == 2) Color.Red else Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "White Heart"
                        )
                    }
                    IconButton(
                        onClick = {
                            selectedButton.value = 3
                            isPanelVisible.value = false
                            readyToLoad.value = false

                            CoroutineScope(Dispatchers.IO).launch {
                                sendGetListsRequest(context, 0) { result ->
                                    when {
                                        result.isSuccess -> {
                                            val forms = result.getOrNull() ?: Forms(emptyArray(), emptyArray())
                                            listDatas.value = forms.usersData.toList()
                                            listPhotos.value = forms.usersPhoto.toList()
                                        }
                                    }
                                }
                                readyToLoad.value = true
                            }
                        },//мы дизлайкнули
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (selectedButton.value == 3) Color.Red else Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Dislike"
                        )
                    }
                },
                modifier = Modifier.height(48.dp), // Установка высоты
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.LightGray)
            )
        },
        content = { paddingValues ->
            // Отображение контента в зависимости от выбранной кнопки
            Box(modifier = Modifier.padding(paddingValues)) {
                if (readyToLoad.value) {
                    if (listDatas.value.isNullOrEmpty()) {
                        Text(
                            text = "Пусто",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    } else {
                        ListForms(selectedButton.value, isPanelVisible, listDatas, listPhotos)
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    )
}
