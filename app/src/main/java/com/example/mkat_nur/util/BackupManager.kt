package com.example.mkat_nur.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Yedekle / geri yükle.
 *
 * Yedek dosyası düz bir JSON'dur ve Belgeler/MikatNur klasörüne kaydedilir.
 * İçeriği: ayarlar (tema, bildirimler, vakit dakika ayarları), konum, kaza borçları,
 * zikir sayaçları ve kendi zikirleriniz, Kur'an yer imleri, son kalınan yer ve ayet,
 * hatim ilerlemesi (cüz dağıtımı, tamamlananlar) ve Kur'an okuma tercihleri.
 * Hesap bilgisi, şifre ve bildirim sesi gibi cihaza özgü şeyler yedeğe girmez.
 */
object BackupManager {
    private const val MAIN_PREFS = "mkat_nur_prefs"
    private const val KAZA_PREFS = "kaza_prefs"
    private const val QURAN1_PREFS = "quran1_prefs"
    private const val DHIKR_PREFS = "dhikr_prefs"
    private const val FORMAT = 1
    private const val APP_ID = "mikat-nur"
    private const val FOLDER = "MikatNur"
    private const val MAX_BYTES = 2 * 1024 * 1024

    private enum class T { BOOL, INT, LONG, FLOAT, STRING, STRING_SET }

    private data class Spec(val key: String, val type: T, val min: Double? = null, val max: Double? = null)

    private val PRAYER_SOUND_KEYS = listOf("sabah", "ogle", "ikindi", "aksam", "yatsi", "imsak")

    private val SETTINGS = buildList {
        add(Spec("is_dark_mode", T.BOOL))
        add(Spec("selected_app_theme", T.STRING))
        add(Spec("font_size", T.FLOAT, 8.0, 40.0))
        add(Spec("daily_content_type", T.STRING))
        add(Spec("show_ayet", T.BOOL)); add(Spec("show_hadis", T.BOOL))
        add(Spec("show_vecize", T.BOOL)); add(Spec("show_esma", T.BOOL))
        add(Spec("highlight_color", T.INT)); add(Spec("sliding_duration", T.FLOAT, 1.0, 120.0))
        add(Spec("auto_location_interval", T.INT, 0.0, 168.0))
        add(Spec("is_women_special", T.BOOL))
        // Bildirimler
        add(Spec("reminder_minutes_set", T.STRING_SET))
        listOf("imsak", "sunrise", "dhuhr", "asr", "maghrib", "isha", "kerahat")
            .forEach { add(Spec("notify_$it", T.BOOL)) }
        PRAYER_SOUND_KEYS.forEach { add(Spec("prayer_sound_mode_$it", T.STRING)) }
        // Vakit dakika ayarları
        add(Spec("time_offset", T.INT, -180.0, 180.0))
        listOf("imsak", "sunrise", "dhuhr", "asr", "maghrib", "isha")
            .forEach { add(Spec("offset_$it", T.INT, -180.0, 180.0)) }
        // Widget görünümü
        add(Spec("widget_transparency", T.FLOAT, 0.0, 1.0)); add(Spec("widget_title_color", T.INT))
        add(Spec("widget_text_color", T.INT)); add(Spec("widget_font_size", T.FLOAT, 6.0, 40.0))
    }

    private val LOCATION = listOf(
        Spec("selected_city_name", T.STRING),
        Spec("selected_city_id", T.STRING)
    )

    private val KAZA = listOf("fajr_debt", "dhuhr_debt", "asr_debt", "maghrib_debt", "isha_debt", "witr_debt")
        .map { Spec(it, T.INT, 0.0, 1_000_000.0) }

    private val ZIKIR = listOf(
        Spec(StatsStore.KEY_ZIKIR_TOTAL, T.LONG, 0.0, 9.0e15),
        Spec(StatsStore.KEY_ZIKIR_CURRENT, T.INT, 0.0, 1.0e9),
        Spec(StatsStore.KEY_ZIKIR_DAYS, T.STRING)
    )

    private val QURAN = listOf(
        Spec("quran_bookmarks", T.STRING_SET),
        Spec("quran_last_juz_tab_read_juz", T.INT, 1.0, 30.0),
        Spec("quran_last_juz_tab_read_page", T.INT, 1.0, 604.0)
    )

    /** Kur'an-ı Kerim: hatim ilerlemesi, son kalınan yerler ve okuma tercihleri ("quran1_prefs"). */
    private val QURAN1 = buildList {
        add(Spec("q1_hatim_start", T.INT, 1.0, 30.0))
        add(Spec("q1_hatim_done", T.STRING_SET))
        add(Spec("q1_hatim_active", T.BOOL))
        for (i in 1..30) add(Spec("q1_hatim_assign_$i", T.STRING))
        add(Spec("q1_last_juz", T.INT, 1.0, 30.0))
        add(Spec("q1_last_page", T.INT, 1.0, 604.0))
        add(Spec("q1_positions", T.STRING))
        add(Spec("mealli", T.BOOL)); add(Spec("tajweed", T.BOOL))
        add(Spec("font_size", T.FLOAT, 18.0, 48.0)); add(Spec("font_key", T.STRING))
        add(Spec("auto_scroll", T.BOOL)); add(Spec("reciter_id", T.STRING))
        add(Spec("playback_speed", T.FLOAT, 1.0, 2.0))
    }

    /** Zikirmatik: kendi zikirleriniz, zikir sayaçları ve hedefleri ("dhikr_prefs"). */
    private val DHIKR = listOf(
        Spec("custom_list", T.STRING), Spec("counts", T.STRING),
        Spec("targets", T.STRING), Spec("selected_id", T.STRING)
    )

    // ------------------------------------------------------------------ Dışa aktar

    data class ExportResult(val fileName: String, val displayPath: String)

    fun export(context: Context): ExportResult {
        val app = context.applicationContext
        val json = buildJson(app).toString(2)
        val stamp = SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.US).format(Date())
        val fileName = "mikat-nur-yedek-$stamp.json"
        val bytes = json.toByteArray(Charsets.UTF_8)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/$FOLDER")
            }
            val resolver = app.contentResolver
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
                ?: throw IllegalStateException("Dosya oluşturulamadı")
            try {
                resolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: throw IllegalStateException("Dosya yazılamadı")
            } catch (e: Exception) {
                try { resolver.delete(uri, null, null) } catch (_: Exception) { }
                throw e
            }
            return ExportResult(fileName, "Belgeler/$FOLDER/$fileName")
        }

        // Android 7-9: ek izin istememek için uygulamaya ait Belgeler klasörü kullanılır
        val dir = File(app.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), FOLDER)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        file.writeBytes(bytes)
        return ExportResult(fileName, file.absolutePath)
    }

    private fun buildJson(context: Context): JSONObject {
        val main = context.getSharedPreferences(MAIN_PREFS, Context.MODE_PRIVATE)
        val kaza = context.getSharedPreferences(KAZA_PREFS, Context.MODE_PRIVATE)
        return JSONObject().apply {
            put("app", APP_ID)
            put("format", FORMAT)
            put("appVersion", AppConfig.VERSION_NAME)
            put("exportedAt", System.currentTimeMillis())
            put("settings", readSection(main, SETTINGS))
            put("location", readSection(main, LOCATION))
            put("kaza", readSection(kaza, KAZA))
            put("zikir", readSection(main, ZIKIR))
            put("quran", readSection(main, QURAN))
            put("quran1", readSection(context.getSharedPreferences(QURAN1_PREFS, Context.MODE_PRIVATE), QURAN1))
            put("dhikr", readSection(context.getSharedPreferences(DHIKR_PREFS, Context.MODE_PRIVATE), DHIKR))
        }
    }

    private fun readSection(prefs: android.content.SharedPreferences, specs: List<Spec>): JSONObject {
        val obj = JSONObject()
        for (s in specs) {
            if (!prefs.contains(s.key)) continue
            try {
                when (s.type) {
                    T.BOOL -> obj.put(s.key, prefs.getBoolean(s.key, false))
                    T.INT -> obj.put(s.key, prefs.getInt(s.key, 0))
                    T.LONG -> obj.put(s.key, prefs.getLong(s.key, 0L))
                    T.FLOAT -> obj.put(s.key, prefs.getFloat(s.key, 0f).toDouble())
                    T.STRING -> prefs.getString(s.key, null)?.let { obj.put(s.key, it) }
                    T.STRING_SET -> obj.put(s.key, JSONArray((prefs.getStringSet(s.key, emptySet()) ?: emptySet()).toList()))
                }
            } catch (_: ClassCastException) {
                // Beklenmedik tipte kayıt varsa o anahtarı atla
            }
        }
        return obj
    }

    // ------------------------------------------------------------------ İçe aktar

    /** İçe aktarma ekranını (dosya seçici) Belgeler/MikatNur klasöründe açan niyet. */
    fun pickerIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain", "application/octet-stream"))
            try {
                putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    DocumentsContract.buildDocumentUri(
                        "com.android.externalstorage.documents",
                        "primary:Documents/$FOLDER"
                    )
                )
            } catch (_: Exception) { }
        }
    }

    data class ImportResult(val restoredCount: Int, val kazaForCloud: Map<String, Int>?)

    class InvalidBackupException(message: String) : Exception(message)

    fun restore(context: Context, uri: Uri): ImportResult {
        val app = context.applicationContext
        val text = app.contentResolver.openInputStream(uri)?.use { input ->
            val buf = java.io.ByteArrayOutputStream()
            val chunk = ByteArray(8192)
            var total = 0
            while (true) {
                val n = input.read(chunk)
                if (n < 0) break
                total += n
                if (total > MAX_BYTES) throw InvalidBackupException("Dosya çok büyük, geçerli bir yedek değil.")
                buf.write(chunk, 0, n)
            }
            buf.toString("UTF-8")
        } ?: throw InvalidBackupException("Dosya okunamadı.")

        val root = try { JSONObject(text) } catch (e: Exception) {
            throw InvalidBackupException("Bu dosya bir Mîkat-ı Nur yedeği değil.")
        }
        if (root.optString("app") != APP_ID) throw InvalidBackupException("Bu dosya bir Mîkat-ı Nur yedeği değil.")
        if (root.optInt("format", 0) > FORMAT) throw InvalidBackupException("Bu yedek daha yeni bir sürümle alınmış. Uygulamayı güncelleyin.")

        val main = app.getSharedPreferences(MAIN_PREFS, Context.MODE_PRIVATE).edit()
        val kaza = app.getSharedPreferences(KAZA_PREFS, Context.MODE_PRIVATE).edit()
        val quran1 = app.getSharedPreferences(QURAN1_PREFS, Context.MODE_PRIVATE).edit()
        val dhikr = app.getSharedPreferences(DHIKR_PREFS, Context.MODE_PRIVATE).edit()

        var count = 0
        count += applySection(root.optJSONObject("settings"), SETTINGS, main)
        count += applySection(root.optJSONObject("location"), LOCATION, main)
        count += applySection(root.optJSONObject("zikir"), ZIKIR, main)
        count += applySection(root.optJSONObject("quran"), QURAN, main)
        count += applySection(root.optJSONObject("quran1"), QURAN1, quran1)
        count += applySection(root.optJSONObject("dhikr"), DHIKR, dhikr)
        val kazaCount = applySection(root.optJSONObject("kaza"), KAZA, kaza)
        count += kazaCount

        if (count == 0) throw InvalidBackupException("Yedekte geri yüklenecek bir bilgi bulunamadı.")

        // Eski dakika ayarı anahtarlarından kalan değerler yeni değerleri ezmesin
        main.commit()
        kaza.commit()
        quran1.commit()
        dhikr.commit()

        val kazaMap: Map<String, Int>? = if (kazaCount > 0) {
            val p = app.getSharedPreferences(KAZA_PREFS, Context.MODE_PRIVATE)
            mapOf(
                "fajr" to p.getInt("fajr_debt", 0), "dhuhr" to p.getInt("dhuhr_debt", 0),
                "asr" to p.getInt("asr_debt", 0), "maghrib" to p.getInt("maghrib_debt", 0),
                "isha" to p.getInt("isha_debt", 0), "witr" to p.getInt("witr_debt", 0)
            )
        } else null

        return ImportResult(count, kazaMap)
    }

    private fun applySection(
        obj: JSONObject?,
        specs: List<Spec>,
        editor: android.content.SharedPreferences.Editor
    ): Int {
        if (obj == null) return 0
        var applied = 0
        for (s in specs) {
            if (!obj.has(s.key) || obj.isNull(s.key)) continue
            try {
                when (s.type) {
                    T.BOOL -> editor.putBoolean(s.key, obj.getBoolean(s.key))
                    T.INT -> editor.putInt(s.key, clamp(obj.getLong(s.key).toDouble(), s).toInt())
                    T.LONG -> editor.putLong(s.key, clamp(obj.getLong(s.key).toDouble(), s).toLong())
                    T.FLOAT -> editor.putFloat(s.key, clamp(obj.getDouble(s.key), s).toFloat())
                    T.STRING -> editor.putString(s.key, obj.getString(s.key).take(40000))
                    T.STRING_SET -> {
                        val arr = obj.getJSONArray(s.key)
                        val set = HashSet<String>()
                        for (i in 0 until minOf(arr.length(), 5000)) set.add(arr.getString(i).take(100))
                        editor.putStringSet(s.key, set)
                    }
                }
                applied++
            } catch (_: Exception) {
                // Tipi uymayan değer atlanır; diğerleri geri yüklenmeye devam eder
            }
        }
        return applied
    }

    private fun clamp(v: Double, s: Spec): Double {
        var r = v
        s.min?.let { if (r < it) r = it }
        s.max?.let { if (r > it) r = it }
        return r
    }

    /** Giriş yapılmışsa geri yüklenen kaza borçlarını buluta da yazar (açılışta buluttan ezilmesin). */
    fun pushKazaToCloud(kaza: Map<String, Int>) {
        try {
            val user = FirebaseAuth.getInstance().currentUser ?: return
            val data = HashMap<String, Any>(kaza)
            data["last_updated"] = System.currentTimeMillis()
            data["user_email"] = user.email ?: ""
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .collection("data").document("kaza_debt")
                .set(data, SetOptions.merge())
        } catch (_: Exception) { }
    }

    /** Uygulamayı baştan başlatır; geri yüklenen ayarlar böylece her yerde geçerli olur. */
    fun restartApp(context: Context) {
        val app = context.applicationContext
        val launch = app.packageManager.getLaunchIntentForPackage(app.packageName) ?: return
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        app.startActivity(launch)
        Runtime.getRuntime().exit(0)
    }
}
