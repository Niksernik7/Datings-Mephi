package com.example.datingsmephi

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApiService {
    @GET("api/users/get_user_data")
    suspend fun getUserData(
        @Query("login") login: String
    ): Response<UserData>

    @POST("api/users/post_user_data")
    suspend fun updateUserData(
        @Body userData: UserData
    ): Response<Void> // Возвращаемый тип, например, Void если сервер не возвращает тело ответа
}