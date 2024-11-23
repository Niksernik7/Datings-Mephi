package com.example.datingsmephi

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

    // Сохранение значений
    fun saveProfile(userData: UserData?) {
        sharedPreferences.edit().apply {
            putString("login", userData?.UUID)
            putString("group", userData?.group)
            putString("course", userData?.course)
            putString("averageGrade", userData?.averageGrade)
            putString("lastName", userData?.lastName)
            putString("firstName", userData?.firstName)
            putString("middleName", userData?.middleName)
            putString("gender", userData?.gender)
            putString("age", userData?.age)
            putString("height", userData?.height)
            putBoolean("isSmoking", userData?.isSmoking ?: false)
            putBoolean("isDrinking", userData?.isDrinking ?: false)
            putString("zodiacSign", userData?.zodiacSign)
            putString("sports", userData?.sports)
            putString("music", userData?.music)
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
            group = sharedPreferences.getString("group", null),
            course = sharedPreferences.getString("course", null),
            averageGrade = sharedPreferences.getString("averageGrade", null),
            lastName = sharedPreferences.getString("lastName", null),
            firstName = sharedPreferences.getString("firstName", null),
            middleName = sharedPreferences.getString("middleName", null),
            gender = sharedPreferences.getString("gender", null),
            age = sharedPreferences.getString("age", null),
            height = sharedPreferences.getString("height", null),
            isSmoking = sharedPreferences.getBoolean("isSmoking", false),
            isDrinking = sharedPreferences.getBoolean("isDrinking", false),
            zodiacSign = sharedPreferences.getString("zodiacSign", null),
            sports = sharedPreferences.getString("sports", null),
            music = sharedPreferences.getString("music", null),
            aboutMe = sharedPreferences.getString("aboutMe", null),
            goals = selectedGoals,
            interests = selectedInterests,
            UUID = sharedPreferences.getString("aboutMe", null),
        )
    }
}