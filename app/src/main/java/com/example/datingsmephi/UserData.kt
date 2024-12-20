package com.example.datingsmephi

import android.graphics.Bitmap
import android.net.Uri
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Forms(
    @SerializedName("users_data")
    val usersData: Array<UserData>, // Список ссылок на изображения

    @SerializedName("users_photos")
    val usersPhoto: Array<UserImagesPaths> // Список ссылок на изображения
)

@Serializable
data class UserData(
    @SerializedName("telegram_tag")
    var tg: String?,

    @SerializedName("user_id")
    var UUID: String?,

    @SerializedName("group")
    var group: String?,

    @SerializedName("group_is_hidden")
    var groupHidden: Boolean?,

    @SerializedName("course")
    var course: String?,

    @SerializedName("grade")
    var averageGrade: String?,

    @SerializedName("grade_is_hidden")
    var averageGradeHidden: Boolean?,

    @SerializedName("last_name")
    var lastName: String?,

    @SerializedName("last_name_is_hidden")
    var lastNameHidden: Boolean?,

    @SerializedName("first_name")
    var firstName: String?,

    @SerializedName("middle_name")
    var middleName: String?,

    @SerializedName("middle_name_is_hidden")
    var middleNameHidden: Boolean?,

    @SerializedName("is_man")
    var gender: String?,

    @SerializedName("age")
    var age: String?,

    @SerializedName("age_is_hidden")
    var ageHidden: Boolean?,

    @SerializedName("height")
    var height: String?,

    @SerializedName("height_is_hidden")
    var heightHidden: Boolean?,

    @SerializedName("is_smoking")
    var isSmoking: Boolean?,

    @SerializedName("is_smoking_is_hidden")
    var isSmokingHidden: Boolean?,

    @SerializedName("is_drinking")
    var isDrinking: Boolean?,

    @SerializedName("is_drinking_is_hidden")
    var isDrinkingHidden: Boolean?,

    @SerializedName("zodiac")
    var zodiacSign: String?,

    @SerializedName("zodiac_is_hidden")
    var zodiacSignHidden: Boolean?,

    @SerializedName("fav_sports")
    var sports: String?,

    @SerializedName("fav_sports_is_hidden")
    var sportsHidden: Boolean?,

    @SerializedName("fav_music")
    var music: String?,

    @SerializedName("fav_music_is_hidden")
    var musicHidden: Boolean?,

    @SerializedName("bio")
    var aboutMe: String?,

    @SerializedName("message")
    var message: String?,

    @SerializedName("message_liked")
    var messageLiked: String?,

    @SerializedName("requirements")
    var goals: Array<String>,

    @SerializedName("interests")
    var interests: Array<String>
)

@Serializable
data class UserDataForRegistration(
    @SerializedName("access_token")
    var accessToken: String?,

    @SerializedName("refresh_token")
    var refreshToken: String?,

    @SerializedName("login")
    var login: String?,

    @SerializedName("user_id")
    var UUID: String?,

    @SerializedName("user_agent")
    var userAgent: String?,

    @SerializedName("tgt")
    var tgt: String?,

    @SerializedName("telegram_tag")
    var tag: String?,

    @SerializedName("password")
    var password: String?,
)

@Serializable
data class UserImagesPaths(
    @SerializedName("user_photos")
    val photo_name: List<String> // Список ссылок на изображения
)
data class UserImagesUri(
    val photo_name: List<Uri> // Список ссылок на изображения
)

@Serializable
data class UserFilters(
    @SerializedName("course_start")
    var courseStart: Int,

    @SerializedName("course_end")
    var courseEnd: Int,

    @SerializedName("gender_man")
    var genderMan: Boolean,

    @SerializedName("gender_woman")
    var genderWoman: Boolean
)

@Serializable
data class Like(
    @SerializedName("user_liking_id")
    val UUID: String, // Список ссылок на изображения

    @SerializedName("user_liked_id")
    val UUID_form: String, // Список ссылок на изображения

    @SerializedName("status")
    val status: Int, // Список ссылок на изображения

    @SerializedName("message")
    val message: String? // Список ссылок на изображения
)
