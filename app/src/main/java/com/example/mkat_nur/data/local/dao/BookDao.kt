package com.example.mkat_nur.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mkat_nur.data.local.entity.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM kitaplar ORDER BY id ASC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM kitaplar WHERE LOWER(slug) = LOWER(:slug) LIMIT 1")
    fun getBookBySlug(slug: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(books: List<Book>)

    @Query("DELETE FROM kitaplar WHERE id = :id")
    fun deleteBookById(id: String)

    @Query("DELETE FROM kitaplar WHERE id NOT IN (:ids)")
    fun keepOnlyTheseIds(ids: List<String>)

    @Query("UPDATE kitaplar SET last_read_page = :pageNumber WHERE LOWER(slug) = LOWER(:slug)")
    fun updateLastReadPage(slug: String, pageNumber: Int)
}
