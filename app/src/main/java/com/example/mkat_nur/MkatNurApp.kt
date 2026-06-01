package com.example.mkat_nur

import android.app.Application
import com.example.mkat_nur.data.local.AppDatabase
import com.example.mkat_nur.data.repository.RisaleRepository

class MkatNurApp : Application() {
    
    val database by lazy { AppDatabase.getDatabase(this) }
    
    val risaleRepository by lazy { 
        RisaleRepository(database.risaleDao())
    }
}
