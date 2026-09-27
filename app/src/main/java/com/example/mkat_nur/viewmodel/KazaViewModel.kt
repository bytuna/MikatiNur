package com.example.mkat_nur.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class KazaSyncState {
    object Idle : KazaSyncState()
    object Syncing : KazaSyncState()
    data class Synced(val userEmail: String) : KazaSyncState()
    object NotLoggedIn : KazaSyncState()
    data class Error(val message: String) : KazaSyncState()
}

class KazaViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("kaza_prefs", Context.MODE_PRIVATE)
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

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

    private val _syncState = MutableStateFlow<KazaSyncState>(KazaSyncState.Idle)
    val syncState: StateFlow<KazaSyncState> = _syncState.asStateFlow()

    init {
        checkAndSyncCloud()
    }

    fun checkAndSyncCloud() {
        val user = try { auth.currentUser } catch (_: Exception) { null }
        if (user == null) {
            _syncState.value = KazaSyncState.NotLoggedIn
            return
        }

        _syncState.value = KazaSyncState.Syncing
        try {
            val userDoc = firestore.collection("users").document(user.uid).collection("data").document("kaza_debt")

            userDoc.get().addOnSuccessListener { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    val cloudFajr = (snapshot.getLong("fajr") ?: 0L).toInt()
                    val cloudDhuhr = (snapshot.getLong("dhuhr") ?: 0L).toInt()
                    val cloudAsr = (snapshot.getLong("asr") ?: 0L).toInt()
                    val cloudMaghrib = (snapshot.getLong("maghrib") ?: 0L).toInt()
                    val cloudIsha = (snapshot.getLong("isha") ?: 0L).toInt()
                    val cloudWitr = (snapshot.getLong("witr") ?: 0L).toInt()

                    _fajrDebt.value = cloudFajr
                    _dhuhrDebt.value = cloudDhuhr
                    _asrDebt.value = cloudAsr
                    _maghribDebt.value = cloudMaghrib
                    _ishaDebt.value = cloudIsha
                    _witrDebt.value = cloudWitr

                    prefs.edit().apply {
                        putInt("fajr_debt", cloudFajr)
                        putInt("dhuhr_debt", cloudDhuhr)
                        putInt("asr_debt", cloudAsr)
                        putInt("maghrib_debt", cloudMaghrib)
                        putInt("isha_debt", cloudIsha)
                        putInt("witr_debt", cloudWitr)
                        apply()
                    }

                    val accountName = user.email.takeIf { !it.isNullOrEmpty() } ?: user.displayName.takeIf { !it.isNullOrEmpty() } ?: "Kullanıcı"
                    _syncState.value = KazaSyncState.Synced(accountName)
                } else {
                    pushLocalToCloud()
                }
            }.addOnFailureListener { e ->
                Log.e("KazaSync", "Fetch error: ${e.message}")
                _syncState.value = KazaSyncState.Error(e.message ?: "Bulut senkronizasyon hatası")
            }
        } catch (e: Exception) {
            Log.e("KazaSync", "Sync Exception: ${e.message}")
            _syncState.value = KazaSyncState.NotLoggedIn
        }
    }

    private fun pushLocalToCloud() {
        val user = try { auth.currentUser } catch (_: Exception) { null } ?: return

        val data = hashMapOf(
            "fajr" to _fajrDebt.value,
            "dhuhr" to _dhuhrDebt.value,
            "asr" to _asrDebt.value,
            "maghrib" to _maghribDebt.value,
            "isha" to _ishaDebt.value,
            "witr" to _witrDebt.value,
            "last_updated" to System.currentTimeMillis(),
            "user_email" to (user.email ?: "")
        )

        try {
            firestore.collection("users").document(user.uid).collection("data").document("kaza_debt")
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    val accountName = user.email.takeIf { !it.isNullOrEmpty() } ?: user.displayName.takeIf { !it.isNullOrEmpty() } ?: "Kullanıcı"
                    _syncState.value = KazaSyncState.Synced(accountName)
                }
                .addOnFailureListener { e ->
                    Log.e("KazaSync", "Push error: ${e.message}")
                    _syncState.value = KazaSyncState.Error(e.message ?: "Yedekleme hatası")
                }
        } catch (e: Exception) {
            Log.e("KazaSync", "Push Exception: ${e.message}")
        }
    }

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
        pushLocalToCloud()
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
        pushLocalToCloud()
    }

    fun resetAll() {
        _fajrDebt.value = 0
        _dhuhrDebt.value = 0
        _asrDebt.value = 0
        _maghribDebt.value = 0
        _ishaDebt.value = 0
        _witrDebt.value = 0
        prefs.edit().clear().apply()
        pushLocalToCloud()
    }
}
