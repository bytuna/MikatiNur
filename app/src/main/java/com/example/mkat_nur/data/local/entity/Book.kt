package com.example.mkat_nur.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kitaplar")
data class Book(
    @PrimaryKey val id: String,
    val title: String?,
    val slug: String?,
    @ColumnInfo(name = "page_count") val pageCount: Int?
)
