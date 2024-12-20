package com.example.voco.model

data class ApiRes<T>(
    val success: Boolean,
    val message: String,
    val data: T
)
