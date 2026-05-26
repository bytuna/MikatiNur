package com.example.mkat_nur.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mkat_nur.data.local.entity.Section
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSections(sections: List<Section>)

    @Query("SELECT * FROM fihrist WHERE LOWER(book_slug) = LOWER(:bookSlug) ORDER BY id ASC")
    fun getSectionsByBook(bookSlug: String): Flow<List<Section>>

    @Query("SELECT * FROM fihrist WHERE parent_id = :parentId ORDER BY id ASC")
    fun getSubSections(parentId: String): Flow<List<Section>>
    
    @Query("SELECT * FROM fihrist WHERE LOWER(book_slug) = LOWER(:bookSlug) AND depth = 0 ORDER BY id ASC")
    fun getRootSectionsByBook(bookSlug: String): Flow<List<Section>>

    @Query("SELECT DISTINCT book_slug FROM fihrist")
    fun getAllUniqueSlugs(): List<String>

    @Query("DELETE FROM fihrist WHERE LOWER(book_slug) = LOWER(:bookSlug)")
    fun deleteSectionsByBook(bookSlug: String)
}
