package com.example.test.retrofit

// 例: UserRepository.kt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class UserRepository(private val userService: ApiService = RetrofitClient.instance.create(ApiService::class.java)) {

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