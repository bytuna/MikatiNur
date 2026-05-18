package com.example.mkat_nur.model

data class RisaleBook(
    val id: String,
    val name: String,
    val author: String = "Bediüzzaman Said Nursi",
    val pageCount: Int,
    val coverColor: Long,
    val coverImageRes: Int? = null,
    val sections: List<RisaleSection> = emptyList()
)

data class RisaleSection(
    val title: String,
    val pageNumber: Int
)

data class RisalePage(
    val bookId: String,
    val pageNumber: Int,
    val content: String,
    var isBookmarked: Boolean = false
)
