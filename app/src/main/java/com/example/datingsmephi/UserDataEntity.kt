package com.example.datingsmephi

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data")
data class UserDataEntity(
    @PrimaryKey val UUID: String,
    val tg: String?,
    val group: String?,
    val groupHidden: Boolean?,
    val course: String?,
    val averageGrade: String?,
    val averageGradeHidden: Boolean?,
    val lastName: String?,
    val lastNameHidden: Boolean?,
    val firstName: String?,
    val middleName: String?,
    val middleNameHidden: Boolean?,
    val gender: String?,
    val age: String?,
    val ageHidden: Boolean?,
    val height: String?,
    val heightHidden: Boolean?,
    val isSmoking: Boolean?,
    val isSmokingHidden: Boolean?,
    val isDrinking: Boolean?,
    val isDrinkingHidden: Boolean?,
    val zodiacSign: String?,
    val zodiacSignHidden: Boolean?,
    val sports: String?,
    val sportsHidden: Boolean?,
    val music: String?,
    val musicHidden: Boolean?,
    val aboutMe: String?,
    val message: String?,
    val messageLiked: String?,
    val goals: String,
    val interests: String
)


