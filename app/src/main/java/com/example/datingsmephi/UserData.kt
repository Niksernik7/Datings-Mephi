package com.example.datingsmephi

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    @SerializedName("login")
    var UUID: String?,

    @SerializedName("group")
    var group: String?,

    @SerializedName("course")
    var course: String?,

    @SerializedName("grade")
    var averageGrade: String?,

    @SerializedName("last_name")
    var lastName: String?,

    @SerializedName("first_name")
    var firstName: String?,

    @SerializedName("middle_name")
    var middleName: String?,

    @SerializedName("is_man")
    var gender: String?,

    @SerializedName("age")
    var age: String?,

    @SerializedName("height")
    var height: String?,

    @SerializedName("is_smoking")
    var isSmoking: Boolean?,

    @SerializedName("is_drinking")
    var isDrinking: Boolean?,

    @SerializedName("zodiac")
    var zodiacSign: String?,

    @SerializedName("fav_sports")
    var sports: String?,

    @SerializedName("fav_music")
    var music: String?,

    @SerializedName("bio")
    var aboutMe: String?,

    @SerializedName("requirements")
    var goals: Array<String>,

    @SerializedName("interests")
    var interests: Array<String>
)
