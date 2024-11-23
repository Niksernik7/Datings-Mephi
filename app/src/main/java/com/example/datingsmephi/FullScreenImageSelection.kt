package com.example.datingsmephi

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.datingsmephi.ImageSelectionViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException


fun getRealPathFromURI(uri: Uri, context: Context): String {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = context.contentResolver.query(uri, projection, null, null, null)
    cursor?.moveToFirst()
    val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    val path = columnIndex?.let { cursor.getString(it) }
    cursor?.close()
    return path ?: ""
}

fun uploadImagesToServerAndSave(images: List<Uri>, context: Context, login: String?) {
    val logging = HttpLoggingInterceptor()
    logging.setLevel(HttpLoggingInterceptor.Level.BODY)

    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val url = "http://10.0.2.2:5002/api/users/post_user_images" // Полный URL сервера
    val mediaType = "application/json; charset=utf-8".toMediaType()

    val multipartBuilder = MultipartBody.Builder()
        .setType(MultipartBody.FORM) // Устанавливаем тип данных для формы

    // Добавляем изображения
    images.forEach { uri ->
        Log.d("Uri", "$uri")
        val file = File(getRealPathFromURI(uri, context))
        Log.d("Message", "$file")
        if (!file.exists()) {
            Log.e("FileError", "File does not exist: ${file.path}")
            return@forEach
        }

        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        multipartBuilder.addFormDataPart("file", file.name, requestBody)
    }

    // Добавляем логин в запрос
    if (login != null) {
        multipartBuilder.addFormDataPart("login", login)
    }

    val requestBody = multipartBuilder.build()

    val request = Request.Builder()
        .url(url) // Указываем полный URL
        .post(requestBody) // Передаем тело запроса
        .build()

    // Выполняем запрос в фоновом потоке
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("RequestError", "Request failed: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            when (response.code) {
                201 -> {

                }

                500 -> {

                }

                else -> {

                }
            }
        }
    })
}


// Сохраняем изображение в галерею
fun saveImageToGallery(imageUri: Uri, context: Context) {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(imageUri)

    if (inputStream != null) {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUriInGallery =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (imageUriInGallery != null) {
            contentResolver.openOutputStream(imageUriInGallery).use { outputStream ->
                inputStream.copyTo(outputStream!!)
            }
        }
    }
}


@Composable
fun FullScreenImageSelection(
    onClose: () -> Unit,
    context: Context,
    login: String?,
    viewModel: ImageSelectionViewModel = viewModel()
) {
    val imageUris by viewModel.imageUris.collectAsState()
    var selectedImageIndex by remember { mutableStateOf(-1) }
    var showDialog by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) } // Режим удаления

    // Функция для открытия диалога
    @Composable
    fun showImageSourceDialog(onSelectGallery: () -> Unit) {
        val options = arrayOf("Выбрать из галереи")

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Выберите источник изображения") },
                text = {
                    Column {
                        options.forEachIndexed { index, option ->
                            Text(
                                text = option,
                                modifier = Modifier
                                    .clickable {
                                        when (index) {
                                            0 -> onSelectGallery() // Галерея
                                        }
                                        showDialog = false // Закрытие диалога
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }

    // Логика добавления изображения в первую пустую ячейку
    fun addImageToFirstEmptySlot(uri: Uri) {
        // Ищем первую пустую ячейку (где Uri.EMPTY)
        val emptyIndex = imageUris.indexOf(Uri.EMPTY)
        if (emptyIndex != -1) {
            viewModel.updateImageUri(emptyIndex, uri) // Обновляем ячейку с первым пустым URI
        } else {
            // Все ячейки заняты, можно уведомить пользователя, если нужно
        }
    }

    // Инициализация лончера для выбора изображения
    val imageSelectionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            addImageToFirstEmptySlot(it) // Используем нашу функцию для добавления изображения в первую пустую ячейку
        }
    }

    // Функция для открытия галереи
    fun openGallery() {
        imageSelectionLauncher.launch("image/*")
    }

    // Логика UI
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Сдвигаем прямоугольники с изображениями вниз
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 40.dp), // Добавляем отступ сверху
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                (0 until 2).forEach { rowIndex ->
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        (0 until 3).forEach { colIndex ->
                            val index = rowIndex * 3 + colIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.5f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Gray)
                                    .let {
                                        if (!isDeleteMode) {
                                            it.clickable {
                                                selectedImageIndex = index
                                                showDialog = true // Показываем диалог при клике
                                            }
                                        } else {
                                            it.pointerInput(Unit) {} // Убираем кликабельность в режиме удаления
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageUris[index] != Uri.EMPTY) {
                                    AsyncImage(
                                        model = imageUris[index],
                                        contentDescription = "Selected Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Если в режиме удаления, показываем крестик
                                    if (isDeleteMode) {
                                        IconButton(
                                            onClick = {
                                                viewModel.updateImageUri(index, Uri.EMPTY)
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(36.dp)
                                                .background(Color.Black, shape = CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete Image",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Image",
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Кнопка "Удалить" для включения/выключения режима удаления
            Button(
                onClick = { isDeleteMode = !isDeleteMode },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(if (isDeleteMode) "Отменить удаление" else "Включить удаление")
            }

            // Кнопка подтверждения выбора (некликабельна, если режим удаления включен)
            Button(
                onClick = {
                    val selectedImages = imageUris.filter { it != Uri.EMPTY }
                    uploadImagesToServerAndSave(selectedImages, context, login)  // Отправка изображений на сервер
                    onClose()  // Закрытие экрана
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .let {
                        if (isDeleteMode) it.alpha(0.3f).pointerInput(Unit) {} else it
                    }
            ) {
                Text("Подтвердить")
            }
        }
    }

    // Вызов диалога
    if (showDialog) {
        showImageSourceDialog(onSelectGallery = { openGallery() })
    }
}