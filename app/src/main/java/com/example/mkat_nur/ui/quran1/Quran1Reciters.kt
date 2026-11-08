package com.example.mkat_nur.ui.quran1

/**
 * Okuyucu (hafız) listesi. Sesler everyayah.com üzerinden ayet ayet çalınır:
 * https://everyayah.com/data/{folder}/{SSS}{AAA}.mp3
 * Klasör adları QuranViewModel'deki (eski modül) listeyle aynı biçimdedir.
 */
data class Quran1Reciter(
    val id: String,
    val name: String,
    /** Alt çubukta sığması için kısa ad. */
    val shortName: String,
    val folder: String,
    val group: String,
    /** true ise (yalnızca Alafasy) sure okumasında AlQuran Cloud'un sesi önceliklidir. */
    val preferApiAudio: Boolean = false
)

object Quran1Reciters {
    const val GROUP_KAABA = "Kâbe (Mescid-i Haram) İmamları"
    const val GROUP_MEDINE = "Medine (Mescid-i Nebevî) İmamları"
    const val GROUP_OTHER = "Diğer Meşhur Hafızlar"

    const val DEFAULT_ID = "alafasy"

    val all = listOf(
        Quran1Reciter("sudais", "Abdurrahman es-Sudeys", "es-Sudeys", "Abdurrahmaan_As-Sudais_192kbps", GROUP_KAABA),
        Quran1Reciter("shuraym", "Suûd eş-Şureym", "eş-Şureym", "Saood_ash-Shuraym_128kbps", GROUP_KAABA),
        Quran1Reciter("muaiqly", "Mâhir el-Muaykılî", "el-Muaykılî", "Maher_AlMuaiqly_64kbps", GROUP_KAABA),
        Quran1Reciter("dussary", "Yâsir ed-Devserî", "ed-Devserî", "Yasser_Ad-Dussary_128kbps", GROUP_KAABA),
        Quran1Reciter("juhany", "Abdullah Avvâd el-Cühenî", "el-Cühenî", "Abdullaah_3awwaad_Al-Juhaynee_128kbps", GROUP_KAABA),
        Quran1Reciter("budair", "Salâh el-Budeyr", "el-Budeyr", "Salah_Al_Budair_128kbps", GROUP_MEDINE),
        Quran1Reciter("hudhaify", "Ali el-Huzeyfî", "el-Huzeyfî", "Hudhaify_128kbps", GROUP_MEDINE),
        Quran1Reciter("alafasy", "Mişarî Raşid Alafasy", "Alafasy", "Alafasy_128kbps", GROUP_OTHER, preferApiAudio = true),
        Quran1Reciter("husary", "Mahmûd Halîl el-Husarî", "el-Husarî", "Husary_128kbps", GROUP_OTHER),
        Quran1Reciter("minshawi", "Muhammed Sıddîk el-Minşâvî", "el-Minşâvî", "Minshawy_Murattal_128kbps", GROUP_OTHER),
        Quran1Reciter("abdulbasit", "Abdulbâsıt Abdüssamed", "Abdulbâsıt", "Abdul_Basit_Murattal_64kbps", GROUP_OTHER),
        Quran1Reciter("ghamdi", "Saad el-Gâmidî", "el-Gâmidî", "Ghamadi_40kbps", GROUP_OTHER),
        Quran1Reciter("shaatree", "Ebû Bekr eş-Şâtırî", "eş-Şâtırî", "Abu_Bakr_Ash-Shaatree_128kbps", GROUP_OTHER)
    )

    val groups = listOf(GROUP_KAABA, GROUP_MEDINE, GROUP_OTHER)

    fun byId(id: String?): Quran1Reciter = all.firstOrNull { it.id == id } ?: all.first { it.id == DEFAULT_ID }

    fun urlFor(reciter: Quran1Reciter, surahNumber: Int, ayahNumber: Int): String {
        val s = surahNumber.toString().padStart(3, '0')
        val a = ayahNumber.toString().padStart(3, '0')
        return "https://everyayah.com/data/${reciter.folder}/$s$a.mp3"
    }

    /** Sure okumasında Alafasy için API'nin kendi ses adresi, diğer durumlarda everyayah adresi. */
    fun audioUrl(reciter: Quran1Reciter, ayah: Quran1Ayah): String? {
        if (reciter.preferApiAudio && !ayah.audioUrl.isNullOrBlank()) return ayah.audioUrl
        if (ayah.surahNumber <= 0) return ayah.audioUrl
        return urlFor(reciter, ayah.surahNumber, ayah.numberInSurah)
    }
}

/** Okuma hızı: her dokunuşta bir üst hıza geçer, x2'den sonra x1'e döner. */
object Quran1Speeds {
    val options = listOf(1f, 1.25f, 1.5f, 1.75f, 2f)

    fun next(current: Float): Float {
        val i = options.indexOfFirst { kotlin.math.abs(it - current) < 0.001f }
        return options[(i + 1) % options.size]
    }

    fun label(speed: Float): String = when {
        kotlin.math.abs(speed - 1f) < 0.001f -> "x1"
        kotlin.math.abs(speed - 1.25f) < 0.001f -> "x1,25"
        kotlin.math.abs(speed - 1.5f) < 0.001f -> "x1,5"
        kotlin.math.abs(speed - 1.75f) < 0.001f -> "x1,75"
        kotlin.math.abs(speed - 2f) < 0.001f -> "x2"
        else -> "x$speed"
    }
}
