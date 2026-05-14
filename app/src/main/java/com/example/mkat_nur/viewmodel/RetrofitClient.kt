package com.example.mkat_nur.viewmodel

import com.example.mkat_nur.model.PrayerResponse
import com.example.mkat_nur.network.NamazApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Eski API Arayüzü (Geri dönüş için korunuyor)
interface PrayerApiService {
    @GET("v1/timingsByCity")
    suspend fun getPrayerTimes(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int = 13
    ): PrayerResponse
}

object RetrofitClient {
    private const val ALADHAN_BASE_URL = "https://api.aladhan.com/"
    
    // Güvenilir ve ücretsiz Diyanet API Base URL'i
    private const val DIYANET_BASE_URL = "https://ezanvakti.emushaf.net/" 

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) MkatNur-Android/1.0")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    val aladhanInstance: PrayerApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ALADHAN_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PrayerApiService::class.java)
    }

    val diyanetInstance: NamazApi by lazy {
        Retrofit.Builder()
            .baseUrl(DIYANET_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NamazApi::class.java)
    }

    // Varsayılan olarak aladhanInstance'ı döndürür (Geriye dönük uyumluluk için)
    val instance: PrayerApiService get() = aladhanInstance
}
