package com.example.mkat_nur.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("body") val body: String,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("name") val name: String
)

interface GitHubApiService {
    @Headers("Cache-Control: no-cache, no-store", "Pragma: no-cache")
    @GET("https://raw.githubusercontent.com/bytuna/MikatiNur/master/update_info.json")
    suspend fun getLatestUpdateInfo(
        @Query("t") timestamp: Long = System.currentTimeMillis()
    ): GitHubRelease

    companion object {
        private const val BASE_URL = "https://api.github.com/"

        fun create(): GitHubApiService {
            val okHttpClient = OkHttpClient.Builder()
                .cache(null)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GitHubApiService::class.java)
        }
    }
}
