package com.example.mkat_nur.repository

import android.app.Application
import com.example.mkat_nur.model.RisalePage

/**
 * Eski yapı için geçici repository (Yeni yapı data.repository altındadır)
 */
class RisaleRepository(private val application: Application) {
    suspend fun getPage(bookId: String, pageNumber: Int): RisalePage? = null
    suspend fun getNotesForPage(bookId: String, pageNumber: Int): List<String> = emptyList()
    suspend fun toggleBookmark(bookId: String, pageNumber: Int): Boolean = false
    suspend fun saveNote(bookId: String, pageNumber: Int, text: String) {}
    suspend fun removeNote(bookId: String, pageNumber: Int, text: String) {}
    suspend fun getAllBookmarks(): List<RisalePage> = emptyList()
    suspend fun searchContent(query: String): List<RisalePage> = emptyList()
    fun getMeaningFromDictionary(word: String): String = ""
    fun isBookIndexed(bookId: String): Boolean = true
    suspend fun indexBook(bookId: String) {}
}
