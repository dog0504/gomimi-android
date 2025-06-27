package com.example.test.retrofit

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val name: String,
    val email: String,
    val token: String
)

//data class RecognitionResult(
//    val name: String,
//    val description: String
//)

//data class ResponseData()

data class LoginRequestBody(
    val email: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String
)

// ゴミ識別APIのレスポンスデータ
data class GarbageIdentificationResponse(
    @SerializedName("query_text")
    val queryText: String,
    val results: List<GarbageResult>
)

// ゴミ識別結果のデータクラス
data class GarbageResult(
    val rank: Int,
    val name: String
)