package com.example.datingsmephi

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.datingsmephi.ImageSelectionViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream


fun extractImagesFromZip(zipInputStream: InputStream, outputDir: File): List<Uri> {
    val extractedImages = mutableListOf<Uri>()

    try {
        val zis = ZipInputStream(zipInputStream)
        var zipEntry = zis.nextEntry

        while (zipEntry != null) {
            if (!zipEntry.isDirectory) {
                val fileName = zipEntry.name.substringAfterLast("/")
                val tempFile = File(outputDir, fileName)

                FileOutputStream(tempFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var len: Int
                    while (zis.read(buffer).also { len = it } > 0) {
                        outputStream.write(buffer, 0, len)
                    }
                }

                extractedImages.add(Uri.fromFile(tempFile)) // Добавляем извлеченное изображение как URI
            }
            zipEntry = zis.nextEntry
        }

        zis.closeEntry()
        zis.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return extractedImages
}

fun fetchZipAndExtractImages(
    context: Context,
    login: String?,
    onImagesExtracted: (List<Uri>) -> Unit
) {
    val zipUrl = "http://10.0.2.2:5002/api/users/get_user_images?login=$login" // Замените на ваш реальный URL
    val client = OkHttpClient()

    val request = Request.Builder()
        .url(zipUrl)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val zipInputStream = response.body?.byteStream()
                zipInputStream?.let {
                    val tempDir = File(context.cacheDir, "extracted_images").apply {
                        if (!exists()) mkdir()
                    }
                    val images = extractImagesFromZip(it, tempDir)
                    // Вызываем коллбэк с результатом
                    Handler(Looper.getMainLooper()).post {
                        onImagesExtracted(images)
                    }
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                }
            }
        }
    })
}



@Composable
fun AppScreen(userData: UserData, login: String?, context: Context, viewModel: ImageSelectionViewModel) {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Survey", "Likes", "Profile")
    val context: Context = LocalContext.current
    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            when (item) {
                                "Likes" -> Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Likes"
                                )

                                "Survey" -> Icon(
                                    imageVector = Icons.Filled.Assessment,
                                    contentDescription = "Survey"
                                )

                                "Profile" -> Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Profile"
                                )
                            }
                        },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> SurveyScreen()
                1 -> LikesScreen()
                2 -> {
                    ProfileScreen(login, context)
                    fetchZipAndExtractImages(
                        context = context,
                        login = login ?: ""
                    ) { extractedImages ->
                            viewModel.setImageUrisWithPlaceholders(extractedImages) // Передаем фотографии с заполнением
                    }
                }
            }
        }
    }
}
