package com.example.mkat_nur.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mkat_nur.model.DailyContent
import com.example.mkat_nur.model.PrayerData
import com.example.mkat_nur.model.Province
import com.example.mkat_nur.receiver.PrayerNotificationReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class PrayerUiState {
    object Loading : PrayerUiState()
    data class Success(val data: PrayerData) : PrayerUiState()
    data class Error(val message: String) : PrayerUiState()
}

data class CountdownState(
    val hours: Int, val minutes: Int, val seconds: Int,
    val nextPrayer: String, val currentPrayer: String
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("mkat_nur_prefs", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow<PrayerUiState>(PrayerUiState.Loading)
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    private val _offsetAppliedUiState = MutableStateFlow<PrayerUiState>(PrayerUiState.Loading)
    val offsetAppliedUiState: StateFlow<PrayerUiState> = _offsetAppliedUiState.asStateFlow()

    private val _countdownState = MutableStateFlow<CountdownState?>(null)
    val countdownState: StateFlow<CountdownState?> = _countdownState.asStateFlow()

    // Hafızadan şehir bilgisini oku (Varsayılan İstanbul)
    private val savedCity = prefs.getString("selected_city", "İstanbul") ?: "İstanbul"
    private val _selectedProvince = MutableStateFlow(provinces.first { it.name == savedCity })
    val selectedProvince: StateFlow<Province> = _selectedProvince.asStateFlow()

    // Hafızadan tema bilgisini oku
    private val _isDarkMode = MutableStateFlow<Boolean?>(
        if (prefs.contains("is_dark_mode")) prefs.getBoolean("is_dark_mode", false) else null
    )
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    // Hafızadan hatırlatıcı bilgisini oku
    private val _reminderMinutes = MutableStateFlow(prefs.getInt("reminder_minutes", 15))
    val reminderMinutes: StateFlow<Int> = _reminderMinutes.asStateFlow()

    // --- YENİ AYARLAR ---
    private val _fontSize = MutableStateFlow(prefs.getFloat("font_size", 16f))
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _dailyContentType = MutableStateFlow(prefs.getString("daily_content_type", "Ayet") ?: "Ayet")
    val dailyContentType: StateFlow<String> = _dailyContentType.asStateFlow()

    private val _timeOffset = MutableStateFlow(prefs.getInt("time_offset", 0))
    val timeOffset: StateFlow<Int> = _timeOffset.asStateFlow()

    // Her vakit için ayrı offset (İmsak, Güneş, Öğle, İkindi, Akşam, Yatsı)
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
    // -------------------

    private val _dailyContent = MutableStateFlow(DailyContent())
    val dailyContent: StateFlow<DailyContent> = _dailyContent.asStateFlow()

    private var countdownJob: Job? = null

    init {
        fetchPrayerTimes()
        fetchDailyContent()
    }

    fun toggleDarkMode(isDark: Boolean?) {
        _isDarkMode.value = isDark
        if (isDark == null) prefs.edit().remove("is_dark_mode").apply()
        else prefs.edit().putBoolean("is_dark_mode", isDark).apply()
    }

    fun setReminderMinutes(mins: Int) {
        _reminderMinutes.value = mins
        prefs.edit().putInt("reminder_minutes", mins).apply()
        // Hatırlatıcıları güncelle
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
        // startCountdown'ı tetiklemek için fetchPrayerTimes yerine uiState'i kullanıyoruz
        val currentState = _uiState.value
        if (currentState is PrayerUiState.Success) {
            startCountdown(currentState.data)
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
            startCountdown(currentState.data)
        }
    }

    fun setNotificationSound(uri: String?) {
        _notificationSoundUri.value = uri
        prefs.edit().putString("notif_sound_uri", uri).apply()
        // Bildirim kanalını yeni sesle güncellemek için NotificationHelper'ı tetikle
        com.example.mkat_nur.util.NotificationHelper(getApplication()).showNotification("Ses Güncellendi", "Yeni bildirim sesi ayarlandı.")
    }

    fun setHighlightColor(colorInt: Int) {
        _highlightColor.value = colorInt
        prefs.edit().putInt("highlight_color", colorInt).apply()
        // Servisi güncelle
        val currentState = _offsetAppliedUiState.value
        if (currentState is PrayerUiState.Success) {
            com.example.mkat_nur.service.PrayerNotificationService.startService(
                getApplication(),
                _selectedProvince.value.name,
                currentState.data
            )
        }
    }

    fun refreshLocation() {
        fetchPrayerTimes()
    }

    fun onProvinceSelected(province: Province) {
        _selectedProvince.value = province
        prefs.edit().putString("selected_city", province.name).apply()
        fetchPrayerTimes()
    }

    private fun fetchPrayerTimes() {
        _uiState.value = PrayerUiState.Loading
        _offsetAppliedUiState.value = PrayerUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getPrayerTimes(_selectedProvince.value.name, "Turkey")
                _uiState.value = PrayerUiState.Success(response.data)
                applyOffsetToUiState()
                schedulePrayerAlarms(response.data)
                
                startCountdown(response.data)
            } catch (e: Exception) {
                _uiState.value = PrayerUiState.Error("Hata: ${e.message}")
                _offsetAppliedUiState.value = PrayerUiState.Error("Hata: ${e.message}")
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

            // Servise güncel (offsetli) veriyi gönder
            com.example.mkat_nur.service.PrayerNotificationService.startService(
                getApplication(),
                _selectedProvince.value.name,
                modifiedData
            )
        }
    }

    fun fetchDailyContent() {
        viewModelScope.launch {
            val type = _dailyContentType.value
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            
            // Veri havuzu (Normalde API'den veya DB'den gelir, şimdilik statik)
            val verses = listOf("Şüphesiz güçlükle beraber bir kolaylık vardır." to "İnşirah, 5", "Allah sabredenlerle beraberdir." to "Bakara, 153")
            val hadiths = listOf("Ameller niyetlere göredir." to "Buhari", "Sizin en hayırlınız Kur'an'ı öğrenen ve öğretendir." to "Tirmizi")
            val quotes = listOf("Bismillah her hayrın başıdır." to "Sözler, Risale-i Nur", "Güzel gören güzel düşünür." to "Mektubat, Risale-i Nur")

            val selected = when(type) {
                "Ayet" -> verses[dayOfYear % verses.size]
                "Hadis" -> hadiths[dayOfYear % hadiths.size]
                "Vecize" -> quotes[dayOfYear % quotes.size]
                else -> verses[0]
            }

            // Esmaül Hüsna her zaman gösterilebilir veya o da değişebilir
            val names = listOf("Er-Rahmân" to "Dünyada bütün mahlukata rızık veren.")
            val n = names[dayOfYear % names.size]
            
            _dailyContent.value = DailyContent(selected.first, selected.second, n.first, n.second)
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
        val reminderMins = _reminderMinutes.value
        val commonOffset = _timeOffset.value

        val prayerTimes = listOf(
            "İmsak" to applyOffset(data.timings.fajr, commonOffset + _imsakOffset.value),
            "Güneş" to applyOffset(data.timings.sunrise, commonOffset + _sunriseOffset.value),
            "Öğle" to applyOffset(data.timings.dhuhr, commonOffset + _dhuhrOffset.value),
            "İkindi" to applyOffset(data.timings.asr, commonOffset + _asrOffset.value),
            "Akşam" to applyOffset(data.timings.maghrib, commonOffset + _maghribOffset.value),
            "Yatsı" to applyOffset(data.timings.isha, commonOffset + _ishaOffset.value)
        )

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = Calendar.getInstance()

        prayerTimes.forEachIndexed { index, (name, timeStr) ->
            try {
                val dateParsed = sdf.parse(timeStr.substringBefore(" "))!!
                val prayerCal = Calendar.getInstance().apply {
                    val temp = Calendar.getInstance().apply { time = dateParsed }
                    set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    
                    // Namaz vaktinden reminderMins önce
                    add(Calendar.MINUTE, -reminderMins)
                }

                // Vakit gelince çalacak alarm (0 dakika kala)
                val exactPrayerCal = Calendar.getInstance().apply {
                    val temp = Calendar.getInstance().apply { time = dateParsed }
                    set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Hatırlatıcı Alarmı (Örn: 15 dk önce)
                if (reminderMins > 0 && prayerCal.after(now)) {
                    val intent = Intent(getApplication(), PrayerNotificationReceiver::class.java).apply {
                        putExtra("PRAYER_NAME", name)
                        putExtra("MINUTES_LEFT", reminderMins)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        getApplication(),
                        index + 100, // Çakışma olmasın
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setExactAlarm(alarmManager, prayerCal.timeInMillis, pendingIntent)
                }

                // Tam Vakit Alarmı (0 dk önce)
                if (exactPrayerCal.after(now)) {
                    val intent = Intent(getApplication(), PrayerNotificationReceiver::class.java).apply {
                        putExtra("PRAYER_NAME", name)
                        putExtra("MINUTES_LEFT", 0)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        getApplication(),
                        index + 200,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setExactAlarm(alarmManager, exactPrayerCal.timeInMillis, pendingIntent)
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

    private fun startCountdown(data: PrayerData) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val now = Calendar.getInstance()
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                
                val commonOffset = _timeOffset.value
                val timings = mapOf(
                    "İmsak" to applyOffset(data.timings.fajr, commonOffset + _imsakOffset.value),
                    "Sabah" to applyOffset(data.timings.sabah, commonOffset + _imsakOffset.value),
                    "Güneş" to applyOffset(data.timings.sunrise, commonOffset + _sunriseOffset.value),
                    "Öğle" to applyOffset(data.timings.dhuhr, commonOffset + _dhuhrOffset.value),
                    "İkindi" to applyOffset(data.timings.asr, commonOffset + _asrOffset.value),
                    "Akşam" to applyOffset(data.timings.maghrib, commonOffset + _maghribOffset.value),
                    "Yatsı" to applyOffset(data.timings.isha, commonOffset + _ishaOffset.value)
                )

                val sortedTimes = timings.mapNotNull { entry ->
                    try {
                        val dateParsed = format.parse(entry.value.substringBefore(" "))
                        val cal = Calendar.getInstance().apply {
                            val temp = Calendar.getInstance().apply { time = dateParsed!! }
                            set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                            set(Calendar.SECOND, 0)
                        }
                        entry.key to cal
                    } catch (e: Exception) { null }
                }.sortedBy { it.second.timeInMillis }

                var nextN = "İmsak"; var nextT: Calendar? = null; var currN = "Yatsı"
                for (i in sortedTimes.indices) {
                    if (now.before(sortedTimes[i].second)) {
                        nextN = sortedTimes[i].first; nextT = sortedTimes[i].second
                        currN = if (i == 0) "Yatsı" else sortedTimes[i-1].first; break
                    }
                }
                if (nextT == null) {
                    nextT = (sortedTimes.first().second.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
                }
                val diff = nextT!!.timeInMillis - now.timeInMillis
                val newState = CountdownState((diff/(1000*60*60)).toInt(), ((diff/(1000*60))%60).toInt(), ((diff/1000)%60).toInt(), nextN, currN)
                _countdownState.value = newState
                
                delay(1000)
            }
        }
    }

    companion object {
        val provinces = listOf(
            Province("Adana"), Province("Adıyaman"), Province("Afyonkarahisar"),
            Province("Ağrı"), Province("Amasya"), Province("Ankara"),
            Province("Antalya"), Province("Artvin"), Province("Aydın"),
            Province("Balıkesir"), Province("Bilecik"), Province("Bingöl"),
            Province("Bitlis"), Province("Bolu"), Province("Burdur"),
            Province("Bursa"), Province("Çanakkale"), Province("Çankırı"),
            Province("Çorum"), Province("Denizli"), Province("Diyarbakır"),
            Province("Edirne"), Province("Elazığ"), Province("Erzincan"),
            Province("Erzurum"), Province("Eskişehir"), Province("Gaziantep"),
            Province("Giresun"), Province("Gümüşhane"), Province("Hakkari"),
            Province("Hatay"), Province("Isparta"), Province("İçel"),
            Province("İstanbul"), Province("İzmir"), Province("Kars"),
            Province("Kastamonu"), Province("Kayseri"), Province("Kırklareli"),
            Province("Kırşehir"), Province("Kocaeli"), Province("Konya"),
            Province("Kütahya"), Province("Malatya"), Province("Manisa"),
            Province("Kahramanmaraş"), Province("Mardin"), Province("Muğla"),
            Province("Muş"), Province("Nevşehir"), Province("Niğde"),
            Province("Ordu"), Province("Rize"), Province("Sakarya"),
            Province("Samsun"), Province("Siirt"), Province("Sinop"),
            Province("Sivas"), Province("Tekirdağ"), Province("Tokat"),
            Province("Trabzon"), Province("Tunceli"), Province("Şanlıurfa"),
            Province("Uşak"), Province("Van"), Province("Yozgat"),
            Province("Zonguldak"), Province("Aksaray"), Province("Bayburt"),
            Province("Karaman"), Province("Kırıkkale"), Province("Batman"),
            Province("Şırnak"), Province("Bartın"), Province("Ardahan"),
            Province("Iğdır"), Province("Yalova"), Province("Karabük"),
            Province("Kilis"), Province("Osmaniye"), Province("Düzce"), Province("Mersin")
        ).let { list ->
            val collator = java.text.Collator.getInstance(java.util.Locale("tr", "TR"))
            list.sortedWith(compareBy(collator) { it.name })
        }
    }
}
