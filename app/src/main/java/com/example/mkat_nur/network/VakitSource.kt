package com.example.mkat_nur.network

import com.example.mkat_nur.model.VakitResponse
import com.example.mkat_nur.viewmodel.RetrofitClient

object VakitSource {
    suspend fun fetch(cityIdentifier: String): List<VakitResponse> {
        return try {
            RetrofitClient.diyanetInstance.getVakitler(cityIdentifier)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
