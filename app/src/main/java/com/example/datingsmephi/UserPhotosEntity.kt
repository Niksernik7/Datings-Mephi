package com.example.datingsmephi

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_photos")
data class UserPhotosEntity(
    @PrimaryKey val UUID: String,
    val photoNames: String
)