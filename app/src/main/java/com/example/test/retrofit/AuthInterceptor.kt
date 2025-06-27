package com.example.test.retrofit

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // TokenManagerから保存されているトークンを取得
        val token = TokenManager.getToken()

        // 元のリクエストを取得
        val originalRequest = chain.request()

        // トークンがあれば、ヘッダーを追加した新しいリクエストを作成
        val requestBuilder = originalRequest.newBuilder()
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // 新しいリクエストを組み立てて、次の処理へ渡す
        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }
}