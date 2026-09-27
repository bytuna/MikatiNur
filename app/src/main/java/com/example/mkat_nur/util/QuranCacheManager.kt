package com.example.mkat_nur.util

import android.content.Context
import com.example.mkat_nur.model.SurahResponse
import com.example.mkat_nur.model.VerseResponse
import com.google.gson.Gson
import java.io.File

class QuranCacheManager(private val context: Context) {
    private val gson = Gson()
    private val cacheDir: File
        get() = File(context.filesDir, "quran_cache").apply { if (!exists()) mkdirs() }

    fun saveSurahs(surahResponse: SurahResponse) {
        try {
            val file = File(cacheDir, "surahs.json")
            val json = gson.toJson(surahResponse)
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSurahs(): SurahResponse? {
        try {
            val file = File(cacheDir, "surahs.json")
            if (file.exists()) {
                val json = file.readText()
                return gson.fromJson(json, SurahResponse::class.java)
            }
            // Fallback to assets/quran_surahs.json
            val assetJson = context.assets.open("quran_surahs.json").bufferedReader().use { it.readText() }
            return gson.fromJson(assetJson, SurahResponse::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun saveVerses(cacheKey: String, verseResponse: VerseResponse) {
        try {
            val file = File(cacheDir, "verses_$cacheKey.json")
            val json = gson.toJson(verseResponse)
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadVerses(cacheKey: String): VerseResponse? {
        try {
            val file = File(cacheDir, "verses_$cacheKey.json")
            if (file.exists()) {
                val json = file.readText()
                return gson.fromJson(json, VerseResponse::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
