package com.example.test.retrofit

import com.example.test.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
//    private const val BASE_URL = "https://api.example.com/" // 実際のAPIのベースURLに置き換える
    private const val BASE_URL =  BuildConfig.API_ENDPOINT // local.propertiesから値を読み込む

//    val instance: Retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }

    // 1. AuthInterceptorを組み込んだOkHttpClientを作成
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .build()
    }

    // 2. Retrofitのインスタンス化時に、上で作成したOkHttpClientを指定
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}