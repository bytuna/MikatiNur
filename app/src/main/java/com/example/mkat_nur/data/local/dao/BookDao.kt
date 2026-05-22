package com.example.mkat_nur.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mkat_nur.data.local.entity.Book
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface BookDao {
    @Query("SELECT * FROM kitaplar ORDER BY id ASC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM kitaplar WHERE LOWER(slug) = LOWER(:slug) LIMIT 1")
    suspend fun getBookBySlug(slug: String): Book?
}
