package com.example.test.retrofit

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<User>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>
}