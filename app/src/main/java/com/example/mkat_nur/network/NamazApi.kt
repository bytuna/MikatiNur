package com.example.mkat_nur.network

import com.example.mkat_nur.model.VakitResponse
import retrofit2.http.GET
import retrofit2.http.Path

data class SehirResponse(
    val SehirAdi: String,
    val SehirID: String
)

data class IlceResponse(
    val IlceAdi: String,
    val IlceID: String
)

interface NamazApi {
    @GET("sehirler/2") // 2 = Türkiye
    suspend fun getSehirler(): List<SehirResponse>

    @GET("ilceler/{sehirId}")
    suspend fun getIlceler(@Path("sehirId") sehirId: String): List<IlceResponse>

    @GET("vakitler/{sehirId}")
    suspend fun getVakitler(@Path("sehirId") sehirId: String): List<VakitResponse>
}
