package com.example.mkat_nur.data.local

import android.content.Context
import android.util.Log
import com.example.mkat_nur.data.local.entity.RisalePageEntity
import com.example.mkat_nur.data.local.model.RisaleJsonModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

object DatabaseInitializer {
    private const val TAG = "DatabaseInitializer"

    suspend fun initializeDatabase(context: Context, database: AppDatabase) {
        withContext(Dispatchers.IO) {
            val risaleDao = database.risaleDao()
            
            // Eğer zaten veri varsa tekrar yükleme
            // (Basit bir kontrol, gerçek senaryoda SharedPreferences veya sürüm kontrolü yapılabilir)
            // if (risaleDao.getBookNames().first().isNotEmpty()) return@withContext

            try {
                val assets = context.assets.list("risale") ?: return@withContext
                val gson = Gson()

                assets.filter { it.endsWith(".json") }.forEach { fileName ->
                    Log.d(TAG, "Yükleniyor: $fileName")
                    context.assets.open("risale/$fileName").use { inputStream ->
                        val reader = InputStreamReader(inputStream)
                        val model = gson.fromJson(reader, RisaleJsonModel::class.java)
                        
                        val entities = model.sayfalar.map { page ->
                            RisalePageEntity(
                                kitapAdi = model.kitapAdi,
                                bolumAdi = page.bolumAdi,
                                sayfaNo = page.sayfaNo,
                                sayfaIcerigi = page.sayfaIcerigi,
                                hasiyeler = page.sayfaHasiyeleri
                            )
                        }
                        risaleDao.insertAll(entities)
                    }
                }
                Log.d(TAG, "Veritabanı başarıyla başlatıldı.")
            } catch (e: Exception) {
                Log.e(TAG, "Veritabanı başlatılırken hata oluştu", e)
            }
        }
    }
}
