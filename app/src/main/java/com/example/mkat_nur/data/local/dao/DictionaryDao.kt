package com.example.mkat_nur.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mkat_nur.data.local.entity.Dictionary

@Dao
@JvmSuppressWildcards
interface DictionaryDao {
    @Query("SELECT * FROM sozluk WHERE kelime = :word LIMIT 1")
    suspend fun getMeaning(word: String): Dictionary?

    @Query("SELECT * FROM sozluk WHERE kelime LIKE :query || '%' LIMIT 10")
    suspend fun searchWords(query: String): List<Dictionary>
}
