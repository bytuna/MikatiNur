package com.example.mkat_nur.network

import retrofit2.http.GET
import retrofit2.http.Path

interface NamazApi {
    @GET("sehirler/2") // 2 Diyanet'in Türkiye ID'si olabilir veya API'ye göre değişir
    suspend fun getSehirler(): List<SehirResponse>

    @GET("ilceler/{sehirId}")
    suspend fun getIlceler(@Path("sehirId") sehirId: String): List<IlceResponse>

    @GET("vakitler/{ilceId}")
    suspend fun getVakitler(@Path("ilceId") ilceId: String): List<DiyanetPrayerResponse>
}

data class SehirResponse(
    val SehirAdi: String,
    val SehirID: String
)

data class IlceResponse(
    val IlceAdi: String,
    val IlceID: String
)

data class DiyanetPrayerResponse(
    val Imsak: String,
    val Gunes: String,
    val Ogle: String,
    val Ikindi: String,
    val Aksam: String,
    val Yatsi: String,
    val MiladiTarihKisa: String,
    val HicriTarihKisa: String,
    val KibleSaati: String
)
