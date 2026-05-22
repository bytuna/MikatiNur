package com.example.mkat_nur.repository

import com.example.mkat_nur.data.local.dao.BookDao
import com.example.mkat_nur.data.local.dao.DictionaryDao
import com.example.mkat_nur.data.local.dao.PageDao
import com.example.mkat_nur.data.local.dao.SectionDao
import com.example.mkat_nur.data.local.entity.Book
import com.example.mkat_nur.data.local.entity.Dictionary
import com.example.mkat_nur.data.local.entity.Page
import com.example.mkat_nur.data.local.entity.Section
import kotlinx.coroutines.flow.Flow

class LibraryRepository(
    private val bookDao: BookDao,
    private val pageDao: PageDao,
    private val sectionDao: SectionDao,
    private val dictionaryDao: DictionaryDao
) {
    /**
     * Tüm kitapları Flow olarak döndürür.
     */
    fun getAllBooks(): Flow<List<Book>> = bookDao.getAllBooks()

    /**
     * Slug değerine göre kitap bilgilerini getirir.
     */
    suspend fun getBookBySlug(slug: String): Book? = bookDao.getBookBySlug(slug)

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
    suspend fun getPage(bookSlug: String, pageNumber: Int): Page? = pageDao.getPage(bookSlug, pageNumber)

    /**
     * Sayfa ID'sine göre sayfa nesnesini getirir (Navigasyon için).
     */
    suspend fun getPageById(pageId: String): Page? = pageDao.getPageById(pageId)

    /**
     * Sözlükten kelime anlamı arar.
     */
    suspend fun getWordMeaning(word: String): Dictionary? = dictionaryDao.getMeaning(word)

    /**
     * Kelime araması yapar (Arama önerileri için).
     */
    suspend fun searchDictionary(query: String): List<Dictionary> = dictionaryDao.searchWords(query)
}
