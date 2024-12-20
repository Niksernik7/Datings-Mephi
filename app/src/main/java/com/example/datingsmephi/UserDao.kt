package com.example.datingsmephi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Работа с таблицей UserData
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserData(userData: UserDataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPhotos(userPhotos: UserPhotosEntity)

    @Query("SELECT * FROM user_data WHERE UUID = :uuid")
    suspend fun getUserData(uuid: String): UserDataEntity?

    @Query("SELECT * FROM user_photos WHERE UUID = :uuid")
    suspend fun getUserPhotos(uuid: String): UserPhotosEntity?

    @Query("SELECT * FROM user_data")
    fun getAllUserData(): Flow<List<UserDataEntity>>

    @Query("SELECT * FROM user_photos")
    fun getAllUserPhotos(): Flow<List<UserPhotosEntity>>

    @Query("SELECT COUNT(*) FROM user_data")
    fun getUserDataCount(): Flow<Int>

    @Query("DELETE FROM user_photos WHERE UUID = :uuid")
    suspend fun deletePhotoByUuid(uuid: String)

    @Query("DELETE FROM user_data WHERE UUID = :uuid")
    suspend fun deleteDataByUuid(uuid: String)

    @Query("DELETE FROM user_data")
    suspend fun deleteAllUserData()

    @Query("DELETE FROM user_photos")
    suspend fun deleteAllUserPhotos()
}
