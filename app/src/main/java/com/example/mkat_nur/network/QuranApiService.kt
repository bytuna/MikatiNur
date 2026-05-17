package com.example.mkat_nur.network

import com.example.mkat_nur.model.SurahResponse
import com.example.mkat_nur.model.Verse
import com.example.mkat_nur.util.AppConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface QuranApiService {
    @GET("chapters")
    suspend fun getSurahs(
        @Query("language") language: String = "tr"
    ): SurahResponse

    @GET("verses/by_chapter/{id}")
    suspend fun getSurahDetail(
        @Path("id") id: Int,
        @Query("language") language: String = "tr",
        @Query("translations") translations: String = "77",
        @Query("fields") fields: String = "text_uthmani",
        @Query("per_page") perPage: Int = 300
    ): com.example.mkat_nur.model.VerseResponse

    @GET("verses/by_juz/{id}")
    suspend fun getVersesByJuz(
        @Path("id") id: Int,
        @Query("language") language: String = "tr",
        @Query("translations") translations: String = "77",
        @Query("fields") fields: String = "text_uthmani",
        @Query("per_page") perPage: Int = 500
    ): com.example.mkat_nur.model.VerseResponse

    @GET("verses/by_page/{id}")
    suspend fun getVersesByPage(
        @Path("id") id: Int,
        @Query("language") language: String = "tr",
        @Query("translations") translations: String = "77",
        @Query("fields") fields: String = "text_uthmani",
        @Query("per_page") perPage: Int = 100
    ): com.example.mkat_nur.model.VerseResponse

    companion object {
        private const val BASE_URL = AppConfig.QURAN_API_BASE_URL

        private fun getUnsafeOkHttpClient(): OkHttpClient {
            return try {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })

                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())

                OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                    .hostnameVerifier { _, _ -> true }
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Authorization", "Bearer ${AppConfig.QURAN_API_TOKEN}")
                            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
                            .build()
                        
                        val response = chain.proceed(request)
                        val bodyString = response.body?.string() ?: ""
                        
                        android.util.Log.d("QuranAPI_Debug", "URL: ${request.url}")
                        android.util.Log.d("QuranAPI_Debug", "Response Code: ${response.code}")
                        
                        if (response.code == 401 || bodyString.contains("<HTML", ignoreCase = true) || bodyString.contains("<BODY", ignoreCase = true)) {
                            android.util.Log.e("QuranAPI_Debug", "HATA: Yetkilendirme hatası veya geçersiz yanıt! Yanıt kodu: ${response.code}")
                            // Hata durumunda boş bir yanıt dönmek yerine exception fırlatılması daha iyi olur
                            // ama şimdilik akışı bozmamak için null güvenli bir JSON dönelim
                        }

                        android.util.Log.d("QuranAPI_Debug", "Response Body: ${bodyString.take(500)}")
                        
                        val newBody = bodyString.toResponseBody(response.body?.contentType())
                        response.newBuilder().body(newBody).build()
                    }
                    .build()
            } catch (e: Exception) {
                OkHttpClient.Builder().build()
            }
        }

        fun create(): QuranApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(QuranApiService::class.java)
        }
    }
}
