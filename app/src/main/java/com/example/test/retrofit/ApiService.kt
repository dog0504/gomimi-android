package com.example.test.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<User>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

//    @Multipart
//    @POST("upload")
//    suspend fun uploadImage(
//        @Part image: MultipartBody.Part,
//        @Part("description") description: RequestBody? = null
//    ): Response<RecognitionResult>

    @POST("auth/login")
    suspend fun login(@Body requestBody: LoginRequestBody): Response<AuthResponse>

    // ゴミ識別APIのエンドポイント
//    @Multipart
//    @POST("garbage/identify")
//    suspend fun uploadImage(
//        @Part image: MultipartBody.Part,
//        @Part("description") description: RequestBody? = null
//    ): Response<GarbageIdentificationResponse>
    // テスト用ゴミ識別APIのエンドポイント
    @Multipart
    @POST("garbage/identify/test")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("description") description: RequestBody? = null
    ): Response<GarbageIdentificationResponse>

//    @GET("manuals/search")
//    suspend fun searchManuals(@Query("keyword") keyword: String): Response<List<Manual>>

    // 新しい完全一致検索APIの定義を追加
    @GET("manuals/search/exact")
    suspend fun searchManualExact(@Query("name") name: String): Response<Manual> // List<>を外し、単一のManualオブジェクトを受け取る

}