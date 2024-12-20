package com.example.datingsmephi

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(trigger: MutableState<Boolean>, navController: NavController) {
    val context = LocalContext.current
    val sph = SharedPreferencesHelper(context)
    val UUID = sph.getUUID(context) ?: ""
    val (accessToken, _) = sph.getTokens(context)
    val showProfile = false
    val userRepository = UserRepository(context)
    trigger.value = !trigger.value


    val iterator by userRepository.getUserDataCount().collectAsState(initial = 0)
    val forms by getFormsFromDatabase(context).collectAsState(initial = Forms(emptyArray(), emptyArray()))


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Анкеты",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("filters_screen")
                    }) {
                        Icon(
                            imageVector = Icons.Default.FilterList, // Иконка фильтра
                            contentDescription = "Фильтры"
                        )
                    }
                },
                modifier = Modifier.height(48.dp), // Установка высоты
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.LightGray)
            )
        },
        content = { paddingValues ->
            if (forms != null && !(forms.usersPhoto.isNullOrEmpty()) && !(forms.usersData.isNullOrEmpty()) && iterator != 0) {
                ProfileView(paddingValues, forms.usersData[0], forms.usersPhoto[0], showProfile)
            } else {
                Log.e("FORMS", "null")
            }
        }
    )
}


//
//@Preview(showBackground = true)
//@Composable
//fun SurveyScreenPreview() {
//    val navController = rememberNavController()
//    SurveyScreen(navController)
//}

