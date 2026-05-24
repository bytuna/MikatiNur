package com.example.mkat_nur.repository

import android.content.Context
import android.util.Log
import com.example.mkat_nur.data.local.dao.BookDao
import com.example.mkat_nur.data.local.dao.DictionaryDao
import com.example.mkat_nur.data.local.dao.PageDao
import com.example.mkat_nur.data.local.dao.SectionDao
import com.example.mkat_nur.data.local.entity.Book
import com.example.mkat_nur.data.local.entity.Dictionary
import com.example.mkat_nur.data.local.entity.Page
import com.example.mkat_nur.data.local.entity.Section
import com.example.mkat_nur.model.RisalePage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class LibraryRepository(
    private val context: Context,
    private val bookDao: BookDao,
    private val pageDao: PageDao,
    private val sectionDao: SectionDao,
    private val dictionaryDao: DictionaryDao
) {
    private val gson = Gson()

    /**
     * Kütüphane temizliği yapar ve kitapları başlatır.
     * Geçersiz veya mükerrer kitapları (Örn: ID 0 olan Sözler) siler.
     */
    suspend fun initializeBooksIfNeeded() = withContext(Dispatchers.IO) {
        try {
            val allBooks = listOf(
                Book("01", "Sözler", "sozler", 1014),
                Book("02", "Mektubat", "mektubat", 608),
                Book("03", "Lem'alar", "lemalar", 656),
                Book("04", "Şualar", "sualar", 752),
                Book("05", "Mesnevi-i Nuriye", "mesnevi-i-nuriye", 304),
                Book("06", "İşaratü'l-İcaz", "isaratul-icaz", 240),
                Book("07", "Asa-yı Musa", "asa-yi-musa", 272),
                Book("08", "Barla Lahikası", "barla-lahikasi", 448),
                Book("09", "Kastamonu Lahikası", "kastamonu-lahikasi", 256),
                Book("10", "Emirdağ Lahikası", "emirdag-lahikasi", 544),
                Book("11", "İman ve Küfür Muvazeneleri", "iman-ve-kufur-muvazeneleri", 256),
                Book("12", "Sikke-i Tasdik-i Gaybi", "sikke-i-tasdik-i-gaybi", 256),
                Book("13", "Muhakemat", "muhakemat", 192),
                Book("14", "Tarihçe-i Hayat", "tarihce-i-hayat", 736)
            )

            val correctIds = allBooks.map { it.id }

            // 1. Temizlik: Sadece yeni "01, 02" formatındaki ID'leri tut
            bookDao.keepOnlyTheseIds(correctIds)
            Log.d("LibraryRepository", "Eski formatlı veya geçersiz kitaplar temizlendi.")

            // 2. Güncelleme: Mevcut kitapları bozmadan eksikleri ekle
            val currentExistingIds = bookDao.getAllBooks().first().map { it.id }.toSet()
            val missingBooks = allBooks.filter { it.id !in currentExistingIds }
            
            if (missingBooks.isNotEmpty()) {
                bookDao.insertBooks(missingBooks)
                Log.d("LibraryRepository", "${missingBooks.size} yeni kitap eklendi.")
            }
            
            Log.d("LibraryRepository", "Kütüphane senkronizasyonu tamamlandı.")
        } catch (e: Exception) {
            Log.e("LibraryRepository", "Kitap başlatma hatası", e)
        }
    }

    /**
     * Tüm kitapları Flow olarak döndürür.
     */
    fun getAllBooks(): Flow<List<Book>> = bookDao.getAllBooks()

    /**
     * Slug değerine göre kitap bilgilerini getirir.
     */
    suspend fun getBookBySlug(slug: String): Book? = withContext(Dispatchers.IO) {
        bookDao.getBookBySlug(slug)
    }

    /**
     * Belirli bir kitabın tüm fihristini (bölümlerini) getirir.
     */
    fun getSectionsByBook(bookSlug: String): Flow<List<Section>> = sectionDao.getSectionsByBook(bookSlug)

    /**
     * Belirli bir kitabın sadece kök (üst) bölümlerini getirir.
     */
    fun getRootSectionsByBook(bookSlug: String): Flow<List<Section>> = sectionDao.getRootSectionsByBook(bookSlug)

    /**
     * Kitap slug ve sayfa numarasına göre sayfa içeriğini getirir.
     */
    suspend fun getPage(bookSlug: String, pageNumber: Int): Page? = withContext(Dispatchers.IO) {
        val dbPage = pageDao.getPage(bookSlug, pageNumber)
        if (dbPage != null) return@withContext dbPage

        // Veritabanında yoksa JSON'dan yükle ve önbelleğe al
        indexBook(bookSlug)
        return@withContext pageDao.getPage(bookSlug, pageNumber)
    }

    private suspend fun indexBook(bookSlug: String) = withContext(Dispatchers.IO) {
        try {
            val fileName = "risale/$bookSlug.json"
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<RisalePage>>() {}.type
            val risalePages: List<RisalePage> = gson.fromJson(json, listType)

            val roomPages = risalePages.map { 
                Page(
                    pageId = "${it.bookId}_${it.pageNumber}",
                    bookSlug = it.bookId,
                    pageNumber = it.pageNumber,
                    contentHtml = it.content,
                    nextPage = "${it.bookId}_${it.pageNumber + 1}",
                    prevPage = "${it.bookId}_${it.pageNumber - 1}"
                )
            }
            pageDao.insertPages(roomPages)
            Log.d("LibraryRepository", "Kitap indexlendi: $bookSlug, ${roomPages.size} sayfa.")
            
            // Eğer fihrist yoksa temel bir fihrist ekle
            val existingSections = sectionDao.getSectionsByBook(bookSlug).first()
            if (existingSections.isEmpty()) {
                val defaultSection = Section(
                    id = "${bookSlug}_root",
                    bookSlug = bookSlug,
                    title = "Kitap İçeriği",
                    slug = "root",
                    pageStart = 27,
                    pageCount = roomPages.size,
                    depth = 0,
                    parentId = null
                )
                sectionDao.insertSections(listOf(defaultSection))
                Log.d("LibraryRepository", "Temel fihrist oluşturuldu: $bookSlug")
            }
        } catch (e: Exception) {
            Log.e("LibraryRepository", "Kitap indexleme hatası: $bookSlug", e)
        }
    }

    /**
     * Kitabın son okunan sayfasını günceller.
     */
    suspend fun updateLastReadPage(slug: String, pageNumber: Int) = withContext(Dispatchers.IO) {
        bookDao.updateLastReadPage(slug, pageNumber)
    }

    /**
     * Sayfa ID'sine göre sayfa nesnesini getirir (Navigasyon için).
     */
    suspend fun getPageById(pageId: String): Page? = withContext(Dispatchers.IO) {
        pageDao.getPageById(pageId)
    }

    /**
     * Sözlükten kelime anlamı arar.
     */
    suspend fun getWordMeaning(word: String): Dictionary? = withContext(Dispatchers.IO) {
        dictionaryDao.getMeaning(word)
    }

    /**
     * Fihrist tablosundaki tüm benzersiz kitap slug'larını döndürür.
     */
    suspend fun getAllUniqueFihristSlugs(): List<String> = withContext(Dispatchers.IO) {
        sectionDao.getAllUniqueSlugs()
    }

    /**
     * Veritabanındaki tüm tabloları getirir (Debug).
     */
    fun getAllTables(): List<String> = emptyList()
}
