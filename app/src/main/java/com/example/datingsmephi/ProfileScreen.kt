package com.example.datingsmephi

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient


@Composable
fun ProfileScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val sph = SharedPreferencesHelper(context)
    var imageBitmap: MutableState<ImageBitmap?> = remember { mutableStateOf(null) }
    var readyToShow = remember { mutableStateOf(false) }
    var shouldReloadImages by remember { mutableStateOf(false) }

    LaunchedEffect(navController.currentBackStackEntryAsState().value?.destination?.route) {
        val currentRoute = navController.currentDestination?.route
        if (currentRoute == "profile_screen") {
            getImagesFromServer(context)
            shouldReloadImages = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Профиль",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        Button(
            onClick = { navController.navigate("show_profile") },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 210.dp)
        ) {
            Text("Посмотреть профиль")
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
                .clickable {
                    navController.navigate("full_screen")
                }
        ) {
            val userImagesResponse = sph.getUserImages()
            val profilePhotoName = userImagesResponse?.photo_name?.firstOrNull()
            Log.e("IMAGES", "$profilePhotoName")
            if (profilePhotoName != null) {
                LaunchedEffect(navController.currentBackStackEntryAsState().value?.destination?.route) {
                    readyToShow.value = false
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    if (currentRoute == "profile_screen") {
                        imageBitmap.value = fetchImageBitmap(context, profilePhotoName)
                        readyToShow.value = true
                    }
                }
                if (readyToShow.value) {
                    if (imageBitmap.value == null) {
                        Image(
                            painter = painterResource(R.drawable.default_icon),
                            contentDescription = "User Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop // Масштабирование под круг
                        )
                    } else {
                        imageBitmap.value?.let { bitmap ->
                            Image(
                                bitmap = bitmap,
                                contentDescription = "User Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop // Масштабирование под круг
                            )
                        }
                    }
                } else {
                    CircularProgressIndicator() // Показ индикатора загрузки
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_icon),
                    contentDescription = "User Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape), // Обрезка в круг
                    contentScale = ContentScale.Crop // Масштабирование под круг
                )
            }
        }

        Button(
            onClick = { navController.navigate("edit_profile_screen") },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 200.dp)
        ) {
            Text("Редактировать")
        }
    }

    if (shouldReloadImages) {
        LaunchedEffect(Unit) {
            getImagesFromServer(context)
            shouldReloadImages = false
        }
    }
}

suspend fun fetchImageBitmap(
    context: Context,
    photoName: String
): ImageBitmap? {
    val sph = SharedPreferencesHelper(context)
    val (accessToken, _) = sph.getTokens(context)
    val UUID = sph.getUUID(context)
    val baseUrl = "http://10.0.2.2:5002/images/"
    val photoUrl = "$baseUrl$UUID/$photoName"

    return withContext(Dispatchers.IO) {
        try {
            val glideUrl = GlideUrl(
                photoUrl,
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
            )
            val bitmap = Glide.with(context)
                .asBitmap()
                .load(glideUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .submit()
                .get()
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}




