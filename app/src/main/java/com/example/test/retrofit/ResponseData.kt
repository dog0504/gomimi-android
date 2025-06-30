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
    val query: String,
    val results: List<GarbageResult>
)

// ゴミ識別結果のデータクラス
data class GarbageResult(
    val rank: Int,
    val name: String
)

// API仕様 /manuals/{manualId} や /manuals/search のレスポンスに対応
data class Manual(
    val id: Int,
    val name: String,
    val category: String,
    val remarks: String? // nullの可能性があるため
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
    @SerializedName("zip") // JSONのキーと変数名をマッピング
    val zip: String,
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

// API仕様 /users/me/histories のレスポンスに対応
data class History(
    val id: Int,
    val name: String,
    val type: String,
    val createdAt: String // 日付は文字列として受け取る
)

// API仕様 GET /users/me のレスポンスに対応
data class UserResponseBody(
    val email: String,
    val address: Address, // 住所情報
    val language: Language  // 言語情報
)

// API仕様 PUT /users/me のリクエストボディに対応
data class UpdateRequestBody(
    val languageId: Int? = null, // 更新しない場合はnull
    val addressId: Int? = null   // 更新しない場合はnull
)