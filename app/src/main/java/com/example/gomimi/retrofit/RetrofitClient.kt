package com.example.gomimi.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL =  com.example.gomimi.BuildConfig.API_ENDPOINT // local.propertiesから値を読み込む

    // 1. AuthInterceptorを組み込んだOkHttpClientを作成
    private val okHttpClient: OkHttpClient by lazy {
        // ログレベルを設定するインターセプターを作成
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // BuildConfig.DEBUGはデバッグビルドの時だけtrueになる
            level = if (com.example.gomimi.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY // BODY: リクエスト/レスポンスの全てをログに出力
            } else {
                HttpLoggingInterceptor.Level.NONE // NONE: 本番ビルドではログを出力しない
            }
        }
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(loggingInterceptor) // ★ 作成したロギングインターセプターを追加
            .connectTimeout(30, TimeUnit.SECONDS) // 接続タイムアウトを30秒に設定
            .readTimeout(30, TimeUnit.SECONDS)    // 読み取りタイムアウトを30秒に設定
            .writeTimeout(30, TimeUnit.SECONDS)   // 書き込みタイムアウトを30秒に設定
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