package com.example.mkat_nur.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mkat_nur.model.Surah
import com.example.mkat_nur.model.Verse
import com.example.mkat_nur.network.QuranApiService
import com.example.mkat_nur.util.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class QuranUiState {
    object Loading : QuranUiState()
    data class Success(val surahs: List<Surah>) : QuranUiState()
    data class Error(val message: String) : QuranUiState()
}

sealed class SurahDetailUiState {
    object Idle : SurahDetailUiState()
    object Loading : SurahDetailUiState()
    data class Success(val verses: List<Verse>) : SurahDetailUiState()
    data class Error(val message: String) : SurahDetailUiState()
}

class QuranViewModel : ViewModel() {
    private val apiService = QuranApiService.create()
    private val apiToken = AppConfig.QURAN_API_TOKEN

    private val _uiState = MutableStateFlow<QuranUiState>(QuranUiState.Loading)
    val uiState: StateFlow<QuranUiState> = _uiState

    private val _detailUiState = MutableStateFlow<SurahDetailUiState>(SurahDetailUiState.Idle)
    val detailUiState: StateFlow<SurahDetailUiState> = _detailUiState

    init {
        fetchSurahs()
    }

    fun fetchSurahs() {
        viewModelScope.launch {
            _uiState.value = QuranUiState.Loading
            try {
                val response = apiService.getSurahs("Bearer $apiToken")
                _uiState.value = QuranUiState.Success(response.data)
            } catch (e: Exception) {
                _uiState.value = QuranUiState.Error(e.message ?: "Bir hata oluştu")
            }
        }
    }

    fun fetchSurahDetail(surahId: Int) {
        viewModelScope.launch {
            _detailUiState.value = SurahDetailUiState.Loading
            try {
                val response = apiService.getSurahDetail(surahId, "Bearer $apiToken")
                _detailUiState.value = SurahDetailUiState.Success(response.data.verses)
            } catch (e: Exception) {
                _detailUiState.value = SurahDetailUiState.Error(e.message ?: "Bir hata oluştu")
            }
        }
    }
}
