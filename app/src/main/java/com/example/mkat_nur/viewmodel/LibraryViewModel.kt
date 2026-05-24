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
        viewModelScope.launch {
            repository.initializeBooksIfNeeded()
            loadAllBooks()
        }
    }

    /**
     * Başlangıçta tüm kitapları yükler.
     */
    private fun loadAllBooks() {
        viewModelScope.launch {
            repository.getAllBooks().collect {
                _books.value = it
                Log.d(TAG, "Kitaplar yüklendi: ${it.map { b -> b.slug }}")
            }
        }
        
        // Debug: Fihrist tablosundaki tüm benzersiz slug'ları logla
        viewModelScope.launch {
            try {
                val tables = repository.getAllTables()
                Log.d(TAG, "DEBUG: Veritabanındaki tüm tablolar: $tables")

                for (table in tables) {
                    Log.d(TAG, "DEBUG: Tablo bulundu: $table")
                }

                val uniqueSlugs = repository.getAllUniqueFihristSlugs()
                Log.d(TAG, "DEBUG: Fihrist tablosundaki tüm sluglar (Benzersiz): $uniqueSlugs")
                
                if (uniqueSlugs.size <= 1) {
                    Log.w(TAG, "Fihrist çok az! Sadece ${uniqueSlugs} var. JSON kitaplar için temel fihrist oluşturulabilir.")
                }
                
                // Kitaplar tablosunu kontrol et
                repository.getAllBooks().collect { bookList ->
                    Log.d(TAG, "DEBUG: Kitaplar tablosundaki tüm sluglar: ${bookList.map { it.slug }}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Debug hatası", e)
            }
        }
    }

    /**
     * Bir kitabın fihrist (içindekiler) bilgisini yükler.
     */
    fun loadSections(bookSlug: String) {
        Log.d(TAG, "Fihrist yükleniyor: slug=$bookSlug")
        viewModelScope.launch {
            repository.getSectionsByBook(bookSlug).collect { list ->
                Log.d(TAG, "VERİTABANINDAN GELEN LİSTE BOYUTU: ${list.size}")
                if (list.isNotEmpty()) {
                    Log.d(TAG, "İLK MADDE: ${list[0].title} (Sayfa: ${list[0].pageStart})")
                }
                
                if (list.size <= 1) {
                    Log.w(TAG, "Slug '$bookSlug' için az fihrist bulundu. Alternatifler aranıyor...")
                    val allUnique = repository.getAllUniqueFihristSlugs()
                    Log.d(TAG, "VERİTABANINDAKİ TÜM SLUGLAR: $allUnique")
                }
                
                _sections.value = list
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
                var page = repository.getPage(bookSlug, pageNumber)
                
                // Eğer sayfa bulunamadıysa (örneğin 1043 gibi uçuk bir rakam geldiyse)
                if (page == null) {
                    Log.w(TAG, "Sayfa $pageNumber bulunamadı, kitabın ilk sayfasını deniyorum...")
                    // Kitabın mevcut olan herhangi bir sayfasını (genelde ilk) getirmeyi dene
                    page = repository.getPage(bookSlug, 27) // Önce 27. sayfayı dene (Risale standardı)
                    if (page == null) {
                        page = repository.getPage(bookSlug, 1) // O da yoksa 1. sayfayı dene
                    }
                }

                if (page == null) {
                    Log.e(TAG, "SAYFA HİÇBİR ŞEKİLDE BULUNAMADI! Parametreler: slug='$bookSlug', page=$pageNumber")
                    _currentPage.value = Page("error", bookSlug, pageNumber, "İçerik bulunamadı (Slug: $bookSlug, S: $pageNumber). Lütfen kütüphaneden başka bir kitap seçin.", null, null)
                } else {
                    Log.d(TAG, "Sayfa başarıyla yüklendi: ID=${page.pageId}")
                    _currentPage.value = page
                    // Son okunan sayfayı veritabanına kaydet
                    repository.updateLastReadPage(bookSlug, page.pageNumber ?: pageNumber)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sayfa yüklenirken hata oluştu", e)
                _currentPage.value = Page("error", bookSlug, pageNumber, "Hata: ${e.message}", null, null)
            }
        }
    }

    /**
     * Sonraki sayfaya geçer.
     */
    fun nextPage() {
        val current = _currentPage.value ?: return
        val currentSlug = current.bookSlug ?: return
        val currentPageNum = current.pageNumber ?: 0

        viewModelScope.launch {
            try {
                var nextPage: Page? = null
                
                // 1. Yol: ID bazlı geçişi dene (Veritabanı zinciri)
                current.nextPage?.let { nextPageId ->
                    nextPage = repository.getPageById(nextPageId.toString())
                }
                
                // 2. Yol: Eğer zincir kopmuşsa sayfa numarasını 1 artırarak bulmayı dene
                if (nextPage == null) {
                    nextPage = repository.getPage(currentSlug, currentPageNum + 1)
                }
                
                nextPage?.let { next ->
                    _currentPage.value = next
                    repository.updateLastReadPage(currentSlug, next.pageNumber ?: (currentPageNum + 1))
                } ?: run {
                    Log.e(TAG, "Sonraki sayfa bulunamadı!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Geçiş hatası", e)
            }
        }
    }

    /**
     * Önceki sayfaya geçer.
     */
    fun prevPage() {
        val current = _currentPage.value ?: return
        val currentSlug = current.bookSlug ?: return
        val currentPageNum = current.pageNumber ?: 0

        viewModelScope.launch {
            try {
                var prevPage: Page? = null
                
                // 1. Yol: ID bazlı geçiş
                current.prevPage?.let { prevPageId ->
                    prevPage = repository.getPageById(prevPageId.toString())
                }
                
                // 2. Yol: Sayfa numarasını 1 eksilterek bul
                if (prevPage == null && currentPageNum > 1) {
                    prevPage = repository.getPage(currentSlug, currentPageNum - 1)
                }
                
                prevPage?.let { prev ->
                    _currentPage.value = prev
                    repository.updateLastReadPage(currentSlug, prev.pageNumber ?: (currentPageNum - 1))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Geri gitme hatası", e)
            }
        }
    }

    /**
     * Tıklanan kelimenin anlamını sözlükten bulur.
     */
    fun searchWordMeaning(word: String) {
        val cleanWord = word.trim().lowercase()
            .replace("[\".?,!]".toRegex(), "") // Noktalama işaretlerini temizle
        
        viewModelScope.launch {
            val meaning = repository.getWordMeaning(cleanWord)
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
