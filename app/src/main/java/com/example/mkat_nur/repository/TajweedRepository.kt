package com.example.mkat_nur.repository

import android.content.Context
import com.example.mkat_nur.model.AyahTajweedData
import com.example.mkat_nur.model.TajweedAnnotation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

object TajweedRepository {
    private var tajweedMap: Map<Pair<Int, Int>, List<TajweedAnnotation>>? = null

    suspend fun getAnnotations(
        context: Context,
        surahId: Int,
        ayahId: Int
    ): List<TajweedAnnotation> {
        val map = tajweedMap ?: loadTajweedData(context)
        return map[Pair(surahId, ayahId)] ?: emptyList()
    }

    private suspend fun loadTajweedData(
        context: Context
    ): Map<Pair<Int, Int>, List<TajweedAnnotation>> = withContext(Dispatchers.IO) {
        val cached = tajweedMap
        if (cached != null) return@withContext cached

        try {
            context.assets.open("tajweed_data.json").use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    val type = object : TypeToken<List<AyahTajweedData>>() {}.type
                    val list: List<AyahTajweedData> = Gson().fromJson(reader, type)

                    val map = list.associate { data ->
                        Pair(data.surah, data.ayah) to data.annotations
                    }
                    tajweedMap = map
                    map
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }
}
