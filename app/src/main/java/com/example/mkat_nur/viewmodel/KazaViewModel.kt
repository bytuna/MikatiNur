package com.example.mkat_nur.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class KazaViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("kaza_prefs", Context.MODE_PRIVATE)

    private val _fajrDebt = MutableStateFlow(prefs.getInt("fajr_debt", 0))
    val fajrDebt: StateFlow<Int> = _fajrDebt.asStateFlow()

    private val _dhuhrDebt = MutableStateFlow(prefs.getInt("dhuhr_debt", 0))
    val dhuhrDebt: StateFlow<Int> = _dhuhrDebt.asStateFlow()

    private val _asrDebt = MutableStateFlow(prefs.getInt("asr_debt", 0))
    val asrDebt: StateFlow<Int> = _asrDebt.asStateFlow()

    private val _maghribDebt = MutableStateFlow(prefs.getInt("maghrib_debt", 0))
    val maghribDebt: StateFlow<Int> = _maghribDebt.asStateFlow()

    private val _ishaDebt = MutableStateFlow(prefs.getInt("isha_debt", 0))
    val ishaDebt: StateFlow<Int> = _ishaDebt.asStateFlow()

    private val _witrDebt = MutableStateFlow(prefs.getInt("witr_debt", 0))
    val witrDebt: StateFlow<Int> = _witrDebt.asStateFlow()

    fun updateDebt(prayer: String, delta: Int) {
        when (prayer) {
            "fajr" -> {
                _fajrDebt.value = (_fajrDebt.value + delta).coerceAtLeast(0)
                prefs.edit().putInt("fajr_debt", _fajrDebt.value).apply()
            }
            "dhuhr" -> {
                _dhuhrDebt.value = (_dhuhrDebt.value + delta).coerceAtLeast(0)
                prefs.edit().putInt("dhuhr_debt", _dhuhrDebt.value).apply()
            }
            "asr" -> {
                _asrDebt.value = (_asrDebt.value + delta).coerceAtLeast(0)
                prefs.edit().putInt("asr_debt", _asrDebt.value).apply()
            }
            "maghrib" -> {
                _maghribDebt.value = (_maghribDebt.value + delta).coerceAtLeast(0)
                prefs.edit().putInt("maghrib_debt", _maghribDebt.value).apply()
            }
            "isha" -> {
                _ishaDebt.value = (_ishaDebt.value + delta).coerceAtLeast(0)
                prefs.edit().putInt("isha_debt", _ishaDebt.value).apply()
            }
            "witr" -> {
                _witrDebt.value = (_witrDebt.value + delta).coerceAtLeast(0)
                prefs.edit().putInt("witr_debt", _witrDebt.value).apply()
            }
        }
    }

    fun setDebt(prayer: String, total: Int) {
        when (prayer) {
            "fajr" -> {
                _fajrDebt.value = total.coerceAtLeast(0)
                prefs.edit().putInt("fajr_debt", _fajrDebt.value).apply()
            }
            "dhuhr" -> {
                _dhuhrDebt.value = total.coerceAtLeast(0)
                prefs.edit().putInt("dhuhr_debt", _dhuhrDebt.value).apply()
            }
            "asr" -> {
                _asrDebt.value = total.coerceAtLeast(0)
                prefs.edit().putInt("asr_debt", _asrDebt.value).apply()
            }
            "maghrib" -> {
                _maghribDebt.value = total.coerceAtLeast(0)
                prefs.edit().putInt("maghrib_debt", _maghribDebt.value).apply()
            }
            "isha" -> {
                _ishaDebt.value = total.coerceAtLeast(0)
                prefs.edit().putInt("isha_debt", _ishaDebt.value).apply()
            }
            "witr" -> {
                _witrDebt.value = total.coerceAtLeast(0)
                prefs.edit().putInt("witr_debt", _witrDebt.value).apply()
            }
        }
    }

    fun resetAll() {
        _fajrDebt.value = 0
        _dhuhrDebt.value = 0
        _asrDebt.value = 0
        _maghribDebt.value = 0
        _ishaDebt.value = 0
        _witrDebt.value = 0
        prefs.edit().clear().apply()
    }
}
