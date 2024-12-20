package com.example.voco.api

import com.example.voco.model.ApiRes
import com.example.voco.model.Product
import com.example.voco.model.ScanDTO

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {

    @GET("products")
    fun getAllProducts(): Call<ApiRes<List<Product>>>

    @POST("scan")
    fun scanQRCode(@Body requestBody: ScanDTO): Call<ApiRes<Product>>

}
