package com.example.test.retrofit

import com.orhanobut.hawk.Hawk

object TokenManager {

    private const val TOKEN_KEY = "jwt_token"

    fun saveToken(token: String) {
        // Hawkを使ってデータを保存する
        Hawk.put(TOKEN_KEY, token)
    }

    fun getToken(): String? {
        // Hawkからデータを取得する
        return Hawk.get(TOKEN_KEY, null) // 第2引数はデータがない場合のデフォルト値
    }

    fun deleteToken() {
        // Hawkからデータを削除する
        Hawk.delete(TOKEN_KEY)
    }
}