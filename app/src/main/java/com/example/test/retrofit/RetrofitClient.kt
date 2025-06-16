package com.example.test.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
//    private const val BASE_URL = "https://api.example.com/" // 実際のAPIのベースURLに置き換える
    private const val BASE_URL =  "API_ENDPOINT" // local.propertiesから値を読み込む

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}