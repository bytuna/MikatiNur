package com.example.mkat_nur.network

import com.example.mkat_nur.model.PrayerResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface PrayerApiService {
    @GET("v1/timingsByCity")
    suspend fun getTimings(
        @Query("city") city: String,
        @Query("country") country: String = "Turkey",
        @Query("method") method: Int = 13 // 13 is Diyanet İşleri Başkanlığı
    ): PrayerResponse

    companion object {
        private const val BASE_URL = "https://api.aladhan.com/"

        fun create(): PrayerApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PrayerApiService::class.java)
        }
    }
}
