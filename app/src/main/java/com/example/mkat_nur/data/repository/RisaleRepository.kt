package com.example.mkat_nur.data.repository

import com.example.mkat_nur.data.local.dao.RisaleDao
import com.example.mkat_nur.data.local.entity.RisalePageEntity
import kotlinx.coroutines.flow.Flow

class RisaleRepository(private val risaleDao: RisaleDao) {
    fun getPagesByBook(kitapAdi: String): Flow<List<RisalePageEntity>> = risaleDao.getPagesByBook(kitapAdi)
    
    suspend fun getPage(kitapAdi: String, sayfaNo: Int): RisalePageEntity? = risaleDao.getPage(kitapAdi, sayfaNo)
    
    fun getBookNames(): Flow<List<String>> = risaleDao.getBookNames()

    suspend fun getPageNumberBySection(kitapAdi: String, bolumAdi: String): Int? = 
        risaleDao.getPageNumberBySection(kitapAdi, bolumAdi)
}
