package com.example.mkat_nur.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mkat_nur.model.RisaleBook
import com.example.mkat_nur.model.RisalePage
import com.example.mkat_nur.model.RisaleSection
import com.example.mkat_nur.model.Verse
import com.example.mkat_nur.repository.RisaleRepository
import com.example.mkat_nur.network.QuranApiService
import com.example.mkat_nur.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RisaleUiState {
    object Loading : RisaleUiState()
    data class Success(val books: List<RisaleBook>) : RisaleUiState()
    data class Error(val message: String) : RisaleUiState()
}

class RisaleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RisaleRepository(application)
    private val prefs = application.getSharedPreferences("risale_prefs", Context.MODE_PRIVATE)
    private val quranApi = QuranApiService.create()

    private val _uiState = MutableStateFlow<RisaleUiState>(RisaleUiState.Loading)
    val uiState: StateFlow<RisaleUiState> = _uiState.asStateFlow()

    private val _selectedBook = MutableStateFlow<RisaleBook?>(null)
    val selectedBook: StateFlow<RisaleBook?> = _selectedBook.asStateFlow()

    private val _currentPageData = MutableStateFlow<RisalePage?>(null)
    val currentPageData: StateFlow<RisalePage?> = _currentPageData.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _fontSize = MutableStateFlow(prefs.getFloat("risale_font_size", 18f))
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _lineSpacing = MutableStateFlow(prefs.getFloat("risale_line_spacing", 1.5f))
    val lineSpacing: StateFlow<Float> = _lineSpacing.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString("risale_theme", "Sepia") ?: "Sepia")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _notes = MutableStateFlow<List<String>>(emptyList())
    val notes: StateFlow<List<String>> = _notes.asStateFlow()

    private val _searchResults = MutableStateFlow<List<RisalePage>>(emptyList())
    val searchResults: StateFlow<List<RisalePage>> = _searchResults.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<RisalePage>>(emptyList())
    val bookmarks: StateFlow<List<RisalePage>> = _bookmarks.asStateFlow()

    private val _selectedVerse = MutableStateFlow<Verse?>(null)
    val selectedVerse: StateFlow<Verse?> = _selectedVerse.asStateFlow()

    init {
        loadBooks()
        loadAllBookmarks()
        indexAllBooksInBackground()
    }

    private fun indexAllBooksInBackground() {
        viewModelScope.launch {
            if (uiState.value is RisaleUiState.Success) {
                val books = (uiState.value as RisaleUiState.Success).books
                books.forEach { book ->
                    if (!repository.isBookIndexed(book.id)) {
                        repository.indexBook(book.id)
                    }
                }
            }
        }
    }

    private fun loadBooks() {
        val books = listOf(
            RisaleBook("sozler", "Sözler", pageCount = 1015, firstPage = 27, coverColor = 0xFF1B5E20, coverImageRes = R.drawable.sozler),
            RisaleBook("mektubat", "Mektubat", pageCount = 688, firstPage = 25, coverColor = 0xFF0D47A1, coverImageRes = R.drawable.mektubat),
            RisaleBook("lemalar", "Lem'alar", pageCount = 638, firstPage = 26, coverColor = 0xFF1B5E20, coverImageRes = R.drawable.lemalar),
            RisaleBook("sualar", "Şualar", pageCount = 901, firstPage = 23, coverColor = 0xFFB71C1C, coverImageRes = R.drawable.sualar),
            RisaleBook("asa-yi-musa", "Asâ-yı Musa", pageCount = 327, firstPage = 17, coverColor = 0xFFE65100, coverImageRes = R.drawable.asa_yi_musa),
            RisaleBook("mesnevi-i-nuriye", "Mesnevi-i Nuriye", pageCount = 323, firstPage = 15, coverColor = 0xFF004D40, coverImageRes = R.drawable.mesnevi_i_nuriye),
            RisaleBook("isaratul-icaz", "İşaratü'l-İ'caz", pageCount = 369, firstPage = 16, coverColor = 0xFF1B5E20, coverImageRes = R.drawable.isaratul_icaz),
            RisaleBook("barla-lahikasi", "Barla Lahikası", pageCount = 495, firstPage = 33, coverColor = 0xFF0D47A1, coverImageRes = R.drawable.barla_lahikasi),
            RisaleBook("kastamonu-lahikasi", "Kastamonu Lahikası", pageCount = 311, firstPage = 19, coverColor = 0xFF1B5E20, coverImageRes = R.drawable.kastamonu_lahikasi),
            RisaleBook("emirdag-lahikasi", "Emirdağ Lahikası", pageCount = 614, firstPage = 25, coverColor = 0xFFB71C1C, coverImageRes = R.drawable.emirdag_lahikasi),
            RisaleBook("iman-ve-kufur-muvazeneleri", "İman ve Küfür Muvazeneleri", pageCount = 237, firstPage = 17, coverColor = 0xFFE65100, coverImageRes = R.drawable.iman_ve_kufur_muvazeneleri),
            RisaleBook("sikke-i-tasdik-i-gaybi", "Sikke-i Tasdik-i Gaybi", pageCount = 349, firstPage = 15, coverColor = 0xFF004D40, coverImageRes = R.drawable.sikke_i_tasdik_i_gaybi),
            RisaleBook("tarihce-i-hayat", "Tarihçe-i Hayat", pageCount = 902, firstPage = 17, coverColor = 0xFF1B5E20, coverImageRes = R.drawable.tarihce_i_hayat),
            RisaleBook("muhakemat", "Muhakemat", pageCount = 170, firstPage = 15, coverColor = 0xFF0D47A1, coverImageRes = R.drawable.muhakemat)
        )
        _uiState.value = RisaleUiState.Success(books)
    }

    fun selectBook(book: RisaleBook?) {
        _selectedBook.value = book
        if (book != null) {
            val lastPage = prefs.getInt("last_page_${book.id}", book.firstPage)
            loadPage(book.id, lastPage)
        }
    }

    fun goToPage(pageNumber: Int) {
        val book = _selectedBook.value ?: return
        val lastPage = book.firstPage + book.pageCount - 1
        val validatedPage = pageNumber.coerceIn(book.firstPage, lastPage)
        loadPage(book.id, validatedPage)
    }

    fun loadPage(bookId: String, pageNumber: Int) {
        _currentPage.value = pageNumber
        prefs.edit().putInt("last_page_$bookId", pageNumber).apply()
        viewModelScope.launch {
            _currentPageData.value = repository.getPage(bookId, pageNumber)
            _notes.value = repository.getNotesForPage(bookId, pageNumber)
        }
    }

    fun toggleBookmark() {
        val current = _currentPageData.value ?: return
        viewModelScope.launch {
            val newState = repository.toggleBookmark(current.bookId, current.pageNumber)
            _currentPageData.value = current.copy(isBookmarked = newState)
            loadAllBookmarks()
        }
    }

    fun addNote(text: String) {
        val current = _currentPageData.value ?: return
        viewModelScope.launch {
            repository.saveNote(current.bookId, current.pageNumber, text)
            _notes.value = repository.getNotesForPage(current.bookId, current.pageNumber)
        }
    }

    fun deleteNote(text: String) {
        val current = _currentPageData.value ?: return
        viewModelScope.launch {
            repository.removeNote(current.bookId, current.pageNumber, text)
            _notes.value = repository.getNotesForPage(current.bookId, current.pageNumber)
        }
    }

    private fun loadAllBookmarks() {
        viewModelScope.launch {
            _bookmarks.value = repository.getAllBookmarks()
        }
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size
        prefs.edit().putFloat("risale_font_size", size).apply()
    }

    fun setLineSpacing(spacing: Float) {
        _lineSpacing.value = spacing
        prefs.edit().putFloat("risale_line_spacing", spacing).apply()
    }

    fun setTheme(theme: String) {
        _themeMode.value = theme
        prefs.edit().putString("risale_theme", theme).apply()
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        // Eğer sayıysa uzunluk sınırı yok, metinse en az 3 karakter
        if (query.toIntOrNull() == null && query.length < 3) return

        viewModelScope.launch {
            _searchResults.value = repository.searchContent(query)
        }
    }

    fun fetchVerseMeaning(verseKey: String) {
        viewModelScope.launch {
            try {
                val response = quranApi.getVerseByKey(verseKey)
                _selectedVerse.value = response.data
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearSelectedVerse() {
        _selectedVerse.value = null
    }

    fun getWordMeaning(word: String): String {
        // Gelişmiş temizleme: Noktalama işaretlerini ve ekleri temizle
        var cleanWord = word.lowercase().trim()
            .replace(Regex("[.,!?;:()\\[\\]\"]"), "") 
            
        if (cleanWord.contains("'")) {
            cleanWord = cleanWord.split("'")[0] 
        }

        // 1. Ayet kontrolü
        if (word.contains(":")) {
            val parts = word.split(":")
            if (parts.size == 2 && parts[0].toIntOrNull() != null && parts[1].toIntOrNull() != null) {
                fetchVerseMeaning(word)
                return "VERSE_MODE"
            }
        }

        // 2. Repository üzerinden geniş veritabanında arama
        return repository.getMeaningFromDictionary(cleanWord)
    }
}
