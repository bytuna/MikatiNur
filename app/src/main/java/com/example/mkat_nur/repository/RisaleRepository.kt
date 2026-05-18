package com.example.mkat_nur.repository

import android.content.Context
import com.example.mkat_nur.data.RisaleDbHelper
import com.example.mkat_nur.model.RisalePage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RisaleRepository(private val context: Context) {
    private val dbHelper = RisaleDbHelper(context)
    private val gson = Gson()

    /**
     * Sayfa içeriğini ve işaretli olup olmadığını getirir.
     */
    suspend fun getPage(bookId: String, pageNumber: Int): RisalePage = withContext(Dispatchers.IO) {
        val dbResult = dbHelper.getPage(bookId, pageNumber)
        if (dbResult != null) {
            return@withContext RisalePage(bookId, pageNumber, dbResult.first, dbResult.second)
        }

        val pagesFromAssets = loadPagesFromAssets(bookId)
        if (pagesFromAssets.isNotEmpty()) {
            dbHelper.insertPages(bookId, pagesFromAssets.map { it.pageNumber to it.content })
            val current = pagesFromAssets.find { it.pageNumber == pageNumber }
            return@withContext current?.copy(isBookmarked = false) ?: RisalePage(bookId, pageNumber, "Sayfa bulunamadı.")
        }

        return@withContext RisalePage(bookId, pageNumber, "Bu sayfa içeriği henüz eklenmemiştir.")
    }

    private fun loadPagesFromAssets(bookId: String): List<RisalePage> {
        return try {
            val fileName = "risale/$bookId.json"
            context.assets.open(fileName).bufferedReader().use { it.readText() }.let { json ->
                val listType = object : TypeToken<List<RisalePage>>() {}.type
                gson.fromJson(json, listType)
            }
        } catch (e: Exception) {
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

    suspend fun searchContent(query: String): List<RisalePage> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val results = mutableListOf<RisalePage>()
        
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
            null, null, null
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
