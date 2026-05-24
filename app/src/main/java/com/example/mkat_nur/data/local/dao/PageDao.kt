package com.example.mkat_nur.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mkat_nur.data.local.entity.Page
import kotlinx.coroutines.flow.Flow

@Dao
interface PageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPages(pages: List<Page>)

    @Query("""
        SELECT * FROM sayfalar 
        WHERE (LOWER(book_slug) = LOWER(:bookSlug) AND page_number = :pageNumber)
        OR (page_id = :bookSlug || '_' || :pageNumber)
        OR (page_id = CAST(:pageNumber AS TEXT))
        LIMIT 1
    """)
    fun getPage(bookSlug: String, pageNumber: Int): Page?

    @Query("SELECT * FROM sayfalar WHERE page_id = :pageId")
    fun getPageById(pageId: String): Page?

    @Query("SELECT * FROM sayfalar WHERE LOWER(book_slug) = LOWER(:bookSlug) ORDER BY page_number ASC")
    fun getPagesByBook(bookSlug: String): Flow<List<Page>>
}
