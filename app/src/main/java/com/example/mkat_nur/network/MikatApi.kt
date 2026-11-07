package com.example.mkat_nur.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class NearestLocationResponse(
    @SerializedName("id", alternate = ["ilceId", "sehirId"]) val id: String = "",
    @SerializedName("name", alternate = ["ilceAdi", "sehirAdi"]) val name: String = "",
    @SerializedName("distanceKm", alternate = ["distance", "mesafe"]) val distanceKm: Double = 0.0
)

interface MikatApi {
    @GET("nearest")
    suspend fun nearest(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): NearestLocationResponse
}
