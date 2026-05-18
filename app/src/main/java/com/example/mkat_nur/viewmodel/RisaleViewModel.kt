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

    private val _themeMode = MutableStateFlow(prefs.getString("risale_theme", "Sepia") ?: "Sepia")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _searchResults = MutableStateFlow<List<RisalePage>>(emptyList())
    val searchResults: StateFlow<List<RisalePage>> = _searchResults.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<RisalePage>>(emptyList())
    val bookmarks: StateFlow<List<RisalePage>> = _bookmarks.asStateFlow()

    private val _selectedVerse = MutableStateFlow<Verse?>(null)
    val selectedVerse: StateFlow<Verse?> = _selectedVerse.asStateFlow()

    init {
        loadBooks()
        loadAllBookmarks()
    }

    private fun loadBooks() {
        val books = listOf(
            RisaleBook("sozler", "Sözler", pageCount = 791, coverColor = 0xFF1B5E20, coverImageRes = R.drawable.sozler, sections = listOf(
                RisaleSection("Birinci Söz", 1),
                RisaleSection("İkinci Söz", 10),
                RisaleSection("Üçüncü Söz", 15)
            )),
            RisaleBook("mektubat", "Mektubat", pageCount = 524, coverColor = 0xFF0D47A1, coverImageRes = R.drawable.mektubat),
            RisaleBook("lemalar", "Lem'alar", pageCount = 447, coverColor = 0xFF1B5E20, coverImageRes = R.drawable.lemalar),
            RisaleBook("sualar", "Şualar", pageCount = 761, coverColor = 0xFFB71C1C, coverImageRes = R.drawable.sualar),
            RisaleBook("asa-yi-musa", "Asâ-yı Musa", pageCount = 269, coverColor = 0xFFE65100, coverImageRes = R.drawable.asa_yi_musa),
            RisaleBook("mesnevi-i-nuriye", "Mesnevi-i Nuriye", pageCount = 266, coverColor = 0xFF004D40, coverImageRes = R.drawable.mesnevi_i_nuriye),
            RisaleBook("isaratul-icaz", "İşaratü'l-İ'caz", pageCount = 229, coverColor = 0xFF1B5E20, coverImageRes = R.drawable.isaratul_icaz),
            RisaleBook("barla-lahikasi", "Barla Lahikası", pageCount = 382, coverColor = 0xFF0D47A1, coverImageRes = R.drawable.barla_lahikasi),
            RisaleBook("kastamonu-lahikasi", "Kastamonu Lahikası", pageCount = 266, coverColor = 0xFF1B5E20, coverImageRes = R.drawable.kastamonu_lahikasi),
            RisaleBook("emirdag-lahikasi", "Emirdağ Lahikası", pageCount = 535, coverColor = 0xFFB71C1C, coverImageRes = R.drawable.emirdag_lahikasi),
            RisaleBook("iman-ve-kufur-muvazeneleri", "İman ve Küfür Muvazeneleri", pageCount = 268, coverColor = 0xFFE65100, coverImageRes = R.drawable.iman_ve_kufur_muvazeneleri),
            RisaleBook("sikke-i-tasdik-i-gaybi", "Sikke-i Tasdik-i Gaybi", pageCount = 269, coverColor = 0xFF004D40, coverImageRes = R.drawable.sikke_i_tasdik_i_gaybi),
            RisaleBook("tarihce-i-hayat", "Tarihçe-i Hayat", pageCount = 739, coverColor = 0xFF1B5E20, coverImageRes = R.drawable.tarihce_i_hayat),
            RisaleBook("muhakemat", "Muhakemat", pageCount = 176, coverColor = 0xFF0D47A1, coverImageRes = R.drawable.muhakemat)
        )
        _uiState.value = RisaleUiState.Success(books)
    }

    fun selectBook(book: RisaleBook?) {
        _selectedBook.value = book
        if (book != null) {
            val lastPage = prefs.getInt("last_page_${book.id}", 1)
            loadPage(book.id, lastPage)
        }
    }

    fun loadPage(bookId: String, pageNumber: Int) {
        _currentPage.value = pageNumber
        prefs.edit().putInt("last_page_$bookId", pageNumber).apply()
        viewModelScope.launch {
            _currentPageData.value = repository.getPage(bookId, pageNumber)
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

    private fun loadAllBookmarks() {
        viewModelScope.launch {
            _bookmarks.value = repository.getAllBookmarks()
        }
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size
        prefs.edit().putFloat("risale_font_size", size).apply()
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
        val dictionary = mapOf(
            "mübarek" to "Bereketli, hayırlı, kutsal.",
            "nişan" to "Alâmet, işaret, iz.",
            "mevcut" to "Var olan, bulunan.",
            "zebun" to "Güçsüz, zayıf, aciz.",
            "asâ" to "Baston, değnek.",
            "mikyas" to "Ölçü, kıstas."
        )
        val cleanWord = word.lowercase().trim().replace(Regex("[^a-zA-ZğüşıöçĞÜŞİÖÇ]"), "")
        
        if (word.contains(":")) {
            val parts = word.split(":")
            if (parts.size == 2 && parts[0].toIntOrNull() != null && parts[1].toIntOrNull() != null) {
                fetchVerseMeaning(word)
                return "Ayet yükleniyor..."
            }
        }

        return dictionary[cleanWord] ?: "Bu kelimenin anlamı lügatte bulunamadı."
    }
}
