package com.example.mkat_nur.repository

import android.content.Context
import android.util.Log
import com.example.mkat_nur.data.RisaleDbHelper
import com.example.mkat_nur.model.RisalePage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RisaleRepository(private val context: Context) {
    private val dbHelper = RisaleDbHelper(context)
    private val gson = Gson()
    private var dictionaryCache: Map<String, String>? = null

    fun getMeaningFromDictionary(word: String): String {
        if (dictionaryCache == null) {
            loadDictionary()
        }
        return dictionaryCache?.get(word) ?: ""
    }

    private fun loadDictionary() {
        try {
            val json = context.assets.open("risale/lugat.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, String>>() {}.type
            dictionaryCache = gson.fromJson(json, type)
            Log.d("RisaleRepository", "Loaded dictionary with ${dictionaryCache?.size} entries")
        } catch (e: Exception) {
            Log.e("RisaleRepository", "Error loading dictionary", e)
            dictionaryCache = emptyMap()
        }
    }

    suspend fun getPage(bookId: String, pageNumber: Int): RisalePage = withContext(Dispatchers.IO) {
        // 1. Önce veritabanından bak
        val dbResult = dbHelper.getPage(bookId, pageNumber)
        if (dbResult != null) {
            // Senior Not: İçerik boşsa veya çok kısaysa bir hata olabilir, asset'ten yüklemeyi dene
            if (dbResult.first.length > 10) {
                return@withContext RisalePage(bookId, pageNumber, dbResult.first, dbResult.second)
            }
        }

        // 2. Veritabanında yoksa Asset'ten yükle ve veritabanına kaydet (Cache)
        indexBook(bookId)
        
        val newDbResult = dbHelper.getPage(bookId, pageNumber)
        if (newDbResult != null) {
            return@withContext RisalePage(bookId, pageNumber, newDbResult.first, newDbResult.second)
        }

        return@withContext RisalePage(bookId, pageNumber, "Bu sayfa içeriği yüklenemedi. Lütfen internet bağlantınızı kontrol edin veya verileri güncelleyin.")
    }

    suspend fun indexBook(bookId: String) = withContext(Dispatchers.IO) {
        val pagesFromAssets = loadPagesFromAssets(bookId)
        if (pagesFromAssets.isNotEmpty()) {
            dbHelper.insertPages(bookId, pagesFromAssets.map { it.pageNumber to it.content })
        }
    }

    suspend fun isBookIndexed(bookId: String): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${RisaleDbHelper.TABLE_NAME} WHERE ${RisaleDbHelper.COLUMN_BOOK_ID} = ?", arrayOf(bookId))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        count > 0
    }

    private fun loadPagesFromAssets(bookId: String): List<RisalePage> {
        return try {
            val fileName = "risale/$bookId.json"
            context.assets.open(fileName).bufferedReader().use { it.readText() }.let { json ->
                val listType = object : TypeToken<List<RisalePage>>() {}.type
                val pages: List<RisalePage> = gson.fromJson(json, listType)
                Log.d("RisaleRepository", "Loaded ${pages.size} pages for $bookId from assets")
                pages
            }
        } catch (e: Exception) {
            Log.e("RisaleRepository", "Error loading pages for $bookId", e)
            emptyList()
        }
    }

    suspend fun toggleBookmark(bookId: String, pageNumber: Int): Boolean = withContext(Dispatchers.IO) {
        dbHelper.toggleBookmark(bookId, pageNumber)
    }

    suspend fun getAllBookmarks(): List<RisalePage> = withContext(Dispatchers.IO) {
        dbHelper.getBookmarks().map { 
            RisalePage(it.first, it.second, it.third, true)
        }
    }

    suspend fun getNotesForPage(bookId: String, pageNumber: Int): List<String> = withContext(Dispatchers.IO) {
        dbHelper.getNotes(bookId, pageNumber)
    }

    suspend fun saveNote(bookId: String, pageNumber: Int, text: String) = withContext(Dispatchers.IO) {
        dbHelper.addNote(bookId, pageNumber, text)
    }

    suspend fun removeNote(bookId: String, pageNumber: Int, text: String) = withContext(Dispatchers.IO) {
        dbHelper.deleteNote(bookId, pageNumber, text)
    }

    suspend fun searchContent(query: String): List<RisalePage> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val results = mutableListOf<RisalePage>()
        
        // Profesyonel arama: Hem sayfa numarası hem içerik
        val selection = if (query.toIntOrNull() != null) {
            "${RisaleDbHelper.COLUMN_CONTENT} LIKE ? OR ${RisaleDbHelper.COLUMN_PAGE_NUMBER} = ?"
        } else {
            "${RisaleDbHelper.COLUMN_CONTENT} LIKE ?"
        }
        
        val selectionArgs = if (query.toIntOrNull() != null) {
            arrayOf("%$query%", query)
        } else {
            arrayOf("%$query%")
        }

        val cursor = db.query(
            RisaleDbHelper.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null, null, null, "50" // Limit results
        )

        if (cursor.moveToFirst()) {
            val bookIdIdx = cursor.getColumnIndex(RisaleDbHelper.COLUMN_BOOK_ID)
            val pageNumIdx = cursor.getColumnIndex(RisaleDbHelper.COLUMN_PAGE_NUMBER)
            val contentIdx = cursor.getColumnIndex(RisaleDbHelper.COLUMN_CONTENT)
            val bookmarkIdx = cursor.getColumnIndex(RisaleDbHelper.COLUMN_IS_BOOKMARKED)
            do {
                results.add(
                    RisalePage(
                        bookId = cursor.getString(bookIdIdx),
                        pageNumber = cursor.getInt(pageNumIdx),
                        content = cursor.getString(contentIdx),
                        isBookmarked = cursor.getInt(bookmarkIdx) == 1
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        results
    }
}
