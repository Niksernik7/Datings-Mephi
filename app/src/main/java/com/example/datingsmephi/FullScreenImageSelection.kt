package com.example.datingsmephi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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


fun getRealPathFromURI(uri: Uri, context: Context): String? {
    Log.e("IMG", "$uri")
    if (uri.scheme.equals("content", ignoreCase = true)) {
        if ("com.android.providers.media.documents" == uri.authority) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            if (split.size == 2) {
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val selection = "${MediaStore.Images.Media._ID}=?"
                val selectionArgs = arrayOf(split[1])

                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val fileName = queryContentResolver(contentUri, MediaStore.Images.Media.DISPLAY_NAME, selection, selectionArgs, context)
                    fileName?.let {
                        val file = File(context.cacheDir, it)
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                        file.absolutePath
                    }
                } else {
                    queryContentResolver(contentUri, MediaStore.Images.Media.DATA, selection, selectionArgs, context)
                }
            }
        } else if ("com.google.android.apps.photos.contentprovider" == uri.authority) {
            return getImageFromGooglePhotos(uri, context)
        } else {
            return queryContentResolver(uri, MediaStore.Images.Media.DATA, null, null, context)

        }
    } else if (uri.scheme.equals("file", ignoreCase = true)) {
        return uri.path
    }
    return null
}

private fun queryContentResolver(
    uri: Uri,
    columnName: String?,
    selection: String?,
    selectionArgs: Array<String>?,
    context: Context
): String? {
    val projection = columnName?.let { arrayOf(it) }
    val cursor: Cursor? = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val columnIndex = columnName?.let { name -> it.getColumnIndex(name) } ?: -1
            if (columnIndex != -1) {
                return it.getString(columnIndex)
            }
        }
    }
    return null
}



private fun getImageFromGooglePhotos(uri: Uri, context: Context): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val uniqueFileName = "google_photo_${System.currentTimeMillis()}.png"
        val sph = SharedPreferencesHelper(context)
        val Names = sph.getUserImages() ?: UserImagesPaths(emptyList())
        val Names2 = UserImagesPaths(Names.photo_name + uniqueFileName)
        sph.setUserImagesPaths(Names2)
        val file = File(context.cacheDir, uniqueFileName)
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Log.e("IMG", "Saved image path: ${file.absolutePath}")
        file.absolutePath
    } catch (e: Exception) {
        Log.e("ImageError", "Failed to retrieve image from Google Photos", e)
        null
    }
}



suspend fun uploadImagesToServerAndSave(
    images: List<Uri>,
    context: Context,
) {
    val sph = SharedPreferencesHelper(context)
    val UUID = sph.getUUID(context)
    var (accessToken, _) = sph.getTokens(context)
    // Создаем клиент и сервис Retrofit
    val apiService = RetrofitInstance.api
    var userImagesResponse =  sph.getUserImages()
    userImagesResponse = userImagesResponse?.copy(photo_name = emptyList())

    // Создаем MultipartBody.Part для каждого изображения
    val imageParts = images.mapNotNull { uri ->
        val file = File(getRealPathFromURI(uri, context))
        if (!file.exists()) {
            return@mapNotNull null
        }
        withContext(Dispatchers.Main) {
            userImagesResponse?.let { response ->
                userImagesResponse = response.copy(
                    photo_name = response.photo_name + file.name
                )
            }
        }
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        MultipartBody.Part.createFormData("file", file.name, requestBody)
    }
    val Names = sph.getUserImages() ?: UserImagesPaths(emptyList())
    Log.e("NAMES", "${Names.photo_name}")
    try {
        // Отправляем запрос с использованием корутины
        val response = apiService.uploadImages(
            "Bearer $accessToken",
            imageParts,
            UUID!!,
            Names.photo_name.toTypedArray()
        )
        when (response.code()) {
            201 -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Фотки успешно сохранены",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            204 -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Фотки успешно удалены",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }

            401 -> {
                refreshAccessToken(context)
                uploadImagesToServerAndSave(images, context)
            }

            500 -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Ошибка на сервере: 500",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }

            else -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Ошибка на сервере",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Не достучались",
                Toast.LENGTH_LONG
            )
                .show()
        }
        e.printStackTrace()
    }
}


@Composable
fun FullScreenImageSelection(navController: NavController) {
    val context = LocalContext.current

    var selectedImageIndex by remember { mutableIntStateOf(-1) }
    val sph = SharedPreferencesHelper(context)
    val UUID = sph.getUUID(context)
    val deleteMode = remember { mutableStateOf(false) }
    val photoNames = sph.getUserImages()
    var readyToShow = remember { mutableStateOf(true) }
    var readyPhoto = remember { mutableStateOf(false) }

    // Список URI для 6 прямоугольников
    val imageUris = remember { mutableStateListOf<Uri?>().apply { repeat(6) { add(null) } } }
    // Лаунчер для выбора изображения
    val imageSelectionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                if (selectedImageIndex != -1) {
                    imageUris[selectedImageIndex] = it
                }
            }
            readyToShow.value = true
        }



    if (readyToShow.value) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val deleteMode = remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 40.dp),
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
                                        .then(
                                            if (!deleteMode.value &&
                                                imageUris.getOrNull(index) == null &&
                                                sph.getUserImages()?.photo_name?.getOrNull(index) == null
                                            ) {
                                                Modifier.clickable {
                                                    readyToShow.value = false
                                                    selectedImageIndex = index
                                                    imageSelectionLauncher.launch("image/*")
                                                }
                                            } else Modifier // Некликабельный, если режим удаления включен или есть изображение
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (photoNames != null && index < photoNames.photo_name.size) {
                                        val photo = photoNames.photo_name.get(index)

                                        val context = LocalContext.current
                                        val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }

                                        LaunchedEffect(photo) {
                                            readyPhoto.value = false
                                            imageBitmap.value =
                                                fetchImagesFromServer(UUID, photo, context)
                                            readyPhoto.value = true
                                        }
                                        if (readyPhoto.value) {
                                            imageBitmap.value?.let { bitmap ->
                                                Box(
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Image(
                                                        bitmap = bitmap.asImageBitmap(),
                                                        contentDescription = "Server Image",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )

                                                    // Если режим удаления включен, показываем кнопку удаления
                                                    if (deleteMode.value) {
                                                        Box(
                                                            modifier = Modifier
                                                                .align(Alignment.TopEnd)
                                                                .size(32.dp)
                                                                .clip(CircleShape)
                                                                .background(Color.Black)
                                                                .clickable {
                                                                    val updatedPhotoNames =
                                                                        photoNames.photo_name.filterIndexed { i, _ ->
                                                                            i != index
                                                                        }
                                                                    val Names = UserImagesPaths(
                                                                        updatedPhotoNames
                                                                    )
                                                                    sph.setUserImagesPaths(Names)
                                                                    deleteMode.value = false
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Close,
                                                                contentDescription = "Delete Image",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            } ?: run {
                                                // Показать индикатор загрузки или заглушку
                                                CircularProgressIndicator()
                                            }
                                        }
                                    } else {
                                        // Локально выбранное изображение
                                        imageUris.getOrNull(index)?.let { uri ->
                                            Box(
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                // Загрузка изображения с помощью Glide
                                                LoadImageWithGlide(uri = uri)

                                                // Если режим удаления включен, показываем кнопку удаления
                                                if (deleteMode.value) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.Black)
                                                            .clickable {
                                                                val updatedUris = imageUris.toMutableList()
                                                                updatedUris.removeAt(index)
                                                                imageUris.clear()
                                                                imageUris.addAll(updatedUris)
                                                                Log.e("URISPATHS", "$imageUris")
                                                                deleteMode.value = false
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Delete Image",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        } ?: run {
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
                }
                // Кнопка удаления
                Button(
                    onClick = {
                        deleteMode.value = !deleteMode.value // Переключаем режим удаления
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(if (deleteMode.value) "Выключить режим удаления" else "Включить режим удаления")
                }
                // Кнопка подтверждения
                Button(
                    onClick = {
                        val paths: List<Uri> = imageUris.filterNotNull()
                        Log.e("URISPATHS", "$paths")
                        CoroutineScope(Dispatchers.IO).launch {
                            uploadImagesToServerAndSave(paths, context)
                        }
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Подтвердить")
                }
            }
        }
    } else {
        CircularProgressIndicator()
    }
}


@Composable
fun LoadImageWithGlide(uri: Uri) {
    val context = LocalContext.current
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }

    // Запускаем Glide для загрузки изображения
    LaunchedEffect(uri) {
        val bitmap = withContext(Dispatchers.IO) {
            Glide.with(context)
                .asBitmap()
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .submit()
                .get()
        }
        imageBitmap.value = bitmap
    }
    // Отображаем изображение
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        imageBitmap.value?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Loaded Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } ?: run {
            // Заглушка или загрузчик
            CircularProgressIndicator()
        }
    }
}

suspend fun fetchImagesFromServer(UUID: String?, photoName: String, context: Context): Bitmap? {
    val baseUrl = "http://10.0.2.2:5002/images/"
    val sph = SharedPreferencesHelper(context)
    val (accessToken, _) = sph.getTokens(context) ?: return null

    val photoUrl = "$baseUrl$UUID/$photoName"
    val glideUrl = GlideUrl(
        photoUrl, LazyHeaders.Builder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
    )
    return withContext(Dispatchers.IO) {
        try {
            Glide.with(context)
                .asBitmap()
                .load(glideUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .submit()
                .get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}



