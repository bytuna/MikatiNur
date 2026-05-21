package com.example.mkat_nur.network

import com.example.mkat_nur.model.Ilce
import com.example.mkat_nur.model.Sehir
import com.example.mkat_nur.model.VakitResponse
import com.example.mkat_nur.model.DiniGunResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface NamazApi {
    @GET("sehirler/2")
    suspend fun getSehirler(): List<Sehir>

    @GET("ilceler/{sehirId}")
    suspend fun getIlceler(@Path("sehirId") sehirId: String): List<Ilce>

    @GET("vakitler/{ilceId}")
    suspend fun getVakitler(@Path("ilceId") ilceId: String): List<VakitResponse>

    @GET("dini-gunler")
    suspend fun getDiniGunler(): List<DiniGunResponse>
}
