package com.example.mkat_nur.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mkat_nur.data.local.dao.DictionaryDao
import com.example.mkat_nur.data.local.dao.RisaleDao
import com.example.mkat_nur.data.local.entity.Dictionary
import com.example.mkat_nur.data.local.entity.RisalePageEntity
import com.example.mkat_nur.data.local.converter.MapConverter

@Database(
    entities = [RisalePageEntity::class, Dictionary::class],
    version = 6, // Versiyon yükseltildi
    exportSchema = false
)
@TypeConverters(MapConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun risaleDao(): RisaleDao
    abstract fun dictionaryDao(): DictionaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mikati_nur_risale.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
