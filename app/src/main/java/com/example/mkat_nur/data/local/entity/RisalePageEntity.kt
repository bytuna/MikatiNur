package com.example.mkat_nur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "risale_sayfalari")
data class RisalePageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val kitapAdi: String,
    val bolumAdi: String,
    val sayfaNo: Int,
    val sayfaIcerigi: String,
    val hasiyeler: Map<String, String>
)
