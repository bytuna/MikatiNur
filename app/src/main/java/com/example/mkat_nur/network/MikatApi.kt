package com.example.mkat_nur.network

import com.example.mkat_nur.model.VakitResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class NearestLocationResponse(
    @SerializedName("id", alternate = ["ilceId", "sehirId"]) val id: String = "",
    @SerializedName("name", alternate = ["ilceAdi", "sehirAdi"]) val name: String = "",
    @SerializedName("distanceKm", alternate = ["distance", "mesafe"]) val distanceKm: Double = 0.0
)

/** Ülke / il / ilçe listelerindeki tek bir satır. */
data class PlaceOption(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = ""
)

data class PlaceListResponse(
    @SerializedName("items") val items: List<PlaceOption> = emptyList()
)

interface MikatApi {
    @GET("nearest/")
    suspend fun nearest(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): NearestLocationResponse

    @GET("places/?type=countries")
    suspend fun countries(): PlaceListResponse

    @GET("places/?type=regions")
    suspend fun regions(@Query("country") countryId: String): PlaceListResponse

    @GET("places/?type=districts")
    suspend fun districts(@Query("region") regionId: String): PlaceListResponse

    /** Eski Diyanet servisiyle aynı alanlarda (hesaplanmış) vakit listesi. */
    @GET("compat/vakitler/")
    suspend fun vakitler(@Query("id") id: String): List<VakitResponse>
}
