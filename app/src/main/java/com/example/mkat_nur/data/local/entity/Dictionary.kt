package com.example.mkat_nur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sozluk")
data class Dictionary(
    @PrimaryKey val kelime: String,
    val anlam: String?
)
