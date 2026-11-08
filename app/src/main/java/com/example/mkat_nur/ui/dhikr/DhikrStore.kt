package com.example.mkat_nur.ui.dhikr

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Zikir(
    val id: String,
    val title: String,
    val arabic: String,
    val meaning: String,
    val purpose: String,
    val defaultTarget: Int,
    val custom: Boolean = false
)

/** Hazır zikirler: Arapça metin, Türkçe anlam ve "ne için çekilir" notu. */
object DhikrCatalog {
    val builtIn = listOf(
        Zikir(
            "subhanallah", "Sübhânallah", "سُبْحَانَ اللَّهِ",
            "Allah'ı her türlü eksiklikten uzak tutarım.",
            "Namazların ardından yapılan tesbihatın ilk bölümüdür (33 defa). Allah'ı yüceltmek ve kalbi dünya meşguliyetinden sakinleştirmek için çekilir.",
            33
        ),
        Zikir(
            "elhamdulillah", "Elhamdülillâh", "الْحَمْدُ لِلَّهِ",
            "Hamd (övgü ve şükür) Allah'a mahsustur.",
            "Namaz sonrası tesbihatın ikinci bölümü (33 defa). Nimetlere şükretmek ve şükür bilincini canlı tutmak için çekilir.",
            33
        ),
        Zikir(
            "allahuekber", "Allâhü Ekber", "اللَّهُ أَكْبَرُ",
            "Allah en büyüktür.",
            "Namaz sonrası tesbihatın üçüncü bölümü (33 defa). Dertlerin ve korkuların karşısında Allah'ın büyüklüğünü hatırlamak için çekilir.",
            33
        ),
        Zikir(
            "tevhid", "Lâ ilâhe illallah", "لَا إِلَٰهَ إِلَّا اللَّهُ",
            "Allah'tan başka ilah yoktur.",
            "Tevhid kelimesidir; en faziletli zikirlerden sayılır. İmanı tazelemek, kalbi yalnız Allah'a bağlamak ve günün her anında çekilmek için uygundur.",
            100
        ),
        Zikir(
            "istigfar", "Estağfirullah", "أَسْتَغْفِرُ اللَّهَ",
            "Allah'tan bağışlanma dilerim.",
            "Günahlardan tövbe ve bağışlanma dilemek için çekilir. Peygamberimizin (s.a.v.) günde yüz kez istiğfar ettiği rivayet edilir. Sıkıntı ve kalp darlığında da tavsiye edilir.",
            100
        ),
        Zikir(
            "salavat", "Salavat-ı Şerife", "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ",
            "Allah'ım! Muhammed'e ve Muhammed'in âline rahmet eyle.",
            "Peygamber Efendimize (s.a.v.) salât ve selam getirmek için çekilir. Özellikle cuma günü ve gecesi çokça salavat getirilmesi tavsiye edilir; dua öncesi ve sonrası da okunur.",
            100
        ),
        Zikir(
            "subhanallahi_bihamdihi", "Sübhânallâhi ve bihamdihî", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ سُبْحَانَ اللَّهِ الْعَظِيمِ",
            "Allah'ı hamd ile tesbih ederim; yüce Allah'ı eksikliklerden uzak tutarım.",
            "Dile kolay, mizanda ağır olduğu bildirilen iki kelimedir. Sabah ve akşam çekilmek, günlük tesbihatı zenginleştirmek için uygundur.",
            100
        ),
        Zikir(
            "havkale", "Lâ havle velâ kuvvete illâ billâh", "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            "Güç ve kuvvet ancak Allah'ın yardımıyladır.",
            "Cennet hazinelerinden bir hazine olarak anılır. Zor iş, hastalık, sıkıntı ve çaresizlikte Allah'a sığınmak ve teslimiyeti hatırlamak için çekilir.",
            100
        ),
        Zikir(
            "hasbunallah", "Hasbünallâhü ve ni'mel vekîl", "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
            "Allah bize yeter; O ne güzel vekildir.",
            "Âl-i İmrân 173. ayette geçer. Endişe, korku, zor karar ve haksızlık karşısında tevekkül etmek için çekilir.",
            100
        ),
        Zikir(
            "hasbiyallah", "Hasbiyallâhü lâ ilâhe illâ hû", "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            "Bana Allah yeter. O'ndan başka ilah yoktur. Yalnız O'na dayandım, O büyük arşın Rabbidir.",
            "Tevbe 129. ayette geçer. Sabah ve akşam yedişer defa okunduğu rivayet edilir. Günün sıkıntılarına karşı tevekkül ve huzur için çekilir.",
            7
        ),
        Zikir(
            "yunus", "Yûnus duası", "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
            "Senden başka ilah yoktur. Seni eksikliklerden tenzih ederim. Gerçekten ben kendine yazık edenlerden oldum.",
            "Enbiyâ 87. ayette Hz. Yûnus'un (a.s.) duasıdır. Sıkıntıdan kurtuluş ve hata sonrası pişmanlık duası olarak okunur.",
            33
        ),
        Zikir(
            "yahayyu", "Yâ Hayyü yâ Kayyûm", "يَا حَيُّ يَا قَيُّومُ",
            "Ey ezelî ve ebedî diri olan, her şeyi ayakta tutan Allah'ım!",
            "Allah'ın Hayy ve Kayyûm isimleriyle yapılan yakarıştır. Dertte, hacet ve yardım isterken, özellikle gece ibadetlerinde çekilir.",
            100
        )
    )
}

/**
 * Zikirmatik kayıtları ("dhikr_prefs"): seçili zikir, her zikrin sayacı ve hedefi, kullanıcının
 * kendi eklediği zikirler. Hepsi yedeğe girer (BackupManager).
 */
object DhikrStore {
    const val PREFS = "dhikr_prefs"
    const val KEY_CUSTOM = "custom_list"
    const val KEY_COUNTS = "counts"
    const val KEY_TARGETS = "targets"
    const val KEY_SELECTED = "selected_id"

    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun customList(c: Context): List<Zikir> {
        val raw = prefs(c).getString(KEY_CUSTOM, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Zikir(
                    id = o.getString("id"), title = o.optString("title"), arabic = o.optString("text"),
                    meaning = o.optString("meaning"), purpose = o.optString("note"),
                    defaultTarget = o.optInt("target", 0), custom = true
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun saveCustom(c: Context, list: List<Zikir>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("id", it.id).put("title", it.title).put("text", it.arabic)
                .put("meaning", it.meaning).put("note", it.purpose).put("target", it.defaultTarget))
        }
        prefs(c).edit().putString(KEY_CUSTOM, arr.toString()).apply()
    }

    /** Yeni zikir ekler ya da aynı id'li olanı günceller. */
    fun upsertCustom(c: Context, z: Zikir) {
        val list = customList(c).toMutableList()
        val i = list.indexOfFirst { it.id == z.id }
        if (i >= 0) list[i] = z else list.add(z)
        saveCustom(c, list)
    }

    fun deleteCustom(c: Context, id: String) {
        saveCustom(c, customList(c).filter { it.id != id })
        setCount(c, id, 0)
    }

    fun newCustomId() = "custom_${System.currentTimeMillis()}"

    fun all(c: Context): List<Zikir> = DhikrCatalog.builtIn + customList(c)

    fun selectedId(c: Context): String = prefs(c).getString(KEY_SELECTED, DhikrCatalog.builtIn.first().id) ?: DhikrCatalog.builtIn.first().id
    fun setSelectedId(c: Context, id: String) { prefs(c).edit().putString(KEY_SELECTED, id).apply() }

    private fun readMap(c: Context, key: String): JSONObject =
        try { JSONObject(prefs(c).getString(key, "{}") ?: "{}") } catch (_: Exception) { JSONObject() }

    fun count(c: Context, id: String): Int = readMap(c, KEY_COUNTS).optInt(id, 0)

    fun setCount(c: Context, id: String, value: Int) {
        val m = readMap(c, KEY_COUNTS)
        if (value <= 0) m.remove(id) else m.put(id, value)
        prefs(c).edit().putString(KEY_COUNTS, m.toString()).apply()
    }

    fun increment(c: Context, id: String): Int {
        val n = count(c, id) + 1
        setCount(c, id, n)
        return n
    }

    fun clearAllCounts(c: Context) { prefs(c).edit().putString(KEY_COUNTS, "{}").apply() }

    /** Hedef: kullanıcı değiştirmediyse zikrin varsayılanı; 0 = sınırsız. */
    fun target(c: Context, z: Zikir): Int {
        val m = readMap(c, KEY_TARGETS)
        return if (m.has(z.id)) m.optInt(z.id, z.defaultTarget) else z.defaultTarget
    }

    fun setTarget(c: Context, id: String, value: Int) {
        val m = readMap(c, KEY_TARGETS)
        m.put(id, value)
        prefs(c).edit().putString(KEY_TARGETS, m.toString()).apply()
    }
}
