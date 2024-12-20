package com.example.datingsmephi

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineStart
import android.util.Base64
import java.io.ByteArrayOutputStream

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()

    companion object {
        private const val KEY_USER_PATHS = "key_user_paths"
    }

    fun saveFilters(context: Context, userFilters: UserFilters) {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Сохраняем массив курсов как строку с разделителями (например, запятая)
        editor.putString("courseStart", userFilters.courseStart.toString())
        editor.putString("courseEnd", userFilters.courseEnd.toString())

        // Сохраняем гендер как строку ("true" или "false")
        editor.putString("genderMan", userFilters.genderMan.toString())
        editor.putString("genderWoman", userFilters.genderWoman.toString())

        editor.apply()
    }

    fun getFilters(context: Context): UserFilters? {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // Извлекаем строку курсов
        val coursesStartString = sharedPreferences.getString("courseStart", null)
        val coursesEndString = sharedPreferences.getString("courseEnd", null)
        val coursesStart = coursesStartString?.toInt() ?: 1
        val coursesEnd = coursesEndString?.toInt() ?: 9

        // Извлекаем гендер
        val genderManString = sharedPreferences.getString("genderMan", null)
        val genderWomanString = sharedPreferences.getString("genderWoman", null)
        val genderMan = genderManString?.toBoolean() ?: true // По умолчанию false, если значение не найдено
        val genderWoman = genderWomanString?.toBoolean() ?: true // По умолчанию false, если значение не найдено

        // Возвращаем объект UserFilters, если курс найден
        return UserFilters(coursesStart, coursesEnd, genderMan, genderWoman)

    }


    // Сеттер
    fun setUserImagesPaths(response: UserImagesPaths) {
        Log.e("PATH", "SET ${response.photo_name}")
        val jsonString = gson.toJson(response) // Сериализуем в JSON
        editor.putString(KEY_USER_PATHS, jsonString)
        editor.apply() // Применяем изменения
    }

    // Геттер
    fun getUserImages(): UserImagesPaths? {
        val jsonString = sharedPreferences.getString(KEY_USER_PATHS, null) ?: return null
        return try {
            gson.fromJson(jsonString, UserImagesPaths::class.java) // Десериализуем обратно в объект
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("access_token", accessToken)
        editor.putString("refresh_token", refreshToken)
        editor.apply()
    }

    fun getTokens(context: Context): Pair<String?, String?> {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("access_token", null)
        val refreshToken = sharedPreferences.getString("refresh_token", null)
        return Pair(accessToken, refreshToken)
    }

    fun saveUUID(context: Context, UUID: String?) {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("uuid", UUID)
        editor.apply()
    }

    fun getUUID(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val UUID = sharedPreferences.getString("uuid", null)
        return UUID
    }

    // Сохранение значений
    fun saveProfile(userData: UserData?, context: Context) {
        val UUID = getUUID(context)
        sharedPreferences.edit().apply {
            putString("telegram_tag", userData?.tg)
            putString("user_id", UUID)
            putString("group", userData?.group)
            putBoolean("groupHidden", userData?.groupHidden ?: false)
            putString("course", userData?.course)
            putString("averageGrade", userData?.averageGrade)
            putBoolean("averageGradeHidden", userData?.averageGradeHidden ?: false)
            putString("lastName", userData?.lastName)
            putBoolean("lastNameHidden", userData?.lastNameHidden ?: false)
            putString("firstName", userData?.firstName)
            putString("middleName", userData?.middleName)
            putBoolean("middleNameHidden", userData?.middleNameHidden ?: false)
            putString("gender", userData?.gender)
            putString("age", userData?.age)
            putBoolean("ageHidden", userData?.ageHidden ?: false)
            putString("height", userData?.height)
            putBoolean("heightHidden", userData?.heightHidden ?: false)
            putBoolean("isSmoking", userData?.isSmoking ?: false)
            putBoolean("isSmokingHidden", userData?.isSmokingHidden ?: false)
            putBoolean("isDrinking", userData?.isDrinking ?: false)
            putBoolean("isDrinkingHidden", userData?.isDrinkingHidden ?: false)
            putString("zodiacSign", userData?.zodiacSign)
            putBoolean("zodiacSignHidden", userData?.zodiacSignHidden ?: false)
            putString("sports", userData?.sports)
            putBoolean("sportsHidden", userData?.sportsHidden ?: false)
            putString("music", userData?.music)
            putBoolean("musicHidden", userData?.musicHidden ?: false)
            putString("aboutMe", userData?.aboutMe)

            val allGoals = arrayOf(
                "партнер по секции",
                "команда для хакатона",
                "совместная учеба",
                "совместный проект",
                "набор на работу"
            )
            val allInterests = arrayOf(
                "программирование",
                "математика",
                "самбо",
                "рыбалка",
                "футбол"
            )
            // Сбросить все значения для целей в false
            allGoals.forEach { goal ->
                putBoolean("goal_$goal", false)
            }
            // Сбросить все значения для интересов в false
            allInterests.forEach { interest ->
                putBoolean("interest_$interest", false)
            }
            // Установить значения true для тех целей, которые переданы
            userData?.goals?.forEach { goal ->
                putBoolean("goal_$goal", true)
            }
            // Установить значения true для тех интересов, которые переданы
            userData?.interests?.forEach { interest ->
                putBoolean("interest_$interest", true)
            }
            apply()
        }
    }

    // Загрузка значений
    fun loadProfile(): UserData {
        val allGoals = arrayOf(
            "партнер по секции",
            "команда для хакатона",
            "совместная учеба",
            "совместный проект",
            "набор на работу"
        )
        val allInterests = arrayOf(
            "программирование",
            "математика",
            "самбо",
            "рыбалка",
            "футбол"
        )

        // Фильтруем цели и интересы, которые сохранены как true
        val selectedGoals = allGoals.filter { sharedPreferences.getBoolean("goal_$it", false) }.toTypedArray()
        val selectedInterests = allInterests.filter { sharedPreferences.getBoolean("interest_$it", false) }.toTypedArray()


        return UserData(
            UUID = sharedPreferences.getString("user_id", null),
            tg = sharedPreferences.getString("telegram_tag", null),
            group = sharedPreferences.getString("group", null),
            groupHidden = sharedPreferences.getBoolean("groupHidden", false),
            course = sharedPreferences.getString("course", null),
            averageGrade = sharedPreferences.getString("averageGrade", null),
            averageGradeHidden = sharedPreferences.getBoolean("averageGradeHidden", false),
            lastName = sharedPreferences.getString("lastName", null),
            lastNameHidden = sharedPreferences.getBoolean("lastNameHidden", false),
            firstName = sharedPreferences.getString("firstName", null),
            middleName = sharedPreferences.getString("middleName", null),
            middleNameHidden = sharedPreferences.getBoolean("middleNameHidden", false),
            gender = sharedPreferences.getString("gender", null),
            age = sharedPreferences.getString("age", null),
            ageHidden = sharedPreferences.getBoolean("ageHidden", false),
            height = sharedPreferences.getString("height", null),
            heightHidden = sharedPreferences.getBoolean("heightHidden", false),
            isSmoking = sharedPreferences.getBoolean("isSmoking", false),
            isSmokingHidden = sharedPreferences.getBoolean("isSmokingHidden", false),
            isDrinking = sharedPreferences.getBoolean("isDrinking", false),
            isDrinkingHidden = sharedPreferences.getBoolean("isDrinkingHidden", false),
            zodiacSign = sharedPreferences.getString("zodiacSign", null),
            zodiacSignHidden = sharedPreferences.getBoolean("zodiacSignHidden", false),
            sports = sharedPreferences.getString("sports", null),
            sportsHidden = sharedPreferences.getBoolean("sportsHidden", false),
            music = sharedPreferences.getString("music", null),
            musicHidden = sharedPreferences.getBoolean("musicHidden", false),
            aboutMe = sharedPreferences.getString("aboutMe", null),
            message = sharedPreferences.getString("message", null),
            messageLiked = sharedPreferences.getString("message_liked", null),
            goals = selectedGoals,
            interests = selectedInterests,
        )
    }
}