package com.example.datingsmephi

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun get_forms(saveCompleted: MutableState<Boolean>,context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        // Вызываем sendGetFormsRequest с передачей onResponse лямбды.
        sendGetFormsRequest(context) { result ->
            when {
                result.isSuccess -> {
                    val forms = result.getOrNull()
                    forms?.let {
                        CoroutineScope(Dispatchers.IO).launch {

                            // Сохраняем данные в БД
                            saveDataToDatabase(it, context)
                            // После того как все данные сохранены, обновляем состояние
                            // Мы вызываем это на главном потоке
                            withContext(Dispatchers.Main) {
                                // Обновляем состояние в UI, чтобы продолжить проверку
                                saveCompleted.value = true
                            }
                        }
                    }
                }
            }
        }
    }
}


// Функция для сохранения данных в БД
suspend fun saveDataToDatabase(forms: Forms, context: Context) {
    val userRepository = UserRepository(context)

    // Сохраняем данные пользователей
    forms.usersData.forEach { userData ->
        val userEntity = UserDataEntity(
            UUID = userData.UUID ?: "",
            tg = userData.tg,
            group = userData.group,
            groupHidden = userData.groupHidden,
            course = userData.course,
            averageGrade = userData.averageGrade,
            averageGradeHidden = userData.averageGradeHidden,
            lastName = userData.lastName,
            lastNameHidden = userData.lastNameHidden,
            firstName = userData.firstName,
            middleName = userData.middleName,
            middleNameHidden = userData.middleNameHidden,
            gender = userData.gender,
            age = userData.age,
            ageHidden = userData.ageHidden,
            height = userData.height,
            heightHidden = userData.heightHidden,
            isSmoking = userData.isSmoking,
            isSmokingHidden = userData.isSmokingHidden,
            isDrinking = userData.isDrinking,
            isDrinkingHidden = userData.isDrinkingHidden,
            zodiacSign = userData.zodiacSign,
            zodiacSignHidden = userData.zodiacSignHidden,
            sports = userData.sports,
            sportsHidden = userData.sportsHidden,
            music = userData.music,
            musicHidden = userData.musicHidden,
            aboutMe = userData.aboutMe,
            message = userData.message,
            messageLiked = userData.messageLiked,
            goals = userData.goals.joinToString(","),
            interests = userData.interests.joinToString(",")
        )
        userRepository.saveUserData(userEntity)
    }

    // Сохраняем фотографии пользователей
    forms.usersPhoto.forEachIndexed { index, userPhoto ->
        val userData = forms.usersData.getOrNull(index)
        val userPhotosEntity = UserPhotosEntity(
            UUID = userData?.UUID ?: "",
            photoNames = userPhoto.photo_name.joinToString(",")
        )
        userRepository.saveUserPhotos(userPhotosEntity)
    }
}

suspend fun sendGetFormsRequest(
    context: Context,
    onResponse: (Result<Forms>) -> Unit
) {
    val sph = SharedPreferencesHelper(context)
    val (accessToken, _) = sph.getTokens(context)
    val userId = sph.getUUID(context)
    val UUID = userId ?: ""
    val filter = sph.getFilters(context)

    try {
        val response = if (filter != null) {
            RetrofitInstance.api.getUserForms(
                "Bearer $accessToken",
                UUID,
                filter.courseStart,
                filter.courseEnd,
                filter.genderMan,
                filter.genderWoman,
                5
            )
        } else {
            RetrofitInstance.api.getUserForms(
                "Bearer $accessToken",
                UUID,
                null, null, null, null, 5
            )
        }

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



// Измененная функция для получения данных через Flow
fun getFormsFromDatabase(context: Context): Flow<Forms> {
    val userRepository = UserRepository(context)

    return combine(
        userRepository.getAllUserData(), // Возвращает Flow<List<UserDataEntity>>
        userRepository.getAllUserPhotos() // Возвращает Flow<List<UserPhotosEntity>>
    ) { userDataEntities, userPhotosEntities ->
        val usersData = userDataEntities.map { userDataEntity ->
            UserData(
                UUID = userDataEntity.UUID,
                tg = userDataEntity.tg,
                group = userDataEntity.group,
                groupHidden = userDataEntity.groupHidden,
                course = userDataEntity.course,
                averageGrade = userDataEntity.averageGrade,
                averageGradeHidden = userDataEntity.averageGradeHidden,
                lastName = userDataEntity.lastName,
                lastNameHidden = userDataEntity.lastNameHidden,
                firstName = userDataEntity.firstName,
                middleName = userDataEntity.middleName,
                middleNameHidden = userDataEntity.middleNameHidden,
                gender = userDataEntity.gender,
                age = userDataEntity.age,
                ageHidden = userDataEntity.ageHidden,
                height = userDataEntity.height,
                heightHidden = userDataEntity.heightHidden,
                isSmoking = userDataEntity.isSmoking,
                isSmokingHidden = userDataEntity.isSmokingHidden,
                isDrinking = userDataEntity.isDrinking,
                isDrinkingHidden = userDataEntity.isDrinkingHidden,
                zodiacSign = userDataEntity.zodiacSign,
                zodiacSignHidden = userDataEntity.zodiacSignHidden,
                sports = userDataEntity.sports,
                sportsHidden = userDataEntity.sportsHidden,
                music = userDataEntity.music,
                musicHidden = userDataEntity.musicHidden,
                aboutMe = userDataEntity.aboutMe,
                message = userDataEntity.message,
                messageLiked = userDataEntity.messageLiked,
                goals = userDataEntity.goals.split(",").toTypedArray(),
                interests = userDataEntity.interests.split(",").toTypedArray()
            )
        }.toTypedArray()

        val usersPhotos = userPhotosEntities.map { userPhotosEntity ->
            UserImagesPaths(photo_name = userPhotosEntity.photoNames.split(","))
        }.toTypedArray()

        Forms(usersData = usersData, usersPhoto = usersPhotos)
    }
}







@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val userRepository = UserRepository(context)
    val userDataCount by userRepository.getUserDataCount().collectAsState(initial = -1)
    var trigger = remember { mutableStateOf(false) }


    // Состояние, чтобы отслеживать завершение сохранения данных
    var saveCompleted = remember { mutableStateOf(true) }

    Log.e("ITR", "Before IF $userDataCount")

    // Следим за изменениями в userDataCount и saveCompleted
    LaunchedEffect(trigger.value, userDataCount, saveCompleted) {
        Log.e("CNTITR", "${userDataCount}")
        // Проверка только если сохранение завершено и данные меньше 5
        if (saveCompleted.value && userDataCount >= 0 && userDataCount < 5) {
            Log.e("CNTITR", "In IF $userDataCount")
            // Запрашиваем данные только если меньше 5 записей
            saveCompleted.value = false
            get_forms(saveCompleted, context)
        }
    }



    NavHost(navController = navController, startDestination = "survey_screen") {

        composable("edit_profile_screen") {
            EditProfileScreen(trigger, navController)
        }
        composable("full_screen") {
            FullScreenImageSelection(navController) // Передаём ViewModel
        }
        composable("privacy_screen") {
            PrivacySettingsScreen(navController)
        }
        composable("show_profile") {
            ShowProfile(navController)
        }
        composable("survey_screen") {
            SurveyScreen(trigger, navController)
        }
        composable("likes_screen") {
            LikesScreen(navController)
        }
        composable("profile_screen") {
            ProfileScreen(navController)
        }
        composable("filters_screen") {
            FiltersScreen(trigger, navController)
        }
    }
}
