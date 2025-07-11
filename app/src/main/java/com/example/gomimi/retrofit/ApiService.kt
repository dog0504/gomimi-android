package com.example.gomimi.retrofit

import com.example.gomimi.dataClass.Address
import com.example.gomimi.dataClass.AuthResponse
import com.example.gomimi.dataClass.BinDay
import com.example.gomimi.dataClass.GarbageIdentificationResponse
import com.example.gomimi.dataClass.History
import com.example.gomimi.dataClass.LoginRequestBody
import com.example.gomimi.dataClass.Manual
import com.example.gomimi.dataClass.ManualInfo
import com.example.gomimi.dataClass.RegisterRequestBody
import com.example.gomimi.dataClass.UpdateRequestBody
import com.example.gomimi.dataClass.User
import com.example.gomimi.dataClass.UserResponseBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<User>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @POST("auth/login")
    suspend fun login(@Body requestBody: LoginRequestBody): Response<AuthResponse>

    // テスト用ゴミ識別APIのエンドポイント
    @Multipart
    @POST("garbage/identify")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("description") description: RequestBody? = null
    ): Response<GarbageIdentificationResponse>

    // 新しい完全一致検索APIの定義を追加
    @GET("manuals/search/exact")
    suspend fun searchManualExact(@Query("name") name: String): Response<Manual> // List<>を外し、単一のManualオブジェクトを受け取る

    @GET("languages")
    suspend fun getLanguages(): Response<List<Language>>

    @GET("addresses/search")
    suspend fun searchAddress(@Query("postalCode") postalCode: String): Response<List<Address>>

    @POST("auth/register")
    suspend fun registerUser(@Body requestBody: RegisterRequestBody): Response<AuthResponse>

    @GET("users/me/bin-days")
    suspend fun getBinDays(): Response<List<BinDay>>

    @GET("users/me/histories")
    suspend fun getHistories(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<List<History>>

    @GET("users/me")
    suspend fun getMyProfile(): Response<UserResponseBody>

    @PUT("users/me")
    suspend fun updateUserProfile(@Body requestBody: UpdateRequestBody): Response<UserResponseBody>

    @GET("manuals")
    suspend fun getAllManuals(): Response<List<ManualInfo>>

    @GET("manuals/search")
    suspend fun searchManualsByKeyword(@Query("keyword") keyword: String): Response<List<ManualInfo>>

    @GET("manuals/search/initials")
    suspend fun searchManualsByInitial(@Query("initial") initial: String): Response<List<ManualInfo>>
}