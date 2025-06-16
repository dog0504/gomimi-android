package com.example.test.retrofit

// 例: UserRepository.kt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class UserRepository(private val userService: ApiService = RetrofitClient.instance.create(ApiService::class.java)) {

    // loginのシミュレーション
    suspend fun login(email: String, password: String): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            delay(1000) // 1秒の遅延（ネットワーク遅延のシミュレーション）

            if (email == "test@test.com" && password == "123") {
                NetworkResult.Success(User(id = "user123", name = "テストユーザー", email = "test@example.com", token = "mock_jwt_token_12345"))
            } else if (email == "error@example.com") {
                NetworkResult.Error("ログインに失敗しました: 不明なエラーが発生しました。")
            } else {
                NetworkResult.Error("ログインに失敗しました: メールアドレスまたはパスワードが違います")
            }
        }
    }

    // ★登録処理のモックメソッドを追加★
    suspend fun register(email: String, password: String): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            delay(1500) // 登録はログインより少し時間がかかると仮定

            if (email.endsWith("@example.com") && password.length >= 6) {
                // 有効なメール形式でパスワードが6文字以上なら成功と仮定
                NetworkResult.Success(User(id = "newuser_" + System.currentTimeMillis(), name = "新規ユーザー", email = email, token = "new_mock_token"))
            } else if (email == "duplicate@example.com") {
                // 特定のメールアドレスで重複エラーをシミュレート
                NetworkResult.Error("登録エラー: このメールアドレスは既に登録されています。")
            } else {
                NetworkResult.Error("登録エラー: 入力情報が無効です。")
            }
        }
    }

    suspend fun getUser(id: String): NetworkResult<User> {
        return safeApiCall { userService.getUser(id) }
    }

    suspend fun getUsers(): NetworkResult<List<User>> {
        return safeApiCall { userService.getUsers() }
    }

    // 汎用的なAPI呼び出しのラッパー関数
    private suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall.invoke()
                if (response.isSuccessful) {
                    response.body()?.let {
                        NetworkResult.Success(it)
                    } ?: NetworkResult.Error("API response body is null")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    NetworkResult.Error("API Error: ${response.code()} - $errorBody")
                }
            } catch (e: HttpException) {
                NetworkResult.Error("Network Error: ${e.code()} - ${e.message()}")
            } catch (e: IOException) {
                NetworkResult.Error("Network Connection Error: ${e.message}")
            } catch (e: Exception) {
                NetworkResult.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }
}