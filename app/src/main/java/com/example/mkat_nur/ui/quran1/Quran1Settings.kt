package com.example.mkat_nur.ui.quran1

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Kur'an-ı Kerim 1 tercihleri. Hem okuma ekranı hem Ayarlar ekranı aynı durumu paylaşsın diye
 * tekil nesnedir; değerler "quran1_prefs" dosyasında saklanır.
 */
object Quran1Settings {
    private var prefs: SharedPreferences? = null

    private val _mealli = MutableStateFlow(true)
    val mealli: StateFlow<Boolean> = _mealli.asStateFlow()

    private val _tajweed = MutableStateFlow(true)
    val tajweed: StateFlow<Boolean> = _tajweed.asStateFlow()

    private val _fontSize = MutableStateFlow(28f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _fontKey = MutableStateFlow("uthman_taha")
    val fontKey: StateFlow<String> = _fontKey.asStateFlow()

    private val _autoScroll = MutableStateFlow(true)
    val autoScroll: StateFlow<Boolean> = _autoScroll.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences("quran1_prefs", Context.MODE_PRIVATE)
        prefs = p
        _mealli.value = p.getBoolean("mealli", true)
        _tajweed.value = p.getBoolean("tajweed", true)
        _fontSize.value = p.getFloat("font_size", 28f).coerceIn(18f, 48f)
        _fontKey.value = p.getString("font_key", "uthman_taha") ?: "uthman_taha"
        _autoScroll.value = p.getBoolean("auto_scroll", true)
    }

    fun setMealli(v: Boolean) { _mealli.value = v; prefs?.edit()?.putBoolean("mealli", v)?.apply() }
    fun setTajweed(v: Boolean) { _tajweed.value = v; prefs?.edit()?.putBoolean("tajweed", v)?.apply() }
    fun setFontSize(v: Float) { _fontSize.value = v; prefs?.edit()?.putFloat("font_size", v)?.apply() }
    fun setFontKey(v: String) { _fontKey.value = v; prefs?.edit()?.putString("font_key", v)?.apply() }
    fun setAutoScroll(v: Boolean) { _autoScroll.value = v; prefs?.edit()?.putBoolean("auto_scroll", v)?.apply() }

    fun resetToDefaults() {
        setMealli(true); setTajweed(true); setFontSize(28f); setFontKey("uthman_taha"); setAutoScroll(true)
    }
}
