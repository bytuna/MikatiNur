package com.example.mkat_nur.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.PrepackagedDatabaseCallback
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mkat_nur.data.local.dao.BookDao
import com.example.mkat_nur.data.local.dao.DictionaryDao
import com.example.mkat_nur.data.local.dao.PageDao
import com.example.mkat_nur.data.local.dao.SectionDao
import com.example.mkat_nur.data.local.entity.Book
import com.example.mkat_nur.data.local.entity.Dictionary
import com.example.mkat_nur.data.local.entity.Page
import com.example.mkat_nur.data.local.entity.Section

import android.util.Log

@Database(
    entities = [Book::class, Page::class, Dictionary::class, Section::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun pageDao(): PageDao
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun sectionDao(): SectionDao

    companion object {
        private const val TAG = "AppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nur_hazinesi_v4.db" // Sürüm 4 - Temiz Geçiş
                )
                    .createFromAsset("database/sozler_kitap.db", object : PrepackagedDatabaseCallback() {
                        override fun onOpenPrepackagedDatabase(db: SupportSQLiteDatabase) {
                            Log.i(TAG, "Senior Dönüşüm başlıyor (v2)...")
                            try {
                                // Kitaplar - Yeni kolon: last_read_page
                                db.execSQL("CREATE TABLE IF NOT EXISTS kitaplar_new (id TEXT NOT NULL PRIMARY KEY, title TEXT, slug TEXT, page_count INTEGER, last_read_page INTEGER)")
                                db.execSQL("INSERT OR REPLACE INTO kitaplar_new (id, title, slug, page_count, last_read_page) SELECT COALESCE(TRIM(CAST(id AS TEXT)), '0'), TRIM(CAST(title AS TEXT)), LOWER(TRIM(CAST(slug AS TEXT))), CAST(page_count AS INTEGER), 27 FROM kitaplar WHERE id IS NOT NULL")
                                db.execSQL("DROP TABLE IF EXISTS kitaplar")
                                db.execSQL("ALTER TABLE kitaplar_new RENAME TO kitaplar")

                                // Sayfalar
                                db.execSQL("CREATE TABLE IF NOT EXISTS sayfalar_new (page_id TEXT NOT NULL PRIMARY KEY, book_slug TEXT, page_number INTEGER, content_html TEXT, next_page TEXT, prev_page TEXT)")
                                db.execSQL("""
                                    INSERT OR REPLACE INTO sayfalar_new (page_id, book_slug, page_number, content_html, next_page, prev_page) 
                                    SELECT 
                                        TRIM(CAST(page_id AS TEXT)), 
                                        LOWER(TRIM(CAST(book_slug AS TEXT))), 
                                        CAST(page_number AS INTEGER), 
                                        CAST(content_html AS TEXT), 
                                        TRIM(CAST(next_page AS TEXT)), 
                                        TRIM(CAST(prev_page AS TEXT)) 
                                    FROM sayfalar 
                                    WHERE page_id IS NOT NULL
                                """.trimIndent())
                                db.execSQL("DROP TABLE IF EXISTS sayfalar")
                                db.execSQL("ALTER TABLE sayfalar_new RENAME TO sayfalar")

                                // Fihrist
                                db.execSQL("CREATE TABLE IF NOT EXISTS fihrist_new (id TEXT NOT NULL PRIMARY KEY, book_slug TEXT, title TEXT, slug TEXT, page_start INTEGER, page_count INTEGER, depth INTEGER, parent_id TEXT)")
                                db.execSQL("INSERT OR REPLACE INTO fihrist_new (id, book_slug, title, slug, page_start, page_count, depth, parent_id) SELECT TRIM(CAST(id AS TEXT)), LOWER(TRIM(CAST(book_slug AS TEXT))), TRIM(CAST(title AS TEXT)), TRIM(CAST(slug AS TEXT)), CAST(page_start AS INTEGER), CAST(page_count AS INTEGER), CAST(depth AS INTEGER), TRIM(CAST(parent_id AS TEXT)) FROM fihrist WHERE id IS NOT NULL")
                                db.execSQL("DROP TABLE IF EXISTS fihrist")
                                db.execSQL("ALTER TABLE fihrist_new RENAME TO fihrist")

                                // Sözlük
                                db.execSQL("CREATE TABLE IF NOT EXISTS sozluk_new (kelime TEXT NOT NULL PRIMARY KEY, anlam TEXT)")
                                db.execSQL("INSERT OR REPLACE INTO sozluk_new (kelime, anlam) SELECT TRIM(CAST(kelime AS TEXT)), CAST(anlam AS TEXT) FROM sozluk WHERE kelime IS NOT NULL")
                                db.execSQL("DROP TABLE IF EXISTS sozluk")
                                db.execSQL("ALTER TABLE sozluk_new RENAME TO sozluk")

                                Log.i(TAG, "Dönüşüm v1 tamamlandı.")
                            } catch (e: Exception) {
                                Log.e(TAG, "Dönüşüm hatası!", e)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
