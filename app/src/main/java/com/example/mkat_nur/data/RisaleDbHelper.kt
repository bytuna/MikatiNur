package com.example.mkat_nur.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

@Deprecated("Yeni Room tabanlı yapıya geçildi. Lütfen AppDatabase kullanın.")
class RisaleDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "risale_manual.db"
        private const val DATABASE_VERSION = 5
        
        const val TABLE_NAME = "risale_pages"
        const val COLUMN_ID = "id"
        const val COLUMN_BOOK_ID = "bookId"
        const val COLUMN_PAGE_NUMBER = "pageNumber"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_IS_BOOKMARKED = "isBookmarked"

        const val TABLE_NOTES = "risale_notes"
        const val COLUMN_NOTE_TEXT = "noteText"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createPagesTable = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_BOOK_ID TEXT," +
                "$COLUMN_PAGE_NUMBER INTEGER," +
                "$COLUMN_CONTENT TEXT," +
                "$COLUMN_IS_BOOKMARKED INTEGER DEFAULT 0)")
        db.execSQL(createPagesTable)

        val createNotesTable = ("CREATE TABLE $TABLE_NOTES (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_BOOK_ID TEXT," +
                "$COLUMN_PAGE_NUMBER INTEGER," +
                "$COLUMN_NOTE_TEXT TEXT," +
                "$COLUMN_TIMESTAMP LONG)")
        db.execSQL(createNotesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertPages(bookId: String, pages: List<Pair<Int, String>>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (page in pages) {
                val values = ContentValues().apply {
                    put(COLUMN_BOOK_ID, bookId)
                    put(COLUMN_PAGE_NUMBER, page.first)
                    put(COLUMN_CONTENT, page.second)
                }
                db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getPage(bookId: String, pageNumber: Int): Pair<String, Boolean>? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_CONTENT, COLUMN_IS_BOOKMARKED),
            "$COLUMN_BOOK_ID = ? AND $COLUMN_PAGE_NUMBER = ?",
            arrayOf(bookId, pageNumber.toString()),
            null, null, null
        )
        
        var result: Pair<String, Boolean>? = null
        if (cursor.moveToFirst()) {
            val content = cursor.getString(0)
            val isBookmarked = cursor.getInt(1) == 1
            result = Pair(content, isBookmarked)
        }
        cursor.close()
        return result
    }

    fun toggleBookmark(bookId: String, pageNumber: Int): Boolean {
        val db = this.writableDatabase
        val current = getPage(bookId, pageNumber) ?: return false
        val newState = if (current.second) 0 else 1
        
        val values = ContentValues().apply {
            put(COLUMN_IS_BOOKMARKED, newState)
        }
        db.update(TABLE_NAME, values, "$COLUMN_BOOK_ID = ? AND $COLUMN_PAGE_NUMBER = ?", arrayOf(bookId, pageNumber.toString()))
        return newState == 1
    }

    fun getBookmarks(): List<Triple<String, Int, String>> {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_BOOK_ID, COLUMN_PAGE_NUMBER, COLUMN_CONTENT),
            "$COLUMN_IS_BOOKMARKED = 1",
            null, null, null, null
        )
        
        val results = mutableListOf<Triple<String, Int, String>>()
        if (cursor.moveToFirst()) {
            do {
                results.add(Triple(cursor.getString(0), cursor.getInt(1), cursor.getString(2)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return results
    }

    // Notes Methods
    fun addNote(bookId: String, pageNumber: Int, text: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_BOOK_ID, bookId)
            put(COLUMN_PAGE_NUMBER, pageNumber)
            put(COLUMN_NOTE_TEXT, text)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
        }
        db.insert(TABLE_NOTES, null, values)
    }

    fun getNotes(bookId: String, pageNumber: Int): List<String> {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NOTES,
            arrayOf(COLUMN_NOTE_TEXT),
            "$COLUMN_BOOK_ID = ? AND $COLUMN_PAGE_NUMBER = ?",
            arrayOf(bookId, pageNumber.toString()),
            null, null, "$COLUMN_TIMESTAMP DESC"
        )
        val notes = mutableListOf<String>()
        if (cursor.moveToFirst()) {
            do {
                notes.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notes
    }

    fun deleteNote(bookId: String, pageNumber: Int, text: String) {
        val db = this.writableDatabase
        db.delete(TABLE_NOTES, "$COLUMN_BOOK_ID = ? AND $COLUMN_PAGE_NUMBER = ? AND $COLUMN_NOTE_TEXT = ?", 
            arrayOf(bookId, pageNumber.toString(), text))
    }
}
