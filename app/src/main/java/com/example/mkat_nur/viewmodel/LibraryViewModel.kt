package com.example.mkat_nur.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mkat_nur.data.local.entity.Book
import com.example.mkat_nur.data.local.entity.Dictionary
import com.example.mkat_nur.data.local.entity.Page
import com.example.mkat_nur.data.local.entity.Section
import com.example.mkat_nur.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import android.util.Log

class LibraryViewModel(private val repository: LibraryRepository) : ViewModel() {

    companion object {
        private const val TAG = "LibraryViewModel"
    }

    // ... (rest of the flows)
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _sections = MutableStateFlow<List<Section>>(emptyList())
    val sections: StateFlow<List<Section>> = _sections.asStateFlow()

    private val _currentPage = MutableStateFlow<Page?>(null)
    val currentPage: StateFlow<Page?> = _currentPage.asStateFlow()

    private val _wordMeaning = MutableStateFlow<Dictionary?>(null)
    val wordMeaning: StateFlow<Dictionary?> = _wordMeaning.asStateFlow()

    init {
        loadAllBooks()
    }

    /**
     * Başlangıçta tüm kitapları yükler.
     */
    private fun loadAllBooks() {
        viewModelScope.launch {
            repository.getAllBooks().collect {
                _books.value = it
            }
        }
    }

    /**
     * Bir kitabın fihrist (içindekiler) bilgisini yükler.
     */
    fun loadSections(bookSlug: String) {
        viewModelScope.launch {
            repository.getSectionsByBook(bookSlug).collect {
                _sections.value = it
            }
        }
    }

    /**
     * Belirli bir kitabın belirli bir sayfasını yükler.
     */
    fun loadPage(bookSlug: String, pageNumber: Int) {
        Log.d(TAG, "Sayfa yükleniyor: slug=$bookSlug, num=$pageNumber")
        viewModelScope.launch {
            try {
                val page = repository.getPage(bookSlug, pageNumber)
                if (page == null) {
                    Log.e(TAG, "SAYFA BULUNAMADI! DB'de bu kriterlere uygun kayıt yok: $bookSlug, $pageNumber")
                } else {
                    Log.d(TAG, "Sayfa başarıyla yüklendi: ID=${page.pageId}")
                }
                _currentPage.value = page
            } catch (e: Exception) {
                Log.e(TAG, "Sayfa yüklenirken hata oluştu", e)
            }
        }
    }

    /**
     * Sonraki sayfaya geçer.
     */
    fun nextPage() {
        val current = _currentPage.value ?: return
        current.nextPage?.let { nextPageId ->
            viewModelScope.launch {
                val nextPage = repository.getPageById(nextPageId.toString())
                _currentPage.value = nextPage
            }
        }
    }

    /**
     * Önceki sayfaya geçer.
     */
    fun prevPage() {
        val current = _currentPage.value ?: return
        current.prevPage?.let { prevPageId ->
            viewModelScope.launch {
                val prevPage = repository.getPageById(prevPageId.toString())
                _currentPage.value = prevPage
            }
        }
    }

    /**
     * Tıklanan kelimenin anlamını sözlükten bulur.
     */
    fun searchWordMeaning(word: String) {
        viewModelScope.launch {
            val meaning = repository.getWordMeaning(word)
            _wordMeaning.value = meaning
        }
    }

    /**
     * Sözlük penceresini kapatmak için anlamı temizler.
     */
    fun clearWordMeaning() {
        _wordMeaning.value = null
    }
}

/**
 * LibraryViewModel için Factory sınıfı.
 */
class LibraryViewModelFactory(private val repository: LibraryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
