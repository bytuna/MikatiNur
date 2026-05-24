package com.example.mkat_nur.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fihrist")
data class Section(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "book_slug") val bookSlug: String?,
    val title: String?,
    val slug: String?,
    @ColumnInfo(name = "page_start") val pageStart: Int?,
    @ColumnInfo(name = "page_count") val pageCount: Int?,
    val depth: Int?,
    @ColumnInfo(name = "parent_id") val parentId: String?
)
