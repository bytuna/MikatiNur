package com.example.mkat_nur.ui.risale

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mkat_nur.data.local.AppDatabase
import com.example.mkat_nur.data.local.DatabaseInitializer
import com.example.mkat_nur.data.local.entity.RisalePageEntity
import com.example.mkat_nur.data.repository.RisaleRepository
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStreamReader

data class RisaleFihristItem(
    @SerializedName("seviye") val seviye: Int,
    @SerializedName("baslik") val baslik: String,
    @SerializedName("sayfa_no") val sayfaNo: Int
)

class RisaleViewModel(application: Application) : AndroidViewModel(application) {
    private val database: AppDatabase = AppDatabase.getDatabase(application)
    private val repository: RisaleRepository = RisaleRepository(database.risaleDao())
    private val prefs = application.getSharedPreferences("risale_reading_prefs", Context.MODE_PRIVATE)

    private val _currentPage = MutableStateFlow<RisalePageEntity?>(null)
    val currentPage: StateFlow<RisalePageEntity?> = _currentPage.asStateFlow()

    private val _isInitializing = MutableStateFlow(false)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    private val _fihrist = MutableStateFlow<List<RisaleFihristItem>>(emptyList())
    val fihrist: StateFlow<List<RisaleFihristItem>> = _fihrist.asStateFlow()

    private val _pendingScrollTarget = MutableStateFlow<String?>(null)
    val pendingScrollTarget: StateFlow<String?> = _pendingScrollTarget.asStateFlow()

    val bookNames: Flow<List<String>> = repository.getBookNames()

    init {
        viewModelScope.launch {
            _isInitializing.value = true
            DatabaseInitializer.initializeDatabase(application, database)
            _isInitializing.value = false
        }
    }

    fun loadPage(kitapAdi: String, sayfaNo: Int) {
        if (kitapAdi.isEmpty()) {
            _currentPage.value = null
            _fihrist.value = emptyList()
            return
        }
        viewModelScope.launch {
            val page = repository.getPage(kitapAdi, sayfaNo)
            if (page != null) {
                _currentPage.value = page
                // Son okunan sayfayı kaydet
                prefs.edit().putInt("last_page_$kitapAdi", sayfaNo).apply()
                // Eğer fihrist boşsa yükle
                if (_fihrist.value.isEmpty()) {
                    loadFihrist(kitapAdi)
                }
            }
        }
    }

    private fun loadFihrist(kitapAdi: String) {
        viewModelScope.launch {
            try {
                val assets = getApplication<Application>().assets
                
                // Manuel eşleştirme: Eğer otomatik eşleşme bozulursa buradan kesin yol veriyoruz
                val manualMap = mapOf(
                    "İşaratü'l-İcaz" to "07-İşaretu-l_İ-caz_fihrist.json",
                    "İşaratü’l-İ’caz" to "07-İşaretu-l_İ-caz_fihrist.json",
                    "Lem'alar" to "03-Lemalar_fihrist.json",
                    "Lem’alar" to "03-Lemalar_fihrist.json"
                )

                var fileName = manualMap[kitapAdi]

                if (fileName == null) {
                    val files = assets.list("database/fihrist") ?: emptyArray()
                    fun String.normalize(): String {
                        return this.lowercase()
                            .replace("i̇", "i")
                            .replace("ı", "i")
                            .replace("ü", "u")
                            .replace("ç", "c")
                            .replace("ş", "s")
                            .replace("ğ", "g")
                            .replace("ö", "o")
                            .replace(Regex("[^a-z0-9]"), "")
                    }
                    val normalizedTarget = kitapAdi.normalize()
                    fileName = files.find { 
                        val normalizedFile = it.replace(".json", "").replace("fihrist", "").normalize()
                        normalizedFile.contains(normalizedTarget) || normalizedTarget.contains(normalizedFile)
                    }
                }
                
                if (fileName != null) {
                    val inputStream = assets.open("database/fihrist/$fileName")
                    val reader = InputStreamReader(inputStream)
                    val listType = object : TypeToken<List<RisaleFihristItem>>() {}.type
                    val fihristData: List<RisaleFihristItem> = Gson().fromJson(reader, listType)
                    _fihrist.value = fihristData
                    reader.close()
                } else {
                    _fihrist.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _fihrist.value = emptyList()
            }
        }
    }

    fun getLastPage(kitapAdi: String): Int {
        val defaultStart = if (kitapAdi == "Asa-yı Musa") 5 else 6
        return prefs.getInt("last_page_$kitapAdi", defaultStart)
    }

    fun goToPage(sayfaNo: Int, targetBaslik: String? = null) {
        val current = _currentPage.value ?: return
        _pendingScrollTarget.value = targetBaslik
        loadPage(current.kitapAdi, sayfaNo)
    }

    fun clearScrollTarget() {
        _pendingScrollTarget.value = null
    }

    fun goToSection(bolumAdi: String) {
        val currentBook = _currentPage.value?.kitapAdi ?: return
        viewModelScope.launch {
            val pageNum = repository.getPageNumberBySection(currentBook, bolumAdi)
            if (pageNum != null) {
                loadPage(currentBook, pageNum)
            }
        }
    }

    fun nextPage() {
        val current = _currentPage.value ?: return
        loadPage(current.kitapAdi, current.sayfaNo + 1)
    }

    fun previousPage() {
        val current = _currentPage.value ?: return
        if (current.sayfaNo > 1) {
            loadPage(current.kitapAdi, current.sayfaNo - 1)
        }
    }
}
