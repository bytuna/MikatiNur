package com.example.mkat_nur

import android.app.Application
import com.example.mkat_nur.data.local.AppDatabase
import com.example.mkat_nur.repository.LibraryRepository

class MkatNurApp : Application() {
    
    val database by lazy { AppDatabase.getDatabase(this) }
    
    val libraryRepository by lazy { 
        LibraryRepository(
            this,
            database.bookDao(),
            database.pageDao(),
            database.sectionDao(),
            database.dictionaryDao()
        )
    }
}
