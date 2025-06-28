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

// GET /languages のレスポンス要素
data class Language(
    val id: Int,
    val name: String,
    val code: String
)

// GET /addresses/search のレスポンス要素
data class Address(
    val id: Int,
    @SerializedName("postal-code") // JSONのキーと変数名をマッピング
    val postalCode: String,
    val city: String,
    val ward: String,
    val town: String, // nullの場合があるため
    val chom: String?,
    val street: String?,
    val inf: String?
)

// POST /auth/register のリクエストボディ
data class RegisterRequestBody(
    val email: String,
    val password: String,
    val languageId: Int,
    val addressId: Int
)

// API仕様 /users/me/bin-days のレスポンスに対応
data class BinDay(
    val id: Int,
    val type: String,
    val dayOfWeek: String,
    val time: String
)