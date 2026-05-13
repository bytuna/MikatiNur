package com.example.mkat_nur.viewmodel

import com.example.mkat_nur.model.PrayerResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// API Arayüzü: Aladhan üzerinden Diyanet metodunu (13) kullanır
interface PrayerApiService {
    @GET("v1/timingsByCity")
    suspend fun getPrayerTimes(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int = 13
    ): PrayerResponse
}

// Retrofit İstemcisi: Unresolved Reference hatasını çözen nesne
object RetrofitClient {
    private const val BASE_URL = "https://api.aladhan.com/"

    val instance: PrayerApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(PrayerApiService::class.java)
    }
}