package com.example.gomimi.dataClass

data class LoginRequestBody(
    val email: String,
    val password: String
)

// POST /auth/register のリクエストボディ
data class RegisterRequestBody(
    val email: String,
    val password: String,
    val languageId: Int,
    val addressId: Int
)

// API仕様 PUT /users/me のリクエストボディに対応
data class UpdateRequestBody(
    val languageId: Int? = null, // 更新しない場合はnull
    val addressId: Int? = null   // 更新しない場合はnull
)