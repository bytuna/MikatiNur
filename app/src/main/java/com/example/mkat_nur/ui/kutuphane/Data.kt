package com.example.mkat_nur.ui.kutuphane

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Bir kitap: kapak resmi ve metin dosyası assets klasöründeki yollardır. */
data class Book(
    val id: String,
    val title: String,
    val coverPath: String?,
    val textPath: String?,
)

/** Bir raf: başlık + kitaplar. */
data class Shelf(
    val key: String,
    val title: String,
    val books: List<Book>,
    val defaultOpen: Boolean = true,
    val hidden: Boolean = false,
)

/** Kitabın fihrist / bölüm ağacı öğesi. */
data class BookSection(
    val id: String,
    val title: String,
    val startPage: Int,
    val level: Int = 0
)

/** Hâşiye (dipnot) öğesi. */
data class Footnote(
    val pageNumber: Int,
    val symbol: String,
    val text: String
)

data class DictEntry(
    val word: String,
    val def: String
)

enum class MealType(val label: String) {
    AYET("Ayet-i Kerîme"),
    HADIS("Hadîs-i Şerif")
}

data class MealPart(
    val text: String,
    val type: MealType
)

data class SearchResultItem(
    val book: Book,
    val sectionTitle: String,
    val pageNumber: Int,
    val pageIndex: Int,
    val snippetAnnotated: AnnotatedString,
    val rawSnippet: String
)

data class NoteItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

object DictionaryRepository {
    private var normalizedMap: Map<String, DictEntry>? = null

    fun load(context: Context) {
        if (normalizedMap != null) return
        synchronized(this) {
            if (normalizedMap != null) return
            try {
                val jsonStr = context.assets.open("lugat/tr.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
                val jsonObj = JSONObject(jsonStr)
                val map = HashMap<String, DictEntry>(jsonObj.length())
                val keys = jsonObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = jsonObj.optJSONObject(key)
                    if (item != null) {
                        val word = item.optString("word", key)
                        val def = item.optString("def", "")
                        val normKey = normalizeKey(key)
                        if (normKey.isNotBlank() && !map.containsKey(normKey)) {
                            map[normKey] = DictEntry(word, def)
                        }
                    }
                }
                normalizedMap = map
            } catch (e: Exception) {
                normalizedMap = emptyMap()
            }
        }
    }

    /** Sayfa akışında takılmayı önlemek için O(1) sabit zamanlı hızlı kontrol */
    fun contains(context: Context, term: String): Boolean {
        load(context)
        val clean = normalizeKey(term)
        if (clean.isBlank()) return false
        return normalizedMap?.containsKey(clean) == true
    }

    /** Arama işlemi: hızlı hash eşleşmesi; dialog aramasında isteğe bağlı fallback arama */
    fun lookup(context: Context, term: String, isInteractiveSearch: Boolean = false): DictEntry? {
        load(context)
        val clean = normalizeKey(term)
        if (clean.isBlank()) return null
        val map = normalizedMap ?: return null

        // 1. O(1) Tam Hash Eşleşmesi (Anında yanıt)
        map[clean]?.let { return it }

        // 2. Yalnızca kullanıcı arama kutusuna yazdığında yavaş arama yap
        if (isInteractiveSearch) {
            return map.entries.firstOrNull { it.key == clean || it.key.startsWith(clean) }?.value
                ?: map.entries.firstOrNull { it.key.contains(clean) }?.value
        }

        return null
    }

    fun normalizeKey(term: String): String {
        return Repository.normalizeSearchText(term)
    }
}

object MealRepository {
    private var mealMap: Map<String, String>? = null

    fun load(context: Context) {
        if (mealMap != null) return
        synchronized(this) {
            if (mealMap != null) return
            val map = HashMap<String, String>()

            // 1. Load meal/tr.json
            try {
                val jsonStr = context.assets.open("meal/tr.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
                val jsonObj = JSONObject(jsonStr)
                val keys = jsonObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val v = jsonObj.optString(k, "")
                    if (v.isNotBlank()) map[k] = v
                }
            } catch (_: Exception) {}

            // 2. Load meal/tr_0.json .. tr_35.json
            for (i in 0..35) {
                try {
                    val str = context.assets.open("meal/tr_$i.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val arr = JSONArray(str)
                    for (j in 0 until arr.length()) {
                        val obj = arr.getJSONObject(j)
                        val id = obj.optString("id", "")
                        val meal = obj.optString("meal", "")
                        if (id.isNotBlank() && meal.isNotBlank() && !map.containsKey(id)) {
                            map[id] = meal
                        }
                    }
                } catch (_: Exception) {}
            }
            mealMap = map
        }
    }

    fun lookup(context: Context, idOrQuery: String): String? {
        load(context)
        val clean = idOrQuery.trim()
        if (clean.isBlank()) return null
        val map = mealMap ?: return null

        // Exact match by ID
        map[clean]?.let { return it }

        // Sadece rakamlardan oluşan ID kontrolü
        val digits = clean.filter { it.isDigit() }
        if (digits.isNotBlank()) {
            map[digits]?.let { return it }
        }

        // Metin içinde arama
        return map.entries.firstOrNull { it.value.contains(clean, ignoreCase = true) }?.value
    }

    fun detectMealType(mealText: String): MealType {
        if (mealText.isBlank()) return MealType.AYET

        val hadisKeywords = listOf(
            "Buharî", "Buhari", "Müslim", "Tirmizî", "Tirmizi", "İbni Mâce", "Ebû Dâvûd",
            "Nesâî", "Müsned", "Aclûnî", "Akilî", "Zehebî", "Taberânî", "Beyhakî",
            "Hadis", "Hadîs", "el-Hâkim", "el-Müstedrek", "el-Cevzî", "rivâyet", "rivayet"
        )

        val hasHadisKeyword = hadisKeywords.any { mealText.contains(it, ignoreCase = true) }
        return if (hasHadisKeyword) MealType.HADIS else MealType.AYET
    }

    /** Meâl metnini ayet ve hadis parçalarına böler */
    fun parseMealParts(mealText: String): List<MealPart> {
        if (mealText.isBlank()) return emptyList()

        // Tırnak içindeki ifadeleri kaynaklarıyla birlikte ayır
        val matches = Regex("\"[^\"]+\"\\s*[^\"\n]*").findAll(mealText).map { it.value.trim() }.filter { it.isNotBlank() }.toList()

        if (matches.size > 1) {
            return matches.map { partStr ->
                MealPart(partStr, detectMealType(partStr))
            }
        }

        // Eğer tırnak ile ayrılmamışsa satırlara böl
        val lines = mealText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        if (lines.size > 1) {
            return lines.map { line ->
                MealPart(line, detectMealType(line))
            }
        }

        return listOf(MealPart(mealText, detectMealType(mealText)))
    }

    fun getAllMeals(context: Context): Map<String, String> {
        load(context)
        return mealMap ?: emptyMap()
    }
}

object Repository {

    private val coverCache = LruCache<String, ImageBitmap>(48)
    private val pageCache = LruCache<String, List<BookPage>>(32)
    private val sectionCache = LruCache<String, List<BookSection>>(32)
    private val footnoteCache = LruCache<String, Map<Int, List<Footnote>>>(32)

    // Tefekkür Modu pastel renk paleti
    val tefekkurPalette = listOf(
        Color(0xFFE8E5F8), // Açık Lila
        Color(0xFFFDE8E4), // Açık Gül/Şeftali
        Color(0xFFE2F0D9), // Açık Yeşil
        Color(0xFFDEEBF7), // Açık Mavi
        Color(0xFFFFF2CC), // Açık Sarı/Krem
        Color(0xFFF2DCDB)  // Pudra Bej
    )

    val tefekkurDarkPalette = listOf(
        Color(0xFF3B2E4A), // Koyu Lila
        Color(0xFF4A2E2E), // Koyu Gül
        Color(0xFF2E402A), // Koyu Yeşil
        Color(0xFF2E3B4A), // Koyu Mavi
        Color(0xFF4A452E), // Koyu Sarı
        Color(0xFF423232)  // Koyu Bej
    )

    /** Türkçe karakterleri (ş, ğ, ç, ö, ü, ı, i) koruyan, şapkalı harfleri normalize eden hassas metin filtreleyici */
    fun normalizeSearchText(text: String): String {
        if (text.isEmpty()) return ""
        val lower = text.lowercase(Locale("tr", "TR"))
        val sb = StringBuilder(lower.length)
        for (i in 0 until lower.length) {
            when (val c = lower[i]) {
                'â' -> sb.append('a')
                'î' -> sb.append('i')
                'û' -> sb.append('u')
                '’', '\'', '-', '`', '~', '÷', '.', ',', ';', ':', '!', '?', '(', ')', '[', ']', '"' -> {} // Noktalama ve işaretleri atla
                else -> sb.append(c)
            }
        }
        return sb.toString().replace(Regex("\\s+"), " ").trim()
    }

    /** Tüm külliyat içinde tam metin araması (Kesin sayfa indeksi ve hatasız Türkçe eşleşme) */
    fun searchInAllBooks(context: Context, query: String, maxResults: Int = 120): List<SearchResultItem> {
        val rawQueryWords = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (rawQueryWords.isEmpty()) return emptyList()

        val normQueryWords = rawQueryWords.map { normalizeSearchText(it) }.filter { it.isNotBlank() }
        if (normQueryWords.isEmpty()) return emptyList()

        val library = loadLibrary(context)
        val allBooks = library.flatMap { it.books }.distinctBy { it.id }
        val results = ArrayList<SearchResultItem>()

        for (book in allBooks) {
            val pages = loadPages(context, book)
            var sections: List<BookSection>? = null

            for ((pageIdx, page) in pages.withIndex()) {
                val pageNum = page.number ?: (pageIdx + 1)
                val rawPageText = page.text
                val normPageText = normalizeSearchText(rawPageText)

                // Tam kelime/kök araması (Türkçe karakterler korunur)
                val matchesAll = normQueryWords.all { word -> normPageText.contains(word) }
                if (matchesAll) {
                    if (sections == null) sections = loadSections(context, book)
                    val currentSection = sections.lastOrNull { it.startPage <= pageNum }?.title ?: book.title
                    val annotatedSnippet = buildHighlightedSnippet(rawPageText, rawQueryWords)

                    results.add(
                        SearchResultItem(
                            book = book,
                            sectionTitle = currentSection,
                            pageNumber = pageNum,
                            pageIndex = pageIdx,
                            snippetAnnotated = annotatedSnippet,
                            rawSnippet = annotatedSnippet.text
                        )
                    )
                    if (results.size >= maxResults) return results
                }
            }
        }
        return results
    }

    private fun buildHighlightedSnippet(
        rawText: String,
        rawQueryWords: List<String>
    ): AnnotatedString {
        val cleanRawText = rawText.replace(Regex("\\|\\d+@"), "").replace("~", "").replace("&", "")
        val lowerRaw = cleanRawText.lowercase(Locale("tr", "TR"))

        var firstMatchIdx = -1
        var matchedWordLength = 0

        for (qw in rawQueryWords) {
            val normQw = normalizeSearchText(qw)
            if (normQw.isBlank()) continue

            var searchFrom = 0
            while (searchFrom < cleanRawText.length) {
                val idx = lowerRaw.indexOf(normQw[0], searchFrom)
                if (idx < 0) break

                val candidateSub = cleanRawText.substring(idx, (idx + normQw.length + 10).coerceAtMost(cleanRawText.length))
                if (normalizeSearchText(candidateSub).startsWith(normQw)) {
                    if (firstMatchIdx == -1 || idx < firstMatchIdx) {
                        firstMatchIdx = idx
                        matchedWordLength = normQw.length
                    }
                    break
                }
                searchFrom = idx + 1
            }
        }

        if (firstMatchIdx < 0) firstMatchIdx = 0

        val start = (firstMatchIdx - 35).coerceAtLeast(0)
        val end = (firstMatchIdx + matchedWordLength + 65).coerceAtMost(cleanRawText.length)

        val prefix = if (start > 0) "..." else ""
        val suffix = if (end < cleanRawText.length) "..." else ""
        val snippetContent = cleanRawText.substring(start, end).replace("\n", " ").trim()
        val fullSnippetText = prefix + snippetContent + suffix

        val lowerFullSnippet = fullSnippetText.lowercase(Locale("tr", "TR"))

        return buildAnnotatedString {
            append(fullSnippetText)

            for (qw in rawQueryWords) {
                val normQw = normalizeSearchText(qw)
                if (normQw.length < 2) continue

                var searchFrom = 0
                while (searchFrom < fullSnippetText.length) {
                    val idx = lowerFullSnippet.indexOf(normQw[0], searchFrom)
                    if (idx < 0) break

                    var matchEnd = idx
                    var normCount = 0
                    while (matchEnd < fullSnippetText.length && normCount < normQw.length) {
                        val normChar = normalizeSearchText(fullSnippetText[matchEnd].toString())
                        if (normChar.isNotEmpty()) {
                            normCount += normChar.length
                        }
                        matchEnd++
                    }

                    val candidateSub = fullSnippetText.substring(idx, matchEnd)
                    if (normalizeSearchText(candidateSub) == normQw) {
                        addStyle(
                            style = SpanStyle(
                                background = Color(0xFFFFD54F), // Amber/sarı vurgu
                                color = Color(0xFF3E2723),
                                fontWeight = FontWeight.Bold
                            ),
                            start = idx,
                            end = matchEnd
                        )
                        searchFrom = matchEnd
                    } else {
                        searchFrom = idx + 1
                    }
                }
            }
        }
    }

    fun getFihristShortName(bookId: String): String? {
        return when (bookId.lowercase().trim()) {
            "sozler" -> "sozlerf"
            "mektubat" -> "mektubatf"
            "lemalar" -> "lemalarf"
            "sualar" -> "sualarf"
            "asa-yi-musa" -> "asamusaf"
            "barla-lahikasi" -> "barlaf"
            "kastamonu-lahikasi" -> "kastamonuh"
            "emirdag-lahikasi-1" -> "emirdag1f"
            "emirdag-lahikasi-2" -> "emirdag2f"
            "isarat-ul-icaz" -> "isaratf"
            "mesnevi", "mesnevi-i-nuriye" -> "mesnevif"
            "sikke-i-tasdik-i-gaybi" -> "stgaybf"
            "muhakemat" -> "muhakematf"
            "iman-ve-kufur-muvazeneleri" -> "imankufurf"
            "tarihce-i-hayat" -> "tarihcef"
            "nurun-ilk-kapisi" -> "muhtelif-nurun-ilk-kapisif"
            "nokta-risalesi" -> "muhtelif-nokta-risalesif"
            "suaat-i-marifetin-nebi-asm" -> "muhtelif-suaatif"
            "rumuz" -> "muhtelif-rumuzf"
            "isarat" -> "muhtelif-isaratif"
            "sunuhat" -> "muhtelif-sunuhatf"
            "tuluat" -> "muhtelif-tuluatf"
            "hutuvat-i-sitte" -> "muhtelif-hutuvatif"
            "divan-i-harb-i-orfi" -> "muhtelif-divanif"
            "munazarat" -> "muhtelif-munazaratf"
            "hutbe-i-samiye" -> "muhtelif-hutbeif"
            else -> null
        }
    }

    fun getHasiyeShortName(bookId: String): String? {
        return when (bookId.lowercase().trim()) {
            "sozler" -> "sozlerh"
            "mektubat" -> "mektubath"
            "lemalar" -> "lemalarh"
            "sualar" -> "sualarh"
            "asa-yi-musa" -> "asamusah"
            "barla-lahikasi" -> "barlah"
            "kastamonu-lahikasi" -> "kastamonuh"
            "emirdag-lahikasi-1" -> "emirdag1h"
            "emirdag-lahikasi-2" -> "emirdag2h"
            "isarat-ul-icaz" -> "isarath"
            "mesnevi", "mesnevi-i-nuriye" -> "mesnevih"
            "sikke-i-tasdik-i-gaybi" -> "stgh"
            "muhakemat" -> "muhakemath"
            "iman-ve-kufur-muvazeneleri" -> "imankufurh"
            "tarihce-i-hayat" -> "tarihceh"
            "nurun-ilk-kapisi" -> "muhtelif-nurun-ilk-kapisih"
            "nokta-risalesi" -> "muhtelif-nokta-risalesih"
            "suaat-i-marifetin-nebi-asm" -> "muhtelif-suaatih"
            "rumuz" -> "muhtelif-rumuzh"
            "isarat" -> "muhtelif-isaratih"
            "sunuhat" -> "muhtelif-sunuhath"
            "tuluat" -> "muhtelif-tuluath"
            "hutuvat-i-sitte" -> "muhtelif-hutuvatih"
            "divan-i-harb-i-orfi" -> "muhtelif-divanih"
            "munazarat" -> "muhtelif-munazarath"
            "hutbe-i-samiye" -> "muhtelif-hutbeih"
            else -> null
        }
    }

    fun loadFootnotes(context: Context, book: Book): Map<Int, List<Footnote>> {
        footnoteCache.get(book.id)?.let { return it }
        val shortName = getHasiyeShortName(book.id) ?: return emptyMap()
        val path = "hasiye/$shortName.txt"
        val result = HashMap<Int, MutableList<Footnote>>()
        try {
            val content = context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
            content.lines().forEach { line ->
                val parts = line.split("|", limit = 3)
                if (parts.size == 3) {
                    val pNum = parts[0].trim().toIntOrNull()
                    val symbolRaw = parts[1].trim()
                    val symbol = symbolRaw.removePrefix("(").removeSuffix(")").ifBlank { "Hâşiye" }
                    val text = parts[2].trim()
                    if (pNum != null && text.isNotBlank()) {
                        result.getOrPut(pNum) { ArrayList() }.add(Footnote(pNum, symbol, text))
                    }
                }
            }
            footnoteCache.put(book.id, result)
        } catch (_: Exception) {}
        return result
    }

    /** Kitabın fihrist (bölümler) listesini okur */
    fun loadSections(context: Context, book: Book): List<BookSection> {
        sectionCache.get(book.id)?.let { return it }

        // 1. Asset fihrist/*.txt dosyasından ağaç yapısını oku
        val shortName = getFihristShortName(book.id)
        if (shortName != null) {
            try {
                val fihristPath = "fihrist/$shortName.txt"
                val content = context.assets.open(fihristPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
                val list = ArrayList<BookSection>()
                var index = 0
                content.lines().forEach { line ->
                    if (line.isNotBlank()) {
                        val level = line.takeWhile { it == '\t' }.length
                        val trimmed = line.trimStart('\t').trimStart('*').trim()
                        val parts = trimmed.split("|")
                        if (parts.size >= 2) {
                            val title = parts[0].trim()
                            val pagePart = parts[1].split("%")[0].trim()
                            val startPage = pagePart.toIntOrNull()
                            if (title.isNotBlank() && startPage != null) {
                                index++
                                list.add(BookSection("sec_$index", title, startPage, level))
                            }
                        }
                    }
                }
                if (list.isNotEmpty()) {
                    sectionCache.put(book.id, list)
                    return list
                }
            } catch (_: Exception) {}
        }

        // 2. Fallback: JSON'dan oku
        val possiblePaths = listOfNotNull(
            book.textPath,
            "books/${book.id}.json",
            "books/${book.id.lowercase()}.json"
        ).distinct()

        for (path in possiblePaths) {
            try {
                val content = context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
                val root = JSONObject(content)
                val sectionsArray = root.optJSONArray("sections")
                if (sectionsArray != null) {
                    val list = ArrayList<BookSection>()
                    for (i in 0 until sectionsArray.length()) {
                        val sObj = sectionsArray.getJSONObject(i)
                        val title = sObj.optString("title", sObj.optString("baslik", "Bölüm ${i + 1}"))
                        val startPage = sObj.optInt("startPage", 1)
                        list.add(BookSection("sec_$i", title, startPage, 0))
                    }
                    sectionCache.put(book.id, list)
                    return list
                }
            } catch (_: Exception) {}
        }
        return emptyList()
    }

    /** Kitabın sayfalarını hafızadaki önbellekten (LruCache) getirir veya hızlıca parse eder */
    fun loadPages(context: Context, book: Book): List<BookPage> {
        pageCache.get(book.id)?.let { return it }
        val rawText = loadText(context, book)
        val pages = PageParser.parse(rawText)
        pageCache.put(book.id, pages)
        return pages
    }

    /** Cümleleri bularak sırayla tefekkür renkleriyle boyar */
    fun applyTefekkurMode(rawText: String, isEnabled: Boolean, isNightMode: Boolean = false): AnnotatedString {
        if (!isEnabled || rawText.isBlank()) return AnnotatedString(rawText)

        val palette = if (isNightMode) tefekkurDarkPalette else tefekkurPalette
        val sentences = rawText.split(Regex("(?<=[.!?])\\s+"))
        return buildAnnotatedString {
            sentences.forEachIndexed { index, sentence ->
                val bg = palette[index % palette.size]
                val start = length
                append(sentence)
                if (index < sentences.size - 1) append(" ")
                val end = length

                addStyle(
                    style = SpanStyle(background = bg),
                    start = start,
                    end = end
                )
            }
        }
    }

    /** assets/library.json dosyasını okuyup raf listesini üretir (Son Okunanlar dinamik doldurulur). */
    fun loadLibrary(context: Context, prefs: Prefs? = null): List<Shelf> {
        return try {
            val json = context.assets.open("library.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val shelvesJson = JSONObject(json).getJSONArray("shelves")
            val shelves = List(shelvesJson.length()) { i ->
                val s = shelvesJson.getJSONObject(i)
                val booksJson = s.optJSONArray("books")
                val books = if (booksJson == null) {
                    emptyList()
                } else {
                    List(booksJson.length()) { j ->
                        val b = booksJson.getJSONObject(j)
                        Book(
                            id = b.getString("id"),
                            title = b.getString("title"),
                            coverPath = b.optString("cover").ifBlank { null },
                            textPath = b.optString("text").ifBlank { null },
                        )
                    }
                }
                Shelf(
                    key = s.optString("id").ifBlank { s.getString("title") },
                    title = s.getString("title"),
                    books = books,
                    defaultOpen = s.optBoolean("open", true),
                    hidden = s.optBoolean("hidden", false),
                )
            }.filterNot { it.hidden }

            if (prefs != null) {
                val allBooks = shelves.flatMap { it.books }.distinctBy { it.id }
                val recentIds = prefs.recentBookIds()
                val recentBooks = recentIds.mapNotNull { id -> allBooks.firstOrNull { it.id == id } }

                shelves.map { shelf ->
                    if (shelf.key == "son-okunanlar" || shelf.title == "Son Okunanlar") {
                        shelf.copy(books = recentBooks)
                    } else {
                        shelf
                    }
                }
            } else {
                shelves
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Kitap içeriğini okur (.txt veya .json ayrımını otomatik yapar). */
    fun loadText(context: Context, book: Book): String {
        val possiblePaths = listOfNotNull(
            book.textPath,
            "books/${book.id}.json",
            "books/${book.id.lowercase()}.json"
        ).distinct()

        for (path in possiblePaths) {
            try {
                val content = context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
                    .removePrefix("\uFEFF")
                    .replace("\r\n", "\n")
                    .replace('\r', '\n')

                if (path.endsWith(".json", ignoreCase = true) || content.trim().startsWith("{") || content.trim().startsWith("[")) {
                    return parseJsonToReadableText(content)
                } else {
                    return content
                }
            } catch (_: Exception) {
                // Sonraki yolu dene
            }
        }
        return "(Metin dosyası açılamadı: ${book.title})"
    }

    private fun parseJsonToReadableText(rawJson: String): String {
        val sb = StringBuilder()
        try {
            val trimmed = rawJson.trim()
            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i)
                    if (obj != null) {
                        val title = obj.optString("title", obj.optString("baslik", ""))
                        val arabic = obj.optString("arabic", obj.optString("arapca", ""))
                        val text = obj.optString("text", obj.optString("icerik", obj.optString("content", "")))
                        val pageNo = obj.optInt("pageNumber", obj.optInt("sayfa", i + 1))
                        sb.append("#$pageNo\n")
                        if (title.isNotBlank()) sb.append("&${cleanInternalMarkup(title)}\n\n")
                        if (arabic.isNotBlank()) sb.append("${cleanInternalMarkup(arabic)}\n\n")
                        sb.append(cleanInternalMarkup(text)).append("\n\n")
                    } else {
                        sb.append("#${i + 1}\n").append(cleanInternalMarkup(array.optString(i))).append("\n\n")
                    }
                }
            } else {
                val root = JSONObject(trimmed)
                val pagesObj = root.optJSONObject("pages") ?: root.optJSONObject("sayfalar")
                val pagesArray = root.optJSONArray("pages") ?: root.optJSONArray("sayfalar")

                if (pagesObj != null) {
                    val sortedKeys = pagesObj.keys().asSequence()
                        .mapNotNull { it.toIntOrNull() }
                        .sorted()
                        .toList()

                    for (pNum in sortedKeys) {
                        val pageObj = pagesObj.optJSONObject(pNum.toString())
                        if (pageObj != null) {
                            val pageNo = pageObj.optInt("pageNumber", pNum)
                            val rawText = pageObj.optString("text", pageObj.optString("content", pageObj.optString("icerik", "")))
                            val clean = cleanInternalMarkup(rawText)
                            if (clean.isNotBlank()) {
                                sb.append("#$pageNo\n$clean\n\n")
                            }
                        }
                    }
                } else if (pagesArray != null) {
                    for (i in 0 until pagesArray.length()) {
                        val pageObj = pagesArray.optJSONObject(i)
                        if (pageObj != null) {
                            val pageNo = pageObj.optInt("pageNumber", pageObj.optInt("sayfa", i + 1))
                            val rawText = pageObj.optString("text", pageObj.optString("content", pageObj.optString("icerik", "")))
                            val clean = cleanInternalMarkup(rawText)
                            if (clean.isNotBlank()) {
                                sb.append("#$pageNo\n$clean\n\n")
                            }
                        } else {
                            val rawStr = pagesArray.optString(i)
                            val clean = cleanInternalMarkup(rawStr)
                            if (clean.isNotBlank()) {
                                sb.append("#${i + 1}\n$clean\n\n")
                            }
                        }
                    }
                } else {
                    val content = root.optString("content", root.optString("text", ""))
                    if (content.isNotBlank()) {
                        sb.append("#1\n${cleanInternalMarkup(content)}\n\n")
                    }
                }
            }
        } catch (e: Exception) {
            return cleanInternalMarkup(rawJson)
        }
        return sb.toString().trim()
    }

    private fun cleanInternalMarkup(raw: String): String {
        if (raw.isBlank()) return raw
        // ~ ve |\d+@ işaretlerini PageBlock'ta Meâl ID tespiti yapabilmek için koruyoruz.
        return raw
            .replace("÷", "")
            .replace("\\", "")
            .replace(">", "")
            .replace(",[", "[")
            .replace(",*", "*")
            .trim()
    }

    /** Kapak resmini yükler. */
    fun loadCover(context: Context, path: String?, targetWidthPx: Int): ImageBitmap? {
        if (path == null) return null
        coverCache.get(path)?.let { return it }
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= targetWidthPx) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bmp = context.assets.open(path).use { BitmapFactory.decodeStream(it, null, opts) }
                ?.asImageBitmap()
            if (bmp != null) coverCache.put(path, bmp)
            bmp
        } catch (e: Exception) {
            null
        }
    }
}

/** Raf açık/kapalı durumu, okuma konumu, yazı boyutu, temalar ve okuma tercihlerini saklar. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("kutuphane", Context.MODE_PRIVATE)

    fun position(bookId: String): Int = sp.getInt("page_$bookId", 0)
    fun setPosition(bookId: String, offset: Int) {
        sp.edit().putInt("page_$bookId", offset).apply()
    }

    var fontSp: Float
        get() = sp.getFloat("font_sp", 19f)
        set(value) {
            sp.edit().putFloat("font_sp", value).apply()
        }

    var textFontFamily: String
        get() = sp.getString("text_font_family", "lora") ?: "lora"
        set(value) {
            sp.edit().putString("text_font_family", value).apply()
        }

    var arabicFontFamily: String
        get() = sp.getString("arabic_font_family", "arabic_uthmantaha") ?: "arabic_uthmantaha"
        set(value) {
            sp.edit().putString("arabic_font_family", value).apply()
        }

    var tefekkurMode: Boolean
        get() = sp.getBoolean("tefekkur_mode", true)
        set(value) {
            sp.edit().putBoolean("tefekkur_mode", value).apply()
        }

    var themeMode: String
        get() = sp.getString("theme_mode", if (isNightMode) "night" else "day") ?: "day"
        set(value) {
            sp.edit().putString("theme_mode", value).apply()
            sp.edit().putBoolean("night_mode", value == "night").apply()
        }

    var pageColorTheme: String
        get() = sp.getString("page_color_theme", "gunduz") ?: "gunduz"
        set(value) {
            sp.edit().putString("page_color_theme", value).apply()
            val isNight = value == "gece"
            sp.edit().putBoolean("night_mode", isNight).apply()
            sp.edit().putString("theme_mode", value).apply()
        }

    var isNightMode: Boolean
        get() = sp.getBoolean("night_mode", false)
        set(value) {
            sp.edit().putBoolean("night_mode", value).apply()
            sp.edit().putString("theme_mode", if (value) "night" else "day").apply()
        }

    var libraryTheme: String
        get() = sp.getString("library_theme", "bal_mese") ?: "bal_mese"
        set(value) {
            sp.edit().putString("library_theme", value).apply()
        }

    var keepScreenOn: Boolean
        get() = sp.getBoolean("keep_screen_on", true)
        set(value) {
            sp.edit().putBoolean("keep_screen_on", value).apply()
        }

    fun getNotes(): List<NoteItem> {
        val raw = sp.getString("user_notes", "") ?: ""
        if (raw.isBlank()) return emptyList()
        val list = ArrayList<NoteItem>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    NoteItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.optString("title", "Not"),
                        content = obj.optString("content", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list.sortedByDescending { it.timestamp }
    }

    fun saveNote(title: String, content: String, id: String? = null) {
        val current = getNotes().toMutableList()
        if (id != null) {
            current.removeAll { it.id == id }
            current.add(0, NoteItem(id = id, title = title, content = content))
        } else {
            current.add(0, NoteItem(title = title, content = content))
        }
        saveNotesList(current)
    }

    fun deleteNote(id: String) {
        val current = getNotes().toMutableList()
        current.removeAll { it.id == id }
        saveNotesList(current)
    }

    private fun saveNotesList(notes: List<NoteItem>) {
        val arr = JSONArray()
        for (n in notes) {
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("title", n.title)
            obj.put("content", n.content)
            obj.put("timestamp", n.timestamp)
            arr.put(obj)
        }
        sp.edit().putString("user_notes", arr.toString()).apply()
    }

    fun exportBackupFile(context: Context): File? {
        return try {
            val jsonStr = createBackupJson(context)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(downloadsDir, "Risale_Kutuphanesi_Yedek_$dateStr.json")
            file.writeText(jsonStr, Charsets.UTF_8)
            file
        } catch (e: Exception) {
            null
        }
    }

    fun createBackupJson(context: Context): String {
        val library = Repository.loadLibrary(context)
        val allBooks = library.flatMap { it.books }.distinctBy { it.id }

        val positionsMap = HashMap<String, Int>()
        val bookmarksMap = HashMap<String, List<Int>>()
        val timesMap = HashMap<String, Long>()

        for (book in allBooks) {
            positionsMap[book.id] = position(book.id)
            val bm = bookmarks(book.id)
            if (bm.isNotEmpty()) bookmarksMap[book.id] = bm
            val t = readingTimeSeconds(book.id)
            if (t > 0) timesMap[book.id] = t
        }

        val prefsMap = mapOf(
            "font_sp" to fontSp.toString(),
            "text_font_family" to textFontFamily,
            "arabic_font_family" to arabicFontFamily,
            "tefekkur_mode" to tefekkurMode.toString(),
            "theme_mode" to themeMode,
            "page_color_theme" to pageColorTheme,
            "library_theme" to libraryTheme,
            "keep_screen_on" to keepScreenOn.toString()
        )

        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())
        root.put("recentBookIds", JSONArray(recentBookIds()))

        val posObj = JSONObject()
        positionsMap.forEach { (k, v) -> posObj.put(k, v) }
        root.put("positions", posObj)

        val bmObj = JSONObject()
        bookmarksMap.forEach { (k, v) -> bmObj.put(k, JSONArray(v)) }
        root.put("bookmarks", bmObj)

        val timesObj = JSONObject()
        timesMap.forEach { (k, v) -> timesObj.put(k, v) }
        root.put("readingTimes", timesObj)

        val notesArr = JSONArray()
        for (n in getNotes()) {
            val nObj = JSONObject()
            nObj.put("id", n.id)
            nObj.put("title", n.title)
            nObj.put("content", n.content)
            nObj.put("timestamp", n.timestamp)
            notesArr.put(nObj)
        }
        root.put("userNotes", notesArr)

        val pObj = JSONObject()
        prefsMap.forEach { (k, v) -> pObj.put(k, v) }
        root.put("preferences", pObj)

        return root.toString(2)
    }

    fun restoreBackupJson(context: Context, jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)

            // Positions
            val posObj = root.optJSONObject("positions")
            if (posObj != null) {
                val keys = posObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    setPosition(k, posObj.optInt(k, 0))
                }
            }

            // Bookmarks
            val bmObj = root.optJSONObject("bookmarks")
            if (bmObj != null) {
                val keys = bmObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val arr = bmObj.optJSONArray(k)
                    if (arr != null) {
                        val set = HashSet<String>()
                        for (i in 0 until arr.length()) {
                            set.add(arr.optInt(i).toString())
                        }
                        sp.edit().putStringSet("bm_$k", set).apply()
                    }
                }
            }

            // Reading Times
            val timesObj = root.optJSONObject("readingTimes")
            if (timesObj != null) {
                val keys = timesObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    sp.edit().putLong("time_$k", timesObj.optLong(k, 0L)).apply()
                }
            }

            // Recents
            val recArr = root.optJSONArray("recentBookIds")
            if (recArr != null) {
                val recList = ArrayList<String>()
                for (i in 0 until recArr.length()) {
                    recList.add(recArr.optString(i))
                }
                sp.edit().putString("recent_books", recList.joinToString(",")).apply()
            }

            // User Notes
            val notesArr = root.optJSONArray("userNotes")
            if (notesArr != null) {
                val restoredNotes = ArrayList<NoteItem>()
                for (i in 0 until notesArr.length()) {
                    val nObj = notesArr.optJSONObject(i)
                    if (nObj != null) {
                        restoredNotes.add(
                            NoteItem(
                                id = nObj.optString("id", java.util.UUID.randomUUID().toString()),
                                title = nObj.optString("title", "Not"),
                                content = nObj.optString("content", ""),
                                timestamp = nObj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }
                saveNotesList(restoredNotes)
            }

            // Preferences
            val pObj = root.optJSONObject("preferences")
            if (pObj != null) {
                fontSp = pObj.optDouble("font_sp", 19.0).toFloat()
                textFontFamily = pObj.optString("text_font_family", "lora")
                arabicFontFamily = pObj.optString("arabic_font_family", "arabic_uthmantaha")
                tefekkurMode = pObj.optBoolean("tefekkur_mode", true)
                themeMode = pObj.optString("theme_mode", "day")
                pageColorTheme = pObj.optString("page_color_theme", "gunduz")
                libraryTheme = pObj.optString("library_theme", "bal_mese")
                keepScreenOn = pObj.optBoolean("keep_screen_on", true)
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    fun shelfOpen(key: String, default: Boolean): Boolean = sp.getBoolean("shelf_open_$key", default)
    fun setShelfOpen(key: String, open: Boolean) {
        sp.edit().putBoolean("shelf_open_$key", open).apply()
    }

    fun readingTimeSeconds(bookId: String): Long = sp.getLong("time_$bookId", 0L)
    fun addReadingTime(bookId: String, seconds: Long) {
        val current = readingTimeSeconds(bookId)
        sp.edit().putLong("time_$bookId", current + seconds).apply()
    }

    fun recentBookIds(): List<String> {
        val raw = sp.getString("recent_books", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun addRecentBook(bookId: String) {
        val current = recentBookIds().toMutableList()
        current.remove(bookId)
        current.add(0, bookId)
        val trimmed = current.take(15)
        sp.edit().putString("recent_books", trimmed.joinToString(",")).apply()
    }

    fun removeRecentBook(bookId: String) {
        val current = recentBookIds().toMutableList()
        current.remove(bookId)
        sp.edit().putString("recent_books", current.joinToString(",")).apply()
    }

    fun isBookmarked(bookId: String, pageNumber: Int): Boolean {
        val set = sp.getStringSet("bm_$bookId", emptySet()) ?: emptySet()
        return set.contains(pageNumber.toString())
    }

    fun toggleBookmark(bookId: String, pageNumber: Int): Boolean {
        val set = (sp.getStringSet("bm_$bookId", emptySet()) ?: emptySet()).toMutableSet()
        val numStr = pageNumber.toString()
        val isAdded = if (set.contains(numStr)) {
            set.remove(numStr)
            false
        } else {
            set.add(numStr)
            true
        }
        sp.edit().putStringSet("bm_$bookId", set).apply()
        return isAdded
    }

    fun bookmarks(bookId: String): List<Int> {
        val set = sp.getStringSet("bm_$bookId", emptySet()) ?: emptySet()
        return set.mapNotNull { it.toIntOrNull() }.sorted()
    }
}