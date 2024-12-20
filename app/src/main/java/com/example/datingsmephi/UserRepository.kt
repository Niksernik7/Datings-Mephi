package com.example.datingsmephi

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(context: Context) {
    private val userDao = AppDatabase.getInstance(context).userDao()

    // Сохранение данных пользователя
    suspend fun saveUserData(userData: UserDataEntity) {
        withContext(Dispatchers.IO) {
            userDao.insertUserData(userData)
        }
    }

    // Получение данных пользователя по UUID
    suspend fun getUserData(uuid: String): UserDataEntity? {
        return withContext(Dispatchers.IO) {
            userDao.getUserData(uuid)
        }
    }

    fun getAllUserData(): Flow<List<UserDataEntity>> {
        return userDao.getAllUserData() // Возвращаем Flow, который отслеживает изменения
    }

    // Сохранение фотографий пользователя
    suspend fun saveUserPhotos(userPhotos: UserPhotosEntity) {
        withContext(Dispatchers.IO) {
            userDao.insertUserPhotos(userPhotos)
        }
    }

    // Получение фотографий пользователя по UUID
    suspend fun getUserPhotos(uuid: String): UserPhotosEntity? {
        return withContext(Dispatchers.IO) {
            userDao.getUserPhotos(uuid)
        }
    }

    fun getAllUserPhotos(): Flow<List<UserPhotosEntity>> {
        return userDao.getAllUserPhotos() // Возвращаем Flow, который отслеживает изменения
    }

    fun getUserDataCount(): Flow<Int> {
        return userDao.getUserDataCount()
    }

    suspend fun deletePhotoByUuid(uuid: String) {
        userDao.deletePhotoByUuid(uuid)
    }

    suspend fun deleteDataByUuid(uuid: String) {
        userDao.deleteDataByUuid(uuid)
    }

    suspend fun deleteAllByUuid(uuid: String) {
        deletePhotoByUuid(uuid)
        deleteDataByUuid(uuid)
    }

    suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            userDao.deleteAllUserData()
            userDao.deleteAllUserPhotos()
        }
    }
}

