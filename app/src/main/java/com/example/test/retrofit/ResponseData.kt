package com.example.test.retrofit

data class User(
    val id: String,
    val name: String,
    val email: String,
    val token: String
)

data class RecognitionResult(
    val name: String,
    val description: String
)

//data class ResponseData()
