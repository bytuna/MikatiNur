package com.example.mkat_nur.util

import android.content.Context
import java.util.Calendar

/**
 * Profil ekranındaki özetler için yalnızca bu telefonda tutulan sayaçlar.
 * Hiçbir yere gönderilmez; yedeğe sadece zikir sayıları dahildir.
 *
 * Not: Android 7 (API 24) desteklendiği için java.time yerine Calendar kullanılır.
 */
object StatsStore {
    private const val PREFS = "mkat_nur_prefs"

    const val KEY_LAST_OPEN_DAY = "stats_last_open_day"
    const val KEY_STREAK = "stats_streak"
    const val KEY_BEST_STREAK = "stats_best_streak"
    const val KEY_TOTAL_DAYS = "stats_total_days"
    const val KEY_FIRST_DAY = "stats_first_day"
    const val KEY_ZIKIR_TOTAL = "zikir_total"
    const val KEY_ZIKIR_CURRENT = "zikir_current"
    const val KEY_ZIKIR_DAYS = "stats_zikir_days"
    const val KEY_KAZA_DAYS = "stats_kaza_days"

    private const val KEEP_DAYS = 60

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Yerel saate göre "gün numarası" (gece yarısında değişir). */
    fun today(): Long {
        val c = Calendar.getInstance()
        return (c.timeInMillis + c.timeZone.getOffset(c.timeInMillis)) / 86_400_000L
    }

    // ---------------------------------------------------------------- Kullanım serisi

    data class Streak(val current: Int, val best: Int, val totalDays: Int, val firstDay: Long)

    /** Uygulama açıldığında çağrılır; aynı gün birden çok kez çağrılması sorun değildir. */
    fun recordAppOpen(context: Context) {
        try {
            val p = prefs(context)
            val today = today()
            val last = p.getLong(KEY_LAST_OPEN_DAY, -1L)
            if (last == today) return

            val streak = if (last == today - 1) p.getInt(KEY_STREAK, 0) + 1 else 1
            val best = maxOf(p.getInt(KEY_BEST_STREAK, 0), streak)
            val edit = p.edit()
                .putLong(KEY_LAST_OPEN_DAY, today)
                .putInt(KEY_STREAK, streak)
                .putInt(KEY_BEST_STREAK, best)
                .putInt(KEY_TOTAL_DAYS, p.getInt(KEY_TOTAL_DAYS, 0) + 1)
            if (!p.contains(KEY_FIRST_DAY)) edit.putLong(KEY_FIRST_DAY, today)
            edit.apply()
        } catch (_: Exception) { }
    }

    fun streak(context: Context): Streak {
        val p = prefs(context)
        val last = p.getLong(KEY_LAST_OPEN_DAY, -1L)
        val today = today()
        // Dün veya bugün açıldıysa seri sürüyor; aradan gün geçtiyse cezalandırmadan sıfırdan sayılır
        val current = if (last >= today - 1) p.getInt(KEY_STREAK, 0) else 0
        return Streak(
            current = current,
            best = p.getInt(KEY_BEST_STREAK, 0),
            totalDays = p.getInt(KEY_TOTAL_DAYS, 0),
            firstDay = p.getLong(KEY_FIRST_DAY, today)
        )
    }

    // ---------------------------------------------------------------- Günlük sayaçlar

    private fun readDays(raw: String?): MutableMap<Long, Int> {
        val map = mutableMapOf<Long, Int>()
        if (raw.isNullOrBlank()) return map
        raw.split(",").forEach { part ->
            val kv = part.split(":")
            val day = kv.getOrNull(0)?.toLongOrNull()
            val count = kv.getOrNull(1)?.toIntOrNull()
            if (day != null && count != null) map[day] = count
        }
        return map
    }

    private fun writeDays(map: Map<Long, Int>): String {
        val min = today() - KEEP_DAYS
        return map.filterKeys { it >= min }.entries.joinToString(",") { "${it.key}:${it.value}" }
    }

    private fun addToDay(context: Context, key: String, amount: Int) {
        if (amount <= 0) return
        val p = prefs(context)
        val map = readDays(p.getString(key, null))
        val today = today()
        map[today] = (map[today] ?: 0) + amount
        p.edit().putString(key, writeDays(map)).apply()
    }

    private fun lastSevenDays(context: Context, key: String): Int {
        val map = readDays(prefs(context).getString(key, null))
        val today = today()
        return (0..6).sumOf { map[today - it] ?: 0 }
    }

    // ---------------------------------------------------------------- Kaza

    fun addKazaPrayed(context: Context, amount: Int) = addToDay(context, KEY_KAZA_DAYS, amount)

    fun kazaPrayedLast7Days(context: Context): Int = lastSevenDays(context, KEY_KAZA_DAYS)

    /** Kalan toplam kaza borcu (bu telefondaki kayıttan). */
    fun kazaRemaining(context: Context): Int {
        val p = context.applicationContext.getSharedPreferences("kaza_prefs", Context.MODE_PRIVATE)
        return listOf("fajr_debt", "dhuhr_debt", "asr_debt", "maghrib_debt", "isha_debt", "witr_debt")
            .sumOf { p.getInt(it, 0) }
    }

    // ---------------------------------------------------------------- Zikir

    fun zikirCurrent(context: Context): Int = prefs(context).getInt(KEY_ZIKIR_CURRENT, 0)

    fun zikirTotal(context: Context): Long = prefs(context).getLong(KEY_ZIKIR_TOTAL, 0L)

    fun zikirLast7Days(context: Context): Int = lastSevenDays(context, KEY_ZIKIR_DAYS)

    /** Zikirmatikte bir kez basıldığında çağrılır; yeni sayaç değerini döndürür. */
    fun zikirIncrement(context: Context): Int {
        val p = prefs(context)
        val next = p.getInt(KEY_ZIKIR_CURRENT, 0) + 1
        p.edit()
            .putInt(KEY_ZIKIR_CURRENT, next)
            .putLong(KEY_ZIKIR_TOTAL, p.getLong(KEY_ZIKIR_TOTAL, 0L) + 1)
            .apply()
        addToDay(context, KEY_ZIKIR_DAYS, 1)
        return next
    }

    /** Sayacı sıfırlar; toplam zikir sayısı korunur. */
    fun zikirResetCurrent(context: Context) {
        prefs(context).edit().putInt(KEY_ZIKIR_CURRENT, 0).apply()
    }

    // ---------------------------------------------------------------- Kur'an

    data class QuranPosition(val juz: Int, val page: Int)

    /** Cüzler sekmesinde en son kalınan yer; hiç okuma kaydı yoksa null. */
    fun quranPosition(context: Context): QuranPosition? {
        val p = prefs(context)
        if (!p.contains("quran_last_juz_tab_read_juz")) return null
        return QuranPosition(
            p.getInt("quran_last_juz_tab_read_juz", 1),
            p.getInt("quran_last_juz_tab_read_page", 1)
        )
    }

    // ---------------------------------------------------------------- Hesap silinirken

    /** Hesap silinince profille ilgili yerel kayıtlar temizlenir (kaza, ayar ve konum kalır). */
    fun clearProfileStats(context: Context) {
        prefs(context).edit()
            .remove(KEY_LAST_OPEN_DAY).remove(KEY_STREAK).remove(KEY_BEST_STREAK)
            .remove(KEY_TOTAL_DAYS).remove(KEY_FIRST_DAY)
            .remove(KEY_ZIKIR_TOTAL).remove(KEY_ZIKIR_CURRENT).remove(KEY_ZIKIR_DAYS)
            .remove(KEY_KAZA_DAYS)
            .apply()
    }
}
