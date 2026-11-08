package com.example.mkat_nur.ui.quran1

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface AlQuranCloudApi {
    /**
     * Tek istekle çoklu edisyon:
     * GET /v1/surah/{surah}/editions/quran-uthmani,quran-tajweed,tr.transliteration,tr.diyanet,ar.alafasy
     */
    @GET("v1/surah/{surah}/editions/{editions}")
    suspend fun getSurahEditions(
        @Path("surah") surah: Int,
        @Path(value = "editions", encoded = true) editions: String
    ): AlQuranMultiResponse

    companion object {
        const val EDITION_UTHMANI = "quran-uthmani"
        const val EDITION_TAJWEED = "quran-tajweed"
        const val EDITION_TRANSLITERATION = "tr.transliteration"
        const val EDITION_MEAL = "tr.diyanet"
        const val EDITION_AUDIO = "ar.alafasy"

        val ALL_EDITIONS = listOf(
            EDITION_UTHMANI, EDITION_TAJWEED, EDITION_TRANSLITERATION, EDITION_MEAL, EDITION_AUDIO
        ).joinToString(",")

        /** Okunuş edisyonu bulunamazsa kullanılan liste. */
        val EDITIONS_WITHOUT_TRANSLITERATION = listOf(
            EDITION_UTHMANI, EDITION_TAJWEED, EDITION_MEAL, EDITION_AUDIO
        ).joinToString(",")

        val instance: AlQuranCloudApi by lazy {
            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            Retrofit.Builder()
                .baseUrl("https://api.alquran.cloud/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AlQuranCloudApi::class.java)
        }
    }
}
