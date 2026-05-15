package com.example.mkat_nur.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.mkat_nur.model.DailyContent
import com.example.mkat_nur.model.PrayerData
import com.example.mkat_nur.model.Province
import com.example.mkat_nur.model.toPrayerData
import com.example.mkat_nur.receiver.PrayerNotificationReceiver
import com.example.mkat_nur.util.NotificationHelper
import com.google.android.gms.location.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

sealed class PrayerUiState {
    object Loading : PrayerUiState()
    data class Success(val data: PrayerData) : PrayerUiState()
    data class Error(val message: String) : PrayerUiState()
}

data class CountdownState(
    val hours: Int, val minutes: Int, val seconds: Int,
    val nextPrayer: String, val currentPrayer: String,
    val isKerahat: Boolean = false
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("mkat_nur_prefs", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow<PrayerUiState>(PrayerUiState.Loading)
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    private val _offsetAppliedUiState = MutableStateFlow<PrayerUiState>(PrayerUiState.Loading)
    val offsetAppliedUiState: StateFlow<PrayerUiState> = _offsetAppliedUiState.asStateFlow()

    private val _countdownState = MutableStateFlow<CountdownState?>(null)
    val countdownState: StateFlow<CountdownState?> = _countdownState.asStateFlow()

    private val savedCityName = prefs.getString("selected_city_name", "İstanbul") ?: "İstanbul"
    private val savedCityId = prefs.getString("selected_city_id", "9541") ?: "9541"
    private val _selectedProvince = MutableStateFlow(Province(savedCityName, savedCityId))
    val selectedProvince: StateFlow<Province> = _selectedProvince.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(
        if (prefs.contains("is_dark_mode")) prefs.getBoolean("is_dark_mode", false) else null
    )
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val _reminderMinutes = MutableStateFlow(
        prefs.getStringSet("reminder_minutes_set", setOf("15"))?.map { it.toInt() }?.toSet() ?: setOf(15)
    )
    val reminderMinutes: StateFlow<Set<Int>> = _reminderMinutes.asStateFlow()

    private val _fontSize = MutableStateFlow(prefs.getFloat("font_size", 16f))
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _dailyContentType = MutableStateFlow(prefs.getString("daily_content_type", "Ayet") ?: "Ayet")
    val dailyContentType: StateFlow<String> = _dailyContentType.asStateFlow()

    private val _timeOffset = MutableStateFlow(prefs.getInt("time_offset", 0))
    val timeOffset: StateFlow<Int> = _timeOffset.asStateFlow()

    private val _imsakOffset = MutableStateFlow(prefs.getInt("offset_imsak", 0))
    val imsakOffset: StateFlow<Int> = _imsakOffset.asStateFlow()

    private val _sunriseOffset = MutableStateFlow(prefs.getInt("offset_sunrise", 0))
    val sunriseOffset: StateFlow<Int> = _sunriseOffset.asStateFlow()

    private val _dhuhrOffset = MutableStateFlow(prefs.getInt("offset_dhuhr", 0))
    val dhuhrOffset: StateFlow<Int> = _dhuhrOffset.asStateFlow()

    private val _asrOffset = MutableStateFlow(prefs.getInt("offset_asr", 0))
    val asrOffset: StateFlow<Int> = _asrOffset.asStateFlow()

    private val _maghribOffset = MutableStateFlow(prefs.getInt("offset_maghrib", 0))
    val maghribOffset: StateFlow<Int> = _maghribOffset.asStateFlow()

    private val _ishaOffset = MutableStateFlow(prefs.getInt("offset_isha", 0))
    val ishaOffset: StateFlow<Int> = _ishaOffset.asStateFlow()

    private val _notificationSoundUri = MutableStateFlow(prefs.getString("notif_sound_uri", null))
    val notificationSoundUri: StateFlow<String?> = _notificationSoundUri.asStateFlow()

    private val _highlightColor = MutableStateFlow(prefs.getInt("highlight_color", 0xFFFF9800.toInt()))
    val highlightColor: StateFlow<Int> = _highlightColor.asStateFlow()

    private val _slidingDuration = MutableStateFlow(prefs.getFloat("sliding_duration", 3f))
    val slidingDuration: StateFlow<Float> = _slidingDuration.asStateFlow()

    private val _showAyet = MutableStateFlow(prefs.getBoolean("show_ayet", true))
    val showAyet: StateFlow<Boolean> = _showAyet.asStateFlow()

    private val _showHadis = MutableStateFlow(prefs.getBoolean("show_hadis", true))
    val showHadis: StateFlow<Boolean> = _showHadis.asStateFlow()

    private val _showVecize = MutableStateFlow(prefs.getBoolean("show_vecize", true))
    val showVecize: StateFlow<Boolean> = _showVecize.asStateFlow()

    private val _showEsma = MutableStateFlow(prefs.getBoolean("show_esma", true))
    val showEsma: StateFlow<Boolean> = _showEsma.asStateFlow()

    private val _notifyImsak = MutableStateFlow(prefs.getBoolean("notify_imsak", true))
    val notifyImsak: StateFlow<Boolean> = _notifyImsak.asStateFlow()

    private val _notifySunrise = MutableStateFlow(prefs.getBoolean("notify_sunrise", true))
    val notifySunrise: StateFlow<Boolean> = _notifySunrise.asStateFlow()

    private val _notifyDhuhr = MutableStateFlow(prefs.getBoolean("notify_dhuhr", true))
    val notifyDhuhr: StateFlow<Boolean> = _notifyDhuhr.asStateFlow()

    private val _notifyAsr = MutableStateFlow(prefs.getBoolean("notify_asr", true))
    val notifyAsr: StateFlow<Boolean> = _notifyAsr.asStateFlow()

    private val _notifyMaghrib = MutableStateFlow(prefs.getBoolean("notify_maghrib", true))
    val notifyMaghrib: StateFlow<Boolean> = _notifyMaghrib.asStateFlow()

    private val _notifyIsha = MutableStateFlow(prefs.getBoolean("notify_isha", true))
    val notifyIsha: StateFlow<Boolean> = _notifyIsha.asStateFlow()

    private val _notifyKerahat = MutableStateFlow(prefs.getBoolean("notify_kerahat", true))
    val notifyKerahat: StateFlow<Boolean> = _notifyKerahat.asStateFlow()

    private val _lastUpdateTimestamp = MutableStateFlow(prefs.getLong("last_update_timestamp", 0L))
    val lastUpdateTimestamp: StateFlow<Long> = _lastUpdateTimestamp.asStateFlow()

    private val _autoLocationInterval = MutableStateFlow(prefs.getInt("auto_location_interval", 0))
    val autoLocationInterval: StateFlow<Int> = _autoLocationInterval.asStateFlow()

    private val _isWomenSpecial = MutableStateFlow(prefs.getBoolean("is_women_special", false))
    val isWomenSpecial: StateFlow<Boolean> = _isWomenSpecial.asStateFlow()

    private val _widgetTransparency = MutableStateFlow(prefs.getFloat("widget_transparency", 0.9f))
    val widgetTransparency: StateFlow<Float> = _widgetTransparency.asStateFlow()

    private val _widgetTitleColor = MutableStateFlow(prefs.getInt("widget_title_color", 0xFFFFD700.toInt()))
    val widgetTitleColor: StateFlow<Int> = _widgetTitleColor.asStateFlow()

    private val _widgetTextColor = MutableStateFlow(prefs.getInt("widget_text_color", 0xFFFFFFFF.toInt()))
    val widgetTextColor: StateFlow<Int> = _widgetTextColor.asStateFlow()

    private val _dailyContent = MutableStateFlow(DailyContent())
    val dailyContent: StateFlow<DailyContent> = _dailyContent.asStateFlow()

    private val _allVakitler = MutableStateFlow<List<PrayerData>>(emptyList())
    val allVakitler: StateFlow<List<PrayerData>> = _allVakitler.asStateFlow()

    private val _sehirler = MutableStateFlow<List<com.example.mkat_nur.network.SehirResponse>>(emptyList())
    val sehirler: StateFlow<List<com.example.mkat_nur.network.SehirResponse>> = _sehirler.asStateFlow()

    private val _ilceler = MutableStateFlow<List<com.example.mkat_nur.network.IlceResponse>>(emptyList())
    val ilceler: StateFlow<List<com.example.mkat_nur.network.IlceResponse>> = _ilceler.asStateFlow()

    private val _dataSource = MutableStateFlow("Yükleniyor...")
    val dataSource: StateFlow<String> = _dataSource.asStateFlow()

    private var countdownJob: Job? = null
    private var allPrayerDataList: List<PrayerData> = emptyList()

    private var locationCallback: LocationCallback? = null

    init {
        fetchPrayerTimes()
        fetchDailyContent()
        fetchSehirler()
        scheduleAutoLocationWork(_autoLocationInterval.value)
        startLocationTracking()
    }

    private fun startLocationTracking() {
        if (_autoLocationInterval.value == 0) return
        
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>())
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            1000 * 60 * 60 // 1 saat
        ).apply {
            setMinUpdateDistanceMeters(5000f) // 5 km
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    Log.d("PrayerViewModel", "Otomatik konum güncelleme tetiklendi: ${it.latitude}, ${it.longitude}")
                    fetchLocationAndDistrict()
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                android.os.Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("PrayerViewModel", "Konum izni eksik.")
        }
    }

    private fun fetchSehirler() {
        viewModelScope.launch {
            try {
                val list = RetrofitClient.diyanetInstance.getSehirler()
                _sehirler.value = list.sortedBy { it.SehirAdi }
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "Şehirler çekilemedi: ${e.message}")
            }
        }
    }

    fun fetchIlceler(sehirId: String) {
        _ilceler.value = emptyList()
        viewModelScope.launch {
            try {
                val list = RetrofitClient.diyanetInstance.getIlceler(sehirId)
                _ilceler.value = list.sortedBy { it.IlceAdi }
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "İlçeler çekilemedi: ${e.message}")
            }
        }
    }

    fun toggleDarkMode(isDark: Boolean?) {
        _isDarkMode.value = isDark
        if (isDark == null) prefs.edit().remove("is_dark_mode").apply()
        else prefs.edit().putBoolean("is_dark_mode", isDark).apply()
    }

    fun toggleReminderMinute(mins: Int) {
        val currentSet = _reminderMinutes.value.toMutableSet()
        if (currentSet.contains(mins)) {
            if (currentSet.size > 1 || mins != 0) { // En az bir seçim kalsın veya 0 (vaktinde) değilse
                currentSet.remove(mins)
            }
        } else {
            currentSet.add(mins)
        }
        _reminderMinutes.value = currentSet
        prefs.edit().putStringSet("reminder_minutes_set", currentSet.map { it.toString() }.toSet()).apply()
        
        val currentState = _uiState.value
        if (currentState is PrayerUiState.Success) {
            schedulePrayerAlarms(currentState.data)
        }
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size
        prefs.edit().putFloat("font_size", size).apply()
    }

    fun setDailyContentType(type: String) {
        _dailyContentType.value = type
        prefs.edit().putString("daily_content_type", type).apply()
        fetchDailyContent()
    }

    fun setTimeOffset(offset: Int) {
        _timeOffset.value = offset
        prefs.edit().putInt("time_offset", offset).apply()
        applyOffsetToUiState()
        val currentState = _uiState.value
        if (currentState is PrayerUiState.Success) {
            startCountdown(allPrayerDataList)
        }
    }

    fun setPrayerOffset(prayer: String, offset: Int) {
        val prefKey = "offset_${prayer.lowercase()}"
        prefs.edit().putInt(prefKey, offset).apply()
        when(prayer) {
            "İmsak" -> _imsakOffset.value = offset
            "Güneş" -> _sunriseOffset.value = offset
            "Öğle" -> _dhuhrOffset.value = offset
            "İkindi" -> _asrOffset.value = offset
            "Akşam" -> _maghribOffset.value = offset
            "Yatsı" -> _ishaOffset.value = offset
        }
        applyOffsetToUiState()
        val currentState = _uiState.value
        if (currentState is PrayerUiState.Success) {
            startCountdown(allPrayerDataList)
        }
    }

    fun setNotificationSound(uri: String?) {
        _notificationSoundUri.value = uri
        prefs.edit().putString("notif_sound_uri", uri).apply()
        com.example.mkat_nur.util.NotificationHelper(getApplication()).showNotification("Ses Güncellendi", "Yeni bildirim sesi ayarlandı.")
    }

    fun setHighlightColor(colorInt: Int) {
        _highlightColor.value = colorInt
        prefs.edit().putInt("highlight_color", colorInt).apply()
        val currentState = _offsetAppliedUiState.value
        if (currentState is PrayerUiState.Success) {
            com.example.mkat_nur.service.PrayerNotificationService.startService(
                getApplication(),
                _selectedProvince.value.name,
                currentState.data
            )
        }
    }

    fun setSlidingDuration(duration: Float) {
        _slidingDuration.value = duration
        prefs.edit().putFloat("sliding_duration", duration).apply()
    }

    fun setAutoLocationInterval(interval: Int) {
        _autoLocationInterval.value = interval
        prefs.edit().putInt("auto_location_interval", interval).apply()
        // WorkManager'ı tetikle
        scheduleAutoLocationWork(interval)
        
        // Foreground takibi güncelle
        locationCallback?.let {
            LocationServices.getFusedLocationProviderClient(getApplication<Application>()).removeLocationUpdates(it)
            locationCallback = null
        }
        if (interval > 0) {
            startLocationTracking()
        }
    }

    private fun scheduleAutoLocationWork(interval: Int) {
        val workManager = WorkManager.getInstance(getApplication())
        workManager.cancelAllWorkByTag("AutoLocationWork")
        
        if (interval > 0) {
            val workRequest = PeriodicWorkRequestBuilder<com.example.mkat_nur.worker.LocationUpdateWorker>(
                interval.toLong(), java.util.concurrent.TimeUnit.HOURS
            ).addTag("AutoLocationWork").build()
            
            workManager.enqueueUniquePeriodicWork(
                "AutoLocationWork",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }

    fun toggleContentType(type: String, enabled: Boolean) {
        when(type) {
            "Ayet" -> { _showAyet.value = enabled; prefs.edit().putBoolean("show_ayet", enabled).apply() }
            "Hadis" -> { _showHadis.value = enabled; prefs.edit().putBoolean("show_hadis", enabled).apply() }
            "Vecize" -> { _showVecize.value = enabled; prefs.edit().putBoolean("show_vecize", enabled).apply() }
            "Esma" -> { _showEsma.value = enabled; prefs.edit().putBoolean("show_esma", enabled).apply() }
        }
    }

    fun toggleNotification(prayer: String, enabled: Boolean) {
        val prefKey = "notify_${prayer.lowercase()}"
        prefs.edit().putBoolean(prefKey, enabled).apply()
        when(prayer) {
            "İmsak" -> _notifyImsak.value = enabled
            "Güneş" -> _notifySunrise.value = enabled
            "Öğle" -> _notifyDhuhr.value = enabled
            "İkindi" -> _notifyAsr.value = enabled
            "Akşam" -> _notifyMaghrib.value = enabled
            "Yatsı" -> _notifyIsha.value = enabled
            "Kerahat" -> _notifyKerahat.value = enabled
        }
        // Alarmları güncelle
        val currentState = _uiState.value
        if (currentState is PrayerUiState.Success) {
            schedulePrayerAlarms(currentState.data)
        }
    }

    fun toggleAllNotifications(enabled: Boolean) {
        val prayers = listOf("İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı", "Kerahat")
        prayers.forEach { prayer ->
            val prefKey = "notify_${prayer.lowercase()}"
            prefs.edit().putBoolean(prefKey, enabled).apply()
        }
        _notifyImsak.value = enabled
        _notifySunrise.value = enabled
        _notifyDhuhr.value = enabled
        _notifyAsr.value = enabled
        _notifyMaghrib.value = enabled
        _notifyIsha.value = enabled
        _notifyKerahat.value = enabled

        val currentState = _uiState.value
        if (currentState is PrayerUiState.Success) {
            schedulePrayerAlarms(currentState.data)
        }
    }

    fun toggleWomenSpecial(enabled: Boolean) {
        _isWomenSpecial.value = enabled
        prefs.edit().putBoolean("is_women_special", enabled).apply()
        
        // Bildirim göster
        val helper = NotificationHelper(getApplication())
        if (enabled) {
            helper.showNotification("Kadın Özel Modu Aktif", "Vakit bildirimleri geçici olarak sessize alındı.")
        } else {
            helper.showNotification("Kadın Özel Modu Kapatıldı", "Vakit bildirimleri tekrar aktif edildi.")
        }
        
        // Alarmları güncelle
        val currentState = _uiState.value
        if (currentState is PrayerUiState.Success) {
            schedulePrayerAlarms(currentState.data)
        }
    }

    fun setWidgetTransparency(transparency: Float) {
        _widgetTransparency.value = transparency
        prefs.edit().putFloat("widget_transparency", transparency).apply()
        // Widget'ı güncellemek için broadcast gönder
        val intent = Intent(getApplication(), com.example.mkat_nur.receiver.QuoteWidgetProvider::class.java).apply {
            action = "REFRESH_WIDGET"
        }
        getApplication<Application>().sendBroadcast(intent)
    }

    fun setWidgetTitleColor(color: Int) {
        _widgetTitleColor.value = color
        prefs.edit().putInt("widget_title_color", color).apply()
        val intent = Intent(getApplication(), com.example.mkat_nur.receiver.QuoteWidgetProvider::class.java).apply {
            action = "REFRESH_WIDGET"
        }
        getApplication<Application>().sendBroadcast(intent)
    }

    fun setWidgetTextColor(color: Int) {
        _widgetTextColor.value = color
        prefs.edit().putInt("widget_text_color", color).apply()
        val intent = Intent(getApplication(), com.example.mkat_nur.receiver.QuoteWidgetProvider::class.java).apply {
            action = "REFRESH_WIDGET"
        }
        getApplication<Application>().sendBroadcast(intent)
    }

    fun refreshLocation() {
        fetchLocationAndDistrict()
    }

    private fun fetchLocationAndDistrict() {
        viewModelScope.launch {
            _dataSource.value = "GPS Konum Aranıyor..."
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>())
                val location = try {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                } catch (e: SecurityException) {
                    Log.e("PrayerViewModel", "GPS İzni Yok: ${e.message}")
                    null
                }
                
                if (location != null) {
                    val geocoder = Geocoder(getApplication(), Locale("tr"))
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    
                    if (!addresses.isNullOrEmpty()) {
                        val city = addresses[0].adminArea ?: "" // Örn: İstanbul
                        val district = addresses[0].subAdminArea ?: addresses[0].locality ?: "" // Örn: Pendik
                        
                        Log.d("PrayerViewModel", "GPS Konum: $city, $district")
                        
                        // Önce şehri bul
                        val sehirList = RetrofitClient.diyanetInstance.getSehirler()
                        val matchedSehir = sehirList.find { it.SehirAdi.contains(city, ignoreCase = true) }
                        
                        if (matchedSehir != null) {
                            // Sonra ilçeyi bul
                            val ilceList = RetrofitClient.diyanetInstance.getIlceler(matchedSehir.SehirID)
                            val matchedIlce = ilceList.find { it.IlceAdi.contains(district, ignoreCase = true) }
                                ?: ilceList.find { it.IlceAdi.contains(city, ignoreCase = true) } // İlçe bulunamazsa merkezi dene
                            
                            if (matchedIlce != null) {
                                onProvinceSelected(Province(matchedIlce.IlceAdi, matchedIlce.IlceID))
                                return@launch
                            }
                        }
                    }
                }
                _dataSource.value = "GPS Başarısız, Kayıtlı Veri."
                fetchPrayerTimes()
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "GPS Hatası: ${e.message}")
                _dataSource.value = "GPS Hatası, Kayıtlı Veri."
                fetchPrayerTimes()
            }
        }
    }

    fun onProvinceSelected(province: Province) {
        _selectedProvince.value = province
        prefs.edit()
            .putString("selected_city_name", province.name)
            .putString("selected_city_id", province.id)
            .apply()
        fetchPrayerTimes()
        com.example.mkat_nur.receiver.QuoteWidgetProvider.updateAllWidgets(getApplication())
    }

    private fun fetchPrayerTimes() {
        _uiState.value = PrayerUiState.Loading
        _offsetAppliedUiState.value = PrayerUiState.Loading
        viewModelScope.launch {
            try {
                // 1. Önce hafızadaki (Cache) veriyi kontrol et
                loadVakitlerFromCache()

                val cityIdentifier = if (_selectedProvince.value.id.isNotEmpty()) _selectedProvince.value.id else _selectedProvince.value.name
                val responseList = RetrofitClient.diyanetInstance.getVakitler(cityIdentifier)
                
                if (responseList.isNotEmpty()) {
                    val now = System.currentTimeMillis()
                    _lastUpdateTimestamp.value = now
                    prefs.edit().putLong("last_update_timestamp", now).apply()

                    _dataSource.value = "Diyanet (Resmi)"
                    allPrayerDataList = responseList.map { it.toPrayerData() }
                    _allVakitler.value = allPrayerDataList
                    saveVakitlerToCache(allPrayerDataList) // Hafızaya kaydet
                    
                    val todayStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                    val prayerData = allPrayerDataList.find { it.date.readable == todayStr } ?: allPrayerDataList[0]

                    _uiState.value = PrayerUiState.Success(prayerData)
                    applyOffsetToUiState()
                    schedulePrayerAlarms(prayerData)
                    startCountdown(allPrayerDataList)
                    com.example.mkat_nur.receiver.QuoteWidgetProvider.updateAllWidgets(getApplication())
                } else {
                    handleFetchError("Veri bulunamadı.")
                }
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "Diyanet API Hatası: ${e.message}")
                handleFetchError(e.message ?: "Bilinmeyen Hata")
            }
        }
    }

    private fun handleFetchError(message: String) {
        if (_allVakitler.value.isNotEmpty()) {
            _dataSource.value = "Diyanet (Hafıza)"
            // Eğer internet yoksa ama hafızada veri varsa, bugünün verisini bul
            val todayStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
            val todayData = _allVakitler.value.find { it.date.readable == todayStr } ?: _allVakitler.value[0]
            
            _uiState.value = PrayerUiState.Success(todayData)
            applyOffsetToUiState()
            startCountdown(_allVakitler.value)
        } else {
            // Hiç veri yoksa fallback'e git
            _dataSource.value = "Aladhan (Yedek)"
            fetchPrayerTimesAladhan()
        }
    }

    private fun saveVakitlerToCache(data: List<PrayerData>) {
        val gson = com.google.gson.Gson()
        val json = gson.toJson(data)
        prefs.edit().putString("vakitler_cache_${_selectedProvince.value.id}", json).apply()
    }

    private fun loadVakitlerFromCache() {
        try {
            val json = prefs.getString("vakitler_cache_${_selectedProvince.value.id}", null)
            if (json != null) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<PrayerData>>() {}.type
                val cachedData: List<PrayerData> = gson.fromJson(json, type)
                _allVakitler.value = cachedData
                allPrayerDataList = cachedData
            }
        } catch (e: Exception) {
            Log.e("PrayerViewModel", "Cache okuma hatası: ${e.message}")
            prefs.edit().remove("vakitler_cache_${_selectedProvince.value.id}").apply()
        }
    }

    private fun fetchPrayerTimesAladhan() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.aladhanInstance.getPrayerTimes(_selectedProvince.value.name, "Turkey")
                val prayerData = response.data
                allPrayerDataList = listOf(prayerData)
                _uiState.value = PrayerUiState.Success(prayerData)
                applyOffsetToUiState()
                schedulePrayerAlarms(prayerData)
                startCountdown(allPrayerDataList)
            } catch (e: Exception) {
                _uiState.value = PrayerUiState.Error("Aladhan API Hatası: ${e.message}")
                _offsetAppliedUiState.value = PrayerUiState.Error("Aladhan API Hatası: ${e.message}")
            }
        }
    }

    private fun applyOffsetToUiState() {
        val currentState = _uiState.value
        if (currentState is PrayerUiState.Success) {
            val originalTimings = currentState.data.timings
            val commonOffset = _timeOffset.value
            val modifiedTimings = originalTimings.copy(
                fajr = applyOffset(originalTimings.fajr, commonOffset + _imsakOffset.value),
                sunrise = applyOffset(originalTimings.sunrise, commonOffset + _sunriseOffset.value),
                dhuhr = applyOffset(originalTimings.dhuhr, commonOffset + _dhuhrOffset.value),
                asr = applyOffset(originalTimings.asr, commonOffset + _asrOffset.value),
                maghrib = applyOffset(originalTimings.maghrib, commonOffset + _maghribOffset.value),
                isha = applyOffset(originalTimings.isha, commonOffset + _ishaOffset.value)
            )
            val modifiedData = currentState.data.copy(timings = modifiedTimings)
            _offsetAppliedUiState.value = PrayerUiState.Success(modifiedData)

            com.example.mkat_nur.service.PrayerNotificationService.startService(
                getApplication(),
                _selectedProvince.value.name,
                modifiedData
            )
        }
    }

    fun fetchDailyContent() {
        viewModelScope.launch {
            try {
                val contentManager = com.example.mkat_nur.util.ContentManager(getApplication())
                val ayet = contentManager.getContentByType("ayet")
                val hadis = contentManager.getContentByType("hadis")
                val vecize = contentManager.getContentByType("vecize")

                // Esmaü'l Hüsna hala sabit kalabilir veya onu da bir dosyaya taşıyabilirsiniz
                val names = listOf(
                    "Er-Rahmân" to "Dünyada bütün mahlukata rızık veren.",
                    "Er-Rahîm" to "Ahirette müminlere sonsuz ikramda bulunan.",
                    "El-Melik" to "Kâinatın mutlak sahibi ve yöneticisi."
                )
                val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                val n = names[dayOfYear % names.size]

                _dailyContent.value = DailyContent(
                    verse = ayet.text, verseSource = ayet.source,
                    hadith = hadis.text, hadithSource = hadis.source,
                    quote = vecize.text, quoteSource = vecize.source,
                    name = n.first, nameMeaning = n.second
                )
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "İçerik yükleme hatası: ${e.message}")
            }
        }
    }

    private fun applyOffset(timeStr: String, offset: Int): String {
        if (offset == 0) return timeStr
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = sdf.parse(timeStr.substringBefore(" "))!!
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.MINUTE, offset)
            }
            sdf.format(cal.time)
        } catch (e: Exception) { timeStr }
    }

    private fun schedulePrayerAlarms(data: PrayerData) {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Kadın özel modu aktifse alarmları iptal et ve kurma
        if (_isWomenSpecial.value) {
            cancelAllAlarms(alarmManager)
            return
        }

        val reminderMinsSet = _reminderMinutes.value
        val commonOffset = _timeOffset.value

        val prayerTimes = mutableListOf(
            Triple("İmsak", applyOffset(data.timings.fajr, commonOffset + _imsakOffset.value), _notifyImsak.value),
            Triple("Güneş", applyOffset(data.timings.sunrise, commonOffset + _sunriseOffset.value), _notifySunrise.value),
            Triple("Öğle", applyOffset(data.timings.dhuhr, commonOffset + _dhuhrOffset.value), _notifyDhuhr.value),
            Triple("İkindi", applyOffset(data.timings.asr, commonOffset + _asrOffset.value), _notifyAsr.value),
            Triple("Akşam", applyOffset(data.timings.maghrib, commonOffset + _maghribOffset.value), _notifyMaghrib.value),
            Triple("Yatsı", applyOffset(data.timings.isha, commonOffset + _ishaOffset.value), _notifyIsha.value)
        )

        // Kerahat Vakitlerini Ekle
        if (_notifyKerahat.value) {
            val sunriseTime = applyOffset(data.timings.sunrise, commonOffset + _sunriseOffset.value)
            val dhuhrTime = applyOffset(data.timings.dhuhr, commonOffset + _dhuhrOffset.value)
            val maghribTime = applyOffset(data.timings.maghrib, commonOffset + _maghribOffset.value)

            prayerTimes.add(Triple("Kerahat (İşrak)", applyOffset(sunriseTime, 45), true))
            prayerTimes.add(Triple("Kerahat (İstiva)", applyOffset(dhuhrTime, -45), true))
            prayerTimes.add(Triple("Kerahat (İsfirar)", applyOffset(maghribTime, -45), true))
        }

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = Calendar.getInstance()

        // Cuma Mesajı Alarmı (Öğleden 45 dk önce)
        if (now.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            try {
                val dhuhrTime = applyOffset(data.timings.dhuhr, commonOffset + _dhuhrOffset.value)
                val dateParsed = sdf.parse(dhuhrTime.substringBefore(" "))!!
                val cumaCal = Calendar.getInstance().apply {
                    val temp = Calendar.getInstance().apply { time = dateParsed }
                    set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.MINUTE, -45)
                }

                if (cumaCal.after(now)) {
                    val intent = Intent(getApplication(), PrayerNotificationReceiver::class.java).apply {
                        putExtra("IS_FRIDAY_MESSAGE", true)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        getApplication(), 9999, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setExactAlarm(alarmManager, cumaCal.timeInMillis, pendingIntent)
                }
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "Friday alarm error: ${e.message}")
            }
        }

        prayerTimes.forEachIndexed { index, (name, timeStr, isNotifyEnabled) ->
            if (!isNotifyEnabled) return@forEachIndexed // Bildirim kapalıysa atla

            try {
                val dateParsed = sdf.parse(timeStr.substringBefore(" "))!!
                
                // Vakit girdi uyarısı (0. dakika) her zaman eklenmeli
                val allReminders = reminderMinsSet.toMutableSet().apply { add(0) }
                
                allReminders.forEach { mins ->
                    val prayerCal = Calendar.getInstance().apply {
                        val temp = Calendar.getInstance().apply { time = dateParsed }
                        set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        add(Calendar.MINUTE, -mins)
                    }

                    if (prayerCal.after(now)) {
                        val intent = Intent(getApplication(), PrayerNotificationReceiver::class.java).apply {
                            putExtra("PRAYER_NAME", name)
                            putExtra("MINUTES_LEFT", mins)
                        }
                        // Request code için vakit indeksi ve dakika değerini birleştiriyoruz (çakışma olmaması için)
                        val requestCode = (index * 1000) + mins
                        val pendingIntent = PendingIntent.getBroadcast(
                            getApplication(), requestCode, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setExactAlarm(alarmManager, prayerCal.timeInMillis, pendingIntent)
                    }
                }
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "Error scheduling alarm for $name: ${e.message}")
            }
        }
    }

    private fun setExactAlarm(alarmManager: AlarmManager, timeInMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("PrayerViewModel", "Alarm scheduling failed: ${e.message}")
        }
    }

    private fun cancelAllAlarms(alarmManager: AlarmManager) {
        val reminders = listOf(0, 15, 30, 45)
        for (index in 0..10) {
            for (mins in reminders) {
                val requestCode = (index * 1000) + mins
                val intent = Intent(getApplication(), PrayerNotificationReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    getApplication(), requestCode, intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }
        // Cuma alarmını da iptal et
        val fridayIntent = Intent(getApplication(), PrayerNotificationReceiver::class.java)
        val fridayPending = PendingIntent.getBroadcast(
            getApplication(), 9999, fridayIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (fridayPending != null) {
            alarmManager.cancel(fridayPending)
            fridayPending.cancel()
        }
    }

    private fun startCountdown(dataList: List<PrayerData>) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val now = Calendar.getInstance()
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                val commonOffset = _timeOffset.value
                
                val todayStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(now.time)
                val currentData = dataList.find { it.date.readable == todayStr } ?: dataList.getOrNull(0) ?: break
                
                val timings = mapOf(
                    "İmsak" to applyOffset(currentData.timings.fajr, commonOffset + _imsakOffset.value),
                    "Sabah" to applyOffset(currentData.timings.sabah, commonOffset + _imsakOffset.value),
                    "Güneş" to applyOffset(currentData.timings.sunrise, commonOffset + _sunriseOffset.value),
                    "Öğle" to applyOffset(currentData.timings.dhuhr, commonOffset + _dhuhrOffset.value),
                    "İkindi" to applyOffset(currentData.timings.asr, commonOffset + _asrOffset.value),
                    "Akşam" to applyOffset(currentData.timings.maghrib, commonOffset + _maghribOffset.value),
                    "Yatsı" to applyOffset(currentData.timings.isha, commonOffset + _ishaOffset.value)
                )

                val sortedTimes = timings.mapNotNull { entry ->
                    try {
                        val dateParsed = format.parse(entry.value.substringBefore(" "))
                        val cal = Calendar.getInstance().apply {
                            time = now.time
                            val temp = Calendar.getInstance().apply { time = dateParsed!! }
                            set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        entry.key to cal
                    } catch (e: Exception) { null }
                }.sortedBy { it.second.timeInMillis }

                var nextN = ""; var nextT: Calendar? = null; var currN = "Yatsı"
                
                for (i in sortedTimes.indices) {
                    if (now.before(sortedTimes[i].second)) {
                        nextN = sortedTimes[i].first
                        nextT = sortedTimes[i].second
                        currN = if (i == 0) "Yatsı" else sortedTimes[i-1].first
                        break
                    }
                }
                
                if (nextT == null) {
                    // Yarının verisini al
                    val tomorrowCal = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
                    val tomorrowStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(tomorrowCal.time)
                    val tomorrowData = dataList.find { it.date.readable == tomorrowStr } ?: dataList.getOrNull(1)

                    if (tomorrowData != null) {
                        val tomorrowImsakStr = applyOffset(tomorrowData.timings.fajr, commonOffset + _imsakOffset.value)
                        val dateParsed = format.parse(tomorrowImsakStr.substringBefore(" "))!!
                        nextT = Calendar.getInstance().apply {
                            time = tomorrowCal.time
                            val temp = Calendar.getInstance().apply { time = dateParsed }
                            set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        nextN = "İmsak"
                    } else {
                        // Eğer yarın verisi yoksa (ay sonu vb), bugünküne 1 gün ekle (fallback)
                        nextT = (sortedTimes.first().second.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
                        nextN = "İmsak"
                    }
                }
                
                val isRamadan = currentData.date.hijri.month.en.contains("Ramadan", true)
                var nextP = if (nextN == "Akşam" && isRamadan) "İftar" else nextN

                // Kerahat Vakti Kontrolü
                var isKerahat = false
                try {
                    val sunriseCal = sortedTimes.find { it.first == "Güneş" }?.second
                    val dhuhrCal = sortedTimes.find { it.first == "Öğle" }?.second
                    val maghribCal = sortedTimes.find { it.first == "Akşam" }?.second

                    if (sunriseCal != null) {
                        val sunriseEnd = (sunriseCal.clone() as Calendar).apply { add(Calendar.MINUTE, 45) }
                        if (now.after(sunriseCal) && now.before(sunriseEnd)) isKerahat = true
                    }
                    if (dhuhrCal != null) {
                        val dhuhrStart = (dhuhrCal.clone() as Calendar).apply { add(Calendar.MINUTE, -45) }
                        if (now.after(dhuhrStart) && now.before(dhuhrCal)) isKerahat = true
                    }
                    if (maghribCal != null) {
                        val maghribStart = (maghribCal.clone() as Calendar).apply { add(Calendar.MINUTE, -45) }
                        if (now.after(maghribStart) && now.before(maghribCal)) isKerahat = true
                    }
                } catch (e: Exception) {
                    Log.e("PrayerViewModel", "Kerahat kontrol hatası: ${e.message}")
                }
                
                val diff = nextT!!.timeInMillis - now.timeInMillis
                _countdownState.value = CountdownState((diff/(1000*60*60)).toInt(), ((diff/(1000*60))%60).toInt(), ((diff/1000)%60).toInt(), nextP, currN, isKerahat)
                
                delay(1000)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        locationCallback?.let {
            LocationServices.getFusedLocationProviderClient(getApplication<Application>()).removeLocationUpdates(it)
        }
        countdownJob?.cancel()
    }

    companion object {
        // Artık şehirler API'den dinamik çekiliyor, bu liste sadece yedek/ilk açılış için.
        val initialProvinces = listOf(
            Province("İstanbul", "9541"),
            Province("Ankara", "9206"),
            Province("İzmir", "9560")
        )
    }
}
