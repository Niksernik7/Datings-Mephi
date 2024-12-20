package com.example.datingsmephi

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {
    @GET("api/users/user_data/{user_id}")
    suspend fun getUserData(
        @Header ("Authorization") bearer : String?,
        @Path("user_id") UUID: String
    ): Response<UserData>

    @POST("api/users/user_data")
    suspend fun updateUserData(
        @Header ("Authorization") bearer : String?,
        @Body userData: UserData
    ): Response<Void>

    @POST("api/users/registration")
    suspend fun registerUser(
        @Body UserDataForReg: UserDataForRegistration
    ): Response<UserDataForRegistration>

    @POST("api/users/telegram")
    suspend fun postUserData(
        @Body UserDataForReg: UserDataForRegistration
    ): Response<Void>

    @POST("api/users/login")
    suspend fun loginUser(
        @Body UserDataForReg: UserDataForRegistration
    ): Response<UserDataForRegistration>

    @GET("api/token/refresh")
    suspend fun refreshToken(
        @Header ("Authorization") bearer : String?,
    ): Response<UserDataForRegistration>

    @GET("api/token/validation_access_token/{user_id}")
    suspend fun validateToken(
        @Header ("Authorization") bearer : String?,
        @Path ("user_id") UUID : String?
    ): Response<Void>

    @Multipart
    @POST("api/users/user_images")
    suspend fun uploadImages(
        @Header ("Authorization") bearer : String?,
        @Part files: List<MultipartBody.Part>,
        @Part("user_id") userId: String,
        @Part("names") names: Array<String>
    ): Response<Void>

    @GET("api/users/user_images/{user_id}")
    suspend fun getUserImagesPaths(
        @Header ("Authorization") bearer : String?,
        @Path("user_id") UUID: String
    ): Response<UserImagesPaths>

    @POST("/api/users/users_status")
    suspend fun postUserDataForm(
        @Header ("Authorization") bearer : String?,
        @Body form: Like
    ): Response<Void>

    @GET("/api/users/users_data/{user_liking_id}")
    suspend fun getUserForms(
        @Header("Authorization") bearer: String,
        @Path("user_liking_id") userId: String, // Передаем user_id в путь
        @Query("course_start") course_start: Int?, // Передаем user_id в путь
        @Query("course_end") course_end: Int?, // Передаем user_id в путь
        @Query("gender_man") gender_man: Boolean?, // Передаем user_id в путь
        @Query("gender_woman") gender_woman: Boolean?, // Передаем user_id в путь
        @Query("limit") limit: Int // Передаем limit в путь
    ): Response<Forms>

    @GET("/api/users/users_status/{user_liking_id}")
    suspend fun getLists(
        @Header ("Authorization") bearer : String?,
        @Path ("user_liking_id") UUID : String?,
        @Query ("status_sent") status : Int
    ): Response<Forms>

    @DELETE("/api/users/users_unactioned/{user_id}")
    suspend fun DeleteForms(
        @Header ("Authorization") bearer : String?,
        @Path ("user_id") UUID : String?
    ): Response<Void>
}