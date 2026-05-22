package com.example.mkat_nur.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mkat_nur.data.local.entity.Section
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface SectionDao {
    @Query("SELECT * FROM fihrist WHERE LOWER(book_slug) = LOWER(:bookSlug) ORDER BY id ASC")
    fun getSectionsByBook(bookSlug: String): Flow<List<Section>>

    @Query("SELECT * FROM fihrist WHERE parent_id = :parentId ORDER BY id ASC")
    fun getSubSections(parentId: Int): Flow<List<Section>>
    
    @Query("SELECT * FROM fihrist WHERE LOWER(book_slug) = LOWER(:bookSlug) AND depth = 0 ORDER BY id ASC")
    fun getRootSectionsByBook(bookSlug: String): Flow<List<Section>>
}
