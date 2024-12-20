package com.example.voco.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    var retrofit: Retrofit? = null

    fun getClient(context: Context): Retrofit {
        val preferences: SharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val token = preferences.getString("ACCESS_TOKEN", null)
        val baseUrl = "https://voco-xi.vercel.app/api/"

        val httpClient = OkHttpClient.Builder()

        httpClient.addInterceptor { chain ->
            val originalRequest: Request = chain.request()

            val updatedToken = preferences.getString("ACCESS_TOKEN", null)
            val requestWithAuth = if (updatedToken != null) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $updatedToken")
                    .build()
            } else {
                originalRequest
            }

            chain.proceed(requestWithAuth)
        }

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit!!
    }
}
