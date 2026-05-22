package com.example.mkat_nur.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sayfalar")
data class Page(
    @PrimaryKey @ColumnInfo(name = "page_id") val pageId: String,
    @ColumnInfo(name = "book_slug") val bookSlug: String?,
    @ColumnInfo(name = "page_number") val pageNumber: Int?,
    @ColumnInfo(name = "content_html") val contentHtml: String?,
    @ColumnInfo(name = "next_page") val nextPage: String?,
    @ColumnInfo(name = "prev_page") val prevPage: String?
)
