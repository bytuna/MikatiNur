package com.example.mkat_nur.network

import com.example.mkat_nur.model.SurahDetailResponse
import com.example.mkat_nur.model.SurahListResponse
import com.example.mkat_nur.util.AppConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface QuranApiService {
    @GET("quran/chapters")
    suspend fun getSurahs(
        @Header("Authorization") token: String
    ): SurahListResponse

    @GET("quran/surah/{id}")
    suspend fun getSurahDetail(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): SurahDetailResponse

    companion object {
        private const val BASE_URL = AppConfig.QURAN_API_BASE_URL

        fun create(): QuranApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(QuranApiService::class.java)
        }
    }
}
