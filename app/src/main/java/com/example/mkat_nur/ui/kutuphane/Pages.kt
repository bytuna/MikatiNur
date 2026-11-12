package com.example.mkat_nur.ui.kutuphane

/** Okuyucudaki tek bir sayfa. [number] null ise numarasız bölümdür. */
data class BookPage(val number: Int?, val text: String)

/**
 * Hem .txt içindeki "#3", "#4" işaretlerini hem de JSON'dan çevrilen sayfaları algılar.
 */
object PageParser {
    private val marker = Regex("^[ \\t]*#(\\d+)[ \\t]*", RegexOption.MULTILINE)

    fun parse(text: String): List<BookPage> {
        val matches = marker.findAll(text).toList()
        if (matches.isEmpty()) return listOf(BookPage(null, text.trim()))

        val pages = ArrayList<BookPage>(matches.size + 1)
        val preamble = text.substring(0, matches[0].range.first).trim()
        if (preamble.isNotEmpty()) pages.add(BookPage(null, preamble))

        for ((i, m) in matches.withIndex()) {
            val start = m.range.last + 1
            val end = if (i + 1 < matches.size) matches[i + 1].range.first else text.length
            pages.add(BookPage(m.groupValues[1].toIntOrNull(), text.substring(start, end).trim()))
        }
        return pages
    }
}