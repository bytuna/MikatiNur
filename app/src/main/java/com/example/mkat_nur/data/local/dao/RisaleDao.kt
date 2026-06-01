package com.example.mkat_nur.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mkat_nur.data.local.entity.RisalePageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RisaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pages: List<RisalePageEntity>)

    @Query("SELECT * FROM risale_sayfalari WHERE kitapAdi = :kitapAdi ORDER BY sayfaNo ASC")
    fun getPagesByBook(kitapAdi: String): Flow<List<RisalePageEntity>>

    @Query("SELECT * FROM risale_sayfalari WHERE kitapAdi = :kitapAdi AND sayfaNo = :sayfaNo LIMIT 1")
    suspend fun getPage(kitapAdi: String, sayfaNo: Int): RisalePageEntity?

    @Query("DELETE FROM risale_sayfalari")
    suspend fun deleteAll()

    @Query("SELECT DISTINCT kitapAdi FROM risale_sayfalari")
    fun getBookNames(): Flow<List<String>>

    @Query("SELECT sayfaNo FROM risale_sayfalari WHERE kitapAdi = :kitapAdi AND bolumAdi = :bolumAdi LIMIT 1")
    suspend fun getPageNumberBySection(kitapAdi: String, bolumAdi: String): Int?
}
