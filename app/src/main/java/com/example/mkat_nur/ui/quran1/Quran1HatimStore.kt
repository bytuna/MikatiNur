package com.example.mkat_nur.ui.quran1

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Kur'an-ı Kerim hatim takibi ve "son kalınan yer" kayıtları.
 * Eski Kur'an modülünün kayıtlarından ayrıdır ("quran1_prefs" içinde "q1_" önekli anahtarlar).
 */
object Quran1HatimStore {
    private var prefs: SharedPreferences? = null

    private val _startJuz = MutableStateFlow(1)
    val startJuz: StateFlow<Int> = _startJuz.asStateFlow()

    private val _completed = MutableStateFlow<Set<Int>>(emptySet())
    val completed: StateFlow<Set<Int>> = _completed.asStateFlow()

    private val _active = MutableStateFlow(false)
    val active: StateFlow<Boolean> = _active.asStateFlow()

    private val _assignments = MutableStateFlow<Map<Int, String>>(emptyMap())
    val assignments: StateFlow<Map<Int, String>> = _assignments.asStateFlow()

    private val _lastJuz = MutableStateFlow(1)
    val lastJuz: StateFlow<Int> = _lastJuz.asStateFlow()

    private val _lastPage = MutableStateFlow(1)
    val lastPage: StateFlow<Int> = _lastPage.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences("quran1_prefs", Context.MODE_PRIVATE)
        prefs = p
        _startJuz.value = p.getInt("q1_hatim_start", 1).coerceIn(1, 30)
        _completed.value = (p.getStringSet("q1_hatim_done", emptySet()) ?: emptySet())
            .mapNotNull { it.toIntOrNull() }.toSet()
        _active.value = p.getBoolean("q1_hatim_active", false)
        val map = mutableMapOf<Int, String>()
        for (i in 1..30) {
            val name = p.getString("q1_hatim_assign_$i", "") ?: ""
            if (name.isNotBlank()) map[i] = name
        }
        _assignments.value = map
        _lastJuz.value = p.getInt("q1_last_juz", 1).coerceIn(1, 30)
        _lastPage.value = p.getInt("q1_last_page", 1).coerceIn(1, 604)
    }

    fun sequence(startJuz: Int): List<Int> = (0..29).map { i -> ((startJuz - 1 + i) % 30) + 1 }

    fun start(startJuz: Int) {
        _startJuz.value = startJuz
        _completed.value = emptySet()
        _active.value = true
        prefs?.edit()
            ?.putInt("q1_hatim_start", startJuz)
            ?.putStringSet("q1_hatim_done", emptySet())
            ?.putBoolean("q1_hatim_active", true)
            ?.apply()
    }

    fun toggleCompleted(juz: Int) {
        val cur = _completed.value.toMutableSet()
        if (!cur.remove(juz)) cur.add(juz)
        _completed.value = cur
        prefs?.edit()?.putStringSet("q1_hatim_done", cur.map { it.toString() }.toSet())?.apply()
    }

    fun reset() {
        _active.value = false
        _completed.value = emptySet()
        prefs?.edit()?.putBoolean("q1_hatim_active", false)?.putStringSet("q1_hatim_done", emptySet())?.apply()
    }

    fun assign(juz: Int, person: String) {
        val cur = _assignments.value.toMutableMap()
        if (person.isBlank()) {
            cur.remove(juz)
            prefs?.edit()?.remove("q1_hatim_assign_$juz")?.apply()
        } else {
            cur[juz] = person.trim()
            prefs?.edit()?.putString("q1_hatim_assign_$juz", person.trim())?.apply()
        }
        _assignments.value = cur
    }

    fun saveLastJuz(juz: Int) {
        _lastJuz.value = juz
        prefs?.edit()?.putInt("q1_last_juz", juz)?.apply()
    }

    fun saveLastPage(page: Int) {
        _lastPage.value = page
        prefs?.edit()?.putInt("q1_last_page", page)?.apply()
    }

    fun shareText(): String {
        val seq = sequence(_startJuz.value)
        val done = _completed.value
        val assigned = _assignments.value
        val sb = StringBuilder()
        sb.append("🤲 MÎKAT-I NUR - 30 CÜZ HATİM DAĞITIM LİSTESİ 🤲\n\n")
        seq.forEachIndexed { idx, juz ->
            val person = assigned[juz]
            val status = when {
                done.contains(juz) -> "✅ (Tamamlandı${if (!person.isNullOrBlank()) " - $person" else ""})"
                !person.isNullOrBlank() -> "👤 $person (Alındı)"
                else -> "🟩 [BOŞTA - Alınabilir]"
            }
            sb.append("#${idx + 1} — $juz. Cüz: $status\n")
        }
        sb.append("\n📊 İlerleme: ${done.size} / 30 Cüz Tamamlandı\n")
        sb.append("Siz de cüz alarak hatimimize katılabilirsiniz! Rabbim kabul eylesin.\n")
        sb.append("Mîkat-ı Nur Kur'an-ı Kerim")
        return sb.toString()
    }
}
