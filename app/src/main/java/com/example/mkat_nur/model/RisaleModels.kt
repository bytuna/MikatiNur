package com.example.mkat_nur.model

data class RisaleBook(
    val id: String,
    val name: String,
    val pageCount: Int,
    val firstPage: Int,
    val coverColor: Long,
    val coverImageRes: Int
)

data class RisalePage(
    val bookId: String,
    val pageNumber: Int,
    val content: String = "",
    val isBookmarked: Boolean = false
)

data class RisaleSection(
    val id: String,
    val title: String,
    val startPage: Int
)
