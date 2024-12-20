package com.example.datingsmephi

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:5002/" // Укажите ваш базовый URL
    private val client = OkHttpClient()
    val gson = GsonBuilder()
        .serializeNulls() // Явно включаем null-поля
        .create()

    val api: UserApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
            .create(UserApiService::class.java)
    }
}
