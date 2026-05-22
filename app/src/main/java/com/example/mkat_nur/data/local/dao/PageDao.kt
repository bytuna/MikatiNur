package com.example.mkat_nur.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mkat_nur.data.local.entity.Page
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface PageDao {
    /**
     * Senior Sorgu: Hem slug/numara ikilisine bakar, hem de numara uyuşmazlığı varsa 
     * o numarayı ID (page_id) olarak kullanmayı dener.
     */
    @Query("""
        SELECT * FROM sayfalar 
        WHERE (LOWER(book_slug) = LOWER(:bookSlug) AND page_number = :pageNumber)
        OR (page_id = CAST(:pageNumber AS TEXT))
        LIMIT 1
    """)
    suspend fun getPage(bookSlug: String, pageNumber: Int): Page?

    @Query("SELECT * FROM sayfalar WHERE page_id = :pageId")
    suspend fun getPageById(pageId: String): Page?

    @Query("SELECT * FROM sayfalar WHERE LOWER(book_slug) = LOWER(:bookSlug) ORDER BY page_number ASC")
    fun getPagesByBook(bookSlug: String): Flow<List<Page>>
}
