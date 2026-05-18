package com.example.mkat_nur.network

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

