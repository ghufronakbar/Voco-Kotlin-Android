package com.example.voco.model

data class Product(
    val id: String,
    val qrCode: String,
    val brand: String,
    val variant:String,
    val expiredDate: String,
    val image: String?,
    val text: String
)
