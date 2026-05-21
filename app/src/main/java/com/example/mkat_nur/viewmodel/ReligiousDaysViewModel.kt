package com.example.mkat_nur.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mkat_nur.model.DiniGunResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class ReligiousDaysUiState {
    object Loading : ReligiousDaysUiState()
    data class Success(
        val days: List<DiniGunResponse>,
        val nextDayIndex: Int = -1,
        val isFromApi: Boolean = false,
        val lastUpdate: String = ""
    ) : ReligiousDaysUiState()
    data class Error(val message: String) : ReligiousDaysUiState()
}

class ReligiousDaysViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ReligiousDaysUiState>(ReligiousDaysUiState.Loading)
    val uiState: StateFlow<ReligiousDaysUiState> = _uiState

    fun loadReligiousDays(context: Context) {
        _uiState.value = ReligiousDaysUiState.Loading
        val nowStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date())
        
        viewModelScope.launch {
            var days: List<DiniGunResponse> = emptyList()
            var isFromApi = false
            
            try {
                // 1. Önce API'den çekmeyi dene
                android.util.Log.d("ReligiousDays", "Attempting API fetch...")
                days = RetrofitClient.diyanetInstance.getDiniGunler()
                if (days.isNotEmpty()) {
                    isFromApi = true
                    android.util.Log.d("ReligiousDays", "API fetch successful")
                }
            } catch (e: Exception) {
                android.util.Log.e("ReligiousDays", "API fetch failed: ${e.message}")
            }

            if (days.isEmpty()) {
                try {
                    // 2. API başarısızsa Diyanet sitesinden parse etmeyi dene
                    android.util.Log.d("ReligiousDays", "Attempting Diyanet HTML scrape...")
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    days = fetchFromDiyanetHtml(currentYear)
                    if (days.isNotEmpty()) {
                        isFromApi = true // Web sitesinden çekmek de "online" sayılır
                        android.util.Log.d("ReligiousDays", "Diyanet HTML scrape successful")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ReligiousDays", "Diyanet scrape failed: ${e.message}")
                }
            }

            if (days.isEmpty()) {
                try {
                    // 3. Online kaynaklar başarısızsa Asset dosyasından yerel veriyi oku
                    android.util.Log.d("ReligiousDays", "Attempting local asset load...")
                    val jsonString = context.assets.open("dini_gunler.json").bufferedReader().use { it.readText() }
                    val listType = object : TypeToken<List<DiniGunResponse>>() {}.type
                    days = Gson().fromJson(jsonString, listType)
                    isFromApi = false
                    android.util.Log.d("ReligiousDays", "Local asset load successful")
                } catch (e: Exception) {
                    android.util.Log.e("ReligiousDays", "Local load failed: ${e.message}")
                }
            }

            if (days.isNotEmpty()) {
                _uiState.value = ReligiousDaysUiState.Success(
                    days = days,
                    nextDayIndex = calculateNextDayIndex(days),
                    isFromApi = isFromApi,
                    lastUpdate = nowStr
                )
            } else {
                // Her şey başarısız olursa fallback listesini kullan
                val fallback = getFallbackDays()
                _uiState.value = ReligiousDaysUiState.Success(
                    days = fallback,
                    nextDayIndex = calculateNextDayIndex(fallback),
                    isFromApi = false,
                    lastUpdate = nowStr
                )
            }
        }
    }

    private suspend fun fetchFromDiyanetHtml(year: Int): List<DiniGunResponse> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val daysList = mutableListOf<DiniGunResponse>()
            try {
                val url = java.net.URL("https://vakithesaplama.diyanet.gov.tr/dinigunler.php?yil=$year")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val html = connection.inputStream.bufferedReader().use { it.readText() }
                
                // Basit bir HTML tablo parselleyici (Regex ile)
                // <tr> ... </tr> bloklarını bul
                val rowRegex = Regex("<tr>(.*?)</tr>", RegexOption.DOT_MATCHES_ALL)
                val cellRegex = Regex("<td.*?>\\s*(.*?)\\s*</td>", RegexOption.DOT_MATCHES_ALL)
                
                val rows = rowRegex.findAll(html)
                for (rowMatch in rows) {
                    val rowHtml = rowMatch.groupValues[1]
                    val cells = cellRegex.findAll(rowHtml).map { it.groupValues[1].replace(Regex("<.*?>"), "").trim() }.toList()
                    
                    // Diyanet tablosu genellikle 7 sütunludur: 
                    // Hicri Gün, Ay, Yıl, Miladi Gün, Ay-Yıl, Haftanın Günü, Dini Gün Adı
                    if (cells.size >= 7) {
                        val ad = cells[6]
                        // Başlık satırı veya boş satır değilse ekle
                        if (ad.isNotEmpty() && ad != "DİNİ GÜNLER" && !ad.contains("...")) {
                            val miladiDay = cells[3]
                            val miladiMonthYear = cells[4] // Örn: "OCAK-2026"
                            
                            // Miladi tarihi formatla: "02.01.2026" gibi
                            val parts = miladiMonthYear.split("-")
                            if (parts.size >= 2) {
                                val monthStr = parts[0]
                                val yearStr = parts[1]
                                val monthNum = when(monthStr.uppercase(Locale("tr", "TR"))) {
                                    "OCAK" -> "01"; "ŞUBAT" -> "02"; "MART" -> "03"; "NİSAN" -> "04"
                                    "MAYIS" -> "05"; "HAZİRAN" -> "06"; "TEMMUZ" -> "07"; "AĞUSTOS" -> "08"
                                    "EYLÜL" -> "09"; "EKİM" -> "10"; "KASIM" -> "11"; "ARALIK" -> "12"
                                    else -> "01"
                                }
                                val formattedDate = "${miladiDay.padStart(2, '0')}.$monthNum.$yearStr"
                                val hicriTarih = "${cells[0]} ${cells[1]} ${cells[2]}"
                                
                                daysList.add(DiniGunResponse(ad, formattedDate, hicriTarih))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ReligiousDays", "Scrape error: ${e.message}")
            }
            daysList
        }
    }

    private fun calculateNextDayIndex(days: List<DiniGunResponse>): Int {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val formats = listOf(
            SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")),
            SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US), // ISO Format
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        )
        
        for (i in days.indices) {
            val dateStr = days[i].tarih ?: continue
            for (format in formats) {
                try {
                    val dayDate = format.parse(dateStr)
                    if (dayDate != null && (dayDate.after(now) || isSameDay(dayDate, now))) {
                        return i
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return -1
    }

    private fun isSameDay(d1: Date, d2: Date): Boolean {
        val c1 = Calendar.getInstance().apply { time = d1 }
        val c2 = Calendar.getInstance().apply { time = d2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
               c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun getFallbackDays(): List<DiniGunResponse> {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return when (year) {
            2025 -> listOf(
                DiniGunResponse("Üç Ayların Başlangıcı", "1 Ocak 2025", "1 Receb 1446"),
                DiniGunResponse("Regaip Kandili", "2 Ocak 2025", "2 Receb 1446"),
                DiniGunResponse("Miraç Kandili", "26 Ocak 2025", "26 Receb 1446"),
                DiniGunResponse("Berat Kandili", "13 Şubat 2025", "15 Şaban 1446"),
                DiniGunResponse("Ramazan Başlangıcı", "1 Mart 2025", "1 Ramazan 1446"),
                DiniGunResponse("Kadir Gecesi", "26 Mart 2025", "26 Ramazan 1446"),
                DiniGunResponse("Arefe", "29 Mart 2025", "29 Ramazan 1446"),
                DiniGunResponse("Ramazan Bayramı (1. Gün)", "30 Mart 2025", "1 Şevval 1446"),
                DiniGunResponse("Ramazan Bayramı (2. Gün)", "31 Mart 2025", "2 Şevval 1446"),
                DiniGunResponse("Ramazan Bayramı (3. Gün)", "1 Nisan 2025", "3 Şevval 1446"),
                DiniGunResponse("Arefe", "5 Haziran 2025", "9 Zilhicce 1446"),
                DiniGunResponse("Kurban Bayramı (1. Gün)", "6 Haziran 2025", "10 Zilhicce 1446"),
                DiniGunResponse("Kurban Bayramı (2. Gün)", "7 Haziran 2025", "11 Zilhicce 1446"),
                DiniGunResponse("Kurban Bayramı (3. Gün)", "8 Haziran 2025", "12 Zilhicce 1446"),
                DiniGunResponse("Kurban Bayramı (4. Gün)", "9 Haziran 2025", "13 Zilhicce 1446"),
                DiniGunResponse("Hicri Yılbaşı", "26 Haziran 2025", "1 Muharrem 1447"),
                DiniGunResponse("Aşure Günü", "5 Temmuz 2025", "10 Muharrem 1447"),
                DiniGunResponse("Mevlid Kandili", "3 Eylül 2025", "11 Rebiülevvel 1447")
            )
            2026 -> listOf(
                DiniGunResponse("Miraç Kandili", "15 Ocak 2026", "26 Receb 1447"),
                DiniGunResponse("Berat Kandili", "2 Şubat 2026", "14 Şaban 1447"),
                DiniGunResponse("Ramazan Başlangıcı", "19 Şubat 2026", "1 Ramazan 1447"),
                DiniGunResponse("Kadir Gecesi", "16 Mart 2026", "26 Ramazan 1447"),
                DiniGunResponse("Arefe", "19 Mart 2026", "29 Ramazan 1447"),
                DiniGunResponse("Ramazan Bayramı (1. Gün)", "20 Mart 2026", "1 Şevval 1447"),
                DiniGunResponse("Ramazan Bayramı (2. Gün)", "21 Mart 2026", "2 Şevval 1447"),
                DiniGunResponse("Ramazan Bayramı (3. Gün)", "22 Mart 2026", "3 Şevval 1447"),
                DiniGunResponse("Arefe", "26 Mayıs 2026", "9 Zilhicce 1447"),
                DiniGunResponse("Kurban Bayramı (1. Gün)", "27 Mayıs 2026", "10 Zilhicce 1447"),
                DiniGunResponse("Kurban Bayramı (2. Gün)", "28 Mayıs 2026", "11 Zilhicce 1447"),
                DiniGunResponse("Kurban Bayramı (3. Gün)", "29 Mayıs 2026", "12 Zilhicce 1447"),
                DiniGunResponse("Kurban Bayramı (4. Gün)", "30 Mayıs 2026", "13 Zilhicce 1447"),
                DiniGunResponse("Hicri Yılbaşı", "16 Haziran 2026", "1 Muharrem 1448"),
                DiniGunResponse("Aşure Günü", "25 Haziran 2026", "10 Muharrem 1448"),
                DiniGunResponse("Mevlid Kandili", "24 Ağustos 2026", "11 Rebiülevvel 1448"),
                DiniGunResponse("Üç Ayların Başlangıcı", "10 Aralık 2026", "1 Receb 1448"),
                DiniGunResponse("Regaip Kandili", "10 Aralık 2026", "1 Receb 1448")
            )
            else -> listOf(
                DiniGunResponse("Veri Bekleniyor", "01.01.$year", "")
            )
        }
    }
}
