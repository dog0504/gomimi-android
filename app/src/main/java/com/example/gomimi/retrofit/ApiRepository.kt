package com.example.gomimi.retrofit

// 例: UserRepository.kt

import android.util.Log
import com.example.gomimi.dataClass.Address
import com.example.gomimi.dataClass.AuthResponse
import com.example.gomimi.dataClass.BinDay
import com.example.gomimi.dataClass.GarbageIdentificationResponse
import com.example.gomimi.dataClass.History
import com.example.gomimi.dataClass.Language
import com.example.gomimi.dataClass.LoginRequestBody
import com.example.gomimi.dataClass.Manual
import com.example.gomimi.dataClass.ManualInfo
import com.example.gomimi.dataClass.RegisterRequestBody
import com.example.gomimi.dataClass.UpdateRequestBody
import com.example.gomimi.dataClass.User
import com.example.gomimi.dataClass.UserResponseBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.IOException

class UserRepository(private val userService: ApiService = RetrofitClient.instance.create(ApiService::class.java)) {
    // 本番仕様
    suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        return safeApiCall {
            val requestBody = LoginRequestBody(email, password)
            userService.login(requestBody)
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

    // ゴミ識別APIの画像アップロード処理
    suspend fun uploadImage(image: MultipartBody.Part, description: RequestBody? = null): NetworkResult<GarbageIdentificationResponse> {
        // safeApiCallが新しい型を扱えるようにする
        return safeApiCall { userService.uploadImage(image, description) }
    }

    // 新しい完全一致検索のメソッドを追加
    suspend fun searchManualExact(name: String): NetworkResult<Manual> { // List<>を外す
        return safeApiCall { userService.searchManualExact(name) }
    }

    suspend fun getLanguages(): NetworkResult<List<Language>> {
        return safeApiCall { userService.getLanguages() }
    }

    suspend fun searchAddress(postalCode: String): NetworkResult<List<Address>> {
        return safeApiCall { userService.searchAddress(postalCode) }
    }

    suspend fun registerUser(requestBody: RegisterRequestBody): NetworkResult<AuthResponse> {
        return safeApiCall { userService.registerUser(requestBody) }
    }

    suspend fun getBinDays(): NetworkResult<List<BinDay>> {
        return safeApiCall { userService.getBinDays() }
    }

    suspend fun getHistories(limit: Int = 20, offset: Int = 0): NetworkResult<List<History>> {
        return safeApiCall { userService.getHistories(limit, offset) }
    }

    suspend fun updateUserProfile(requestBody: UpdateRequestBody): NetworkResult<UserResponseBody> {
        return safeApiCall { userService.updateUserProfile(requestBody) }
    }

    suspend fun getAllManuals(): NetworkResult<List<ManualInfo>> {
        return safeApiCall { userService.getAllManuals() }
    }

    suspend fun searchManualsByKeyword(keyword: String): NetworkResult<List<ManualInfo>> {
        return safeApiCall { userService.searchManualsByKeyword(keyword) }
    }

    suspend fun searchManualsByInitial(initial: String): NetworkResult<List<ManualInfo>> {
        return safeApiCall { userService.searchManualsByInitial(initial) }
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
                    Log.e("ApiRepository", "API Error: ${response.code()} - $errorBody")
                    NetworkResult.Error("API Error: ${response.code()} - $errorBody")
                }
            } catch (e: HttpException) {
                Log.e("ApiRepository", "Network Error: ${e.code()} - ${e.message()}")
                NetworkResult.Error("Network Error: ${e.code()} - ${e.message()}")
            } catch (e: IOException) {
                Log.e("ApiRepository", "Network Connection Error: ${e.message}")
                NetworkResult.Error("Network Connection Error: ${e.message}")
            } catch (e: Exception) {
                Log.e("ApiRepository", "An unexpected error occurred: ${e.message}")
                NetworkResult.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    suspend fun getMyProfile(): NetworkResult<UserResponseBody> {
        return safeApiCall { userService.getMyProfile() }
    }
}