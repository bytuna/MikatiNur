package com.example.mkat_nur.util

object QuranTransliterationHelper {
    private val fatihaTransliterations = mapOf(
        "1:1" to "Bismi(A)llâhi-rrahmâni-rrahîm(i)",
        "1:2" to "Elhamdu li(A)llâhi rabbi-l'âlemîn(e)",
        "1:3" to "Errahmâni-rrahîm(i)",
        "1:4" to "Mâliki yevmi-ddîn(i)",
        "1:5" to "İyyâke na'budu ve iyyâke neste'în(u)",
        "1:6" to "İhdinâ-ssirâta-lmustekîm(e)",
        "1:7" to "Sirâtallezîne en'amte 'aleyhim gayri-lmagdûbi 'aleyhim vele-ddâllîn(e)"
    )

    private val bakaraSampleTransliterations = mapOf(
        "2:1" to "Elif-lâm-mîm",
        "2:2" to "Zâlike-lkitâbu lâ raybe fîh(i) huden lilmuttekîn(e)",
        "2:3" to "Ellezîne yu'minûne bilgaybi ve yukîmûne-ssalâte ve mimmâ rezaknâhum yunfikûn(e)",
        "2:4" to "Vellezîne yu'minûne bimâ unzile ileyke vemâ unzile min kablike ve bil-âḣirati hum yûkinûn(e)",
        "2:5" to "Ulâ-ike 'alâ huden min rabbihim ve ulâ-ike humu-lmuflihûn(e)",
        "2:255" to "Allâhu lâ ilâhe illâ huve-lhayyu-lkayyûm(u) lâ te'ḣużuhu sinetun velâ nevmu-llahu mâ fî-ssemâvâti vemâ fî-l-ard(i)..."
    )

    fun getTransliteration(verseKey: String?, arabicText: String? = null): String? {
        if (verseKey == null && arabicText == null) return null
        if (verseKey != null && fatihaTransliterations.containsKey(verseKey)) return fatihaTransliterations[verseKey]
        if (verseKey != null && bakaraSampleTransliterations.containsKey(verseKey)) return bakaraSampleTransliterations[verseKey]
        
        if (!arabicText.isNullOrBlank()) {
            return convertArabicToTurkishPhonetic(arabicText)
        }
        return null
    }

    private fun convertArabicToTurkishPhonetic(arabicText: String): String {
        val sb = StringBuilder()
        var i = 0
        val len = arabicText.length

        while (i < len) {
            val ch = arabicText[i]
            when (ch) {
                'ا', 'أ', 'إ', 'آ', 'ٱ' -> {
                    if (sb.isEmpty() || sb.last() == ' ' || sb.last() == '-') {
                        sb.append("e")
                    } else {
                        sb.append("â")
                    }
                }
                'ب' -> sb.append("b")
                'ت' -> sb.append("t")
                'ث' -> sb.append("s")
                'ج' -> sb.append("c")
                'ح', 'خ' -> sb.append("h")
                'د' -> sb.append("d")
                'ذ', 'ز', 'ظ' -> sb.append("z")
                'ر' -> sb.append("r")
                'س', 'ص' -> sb.append("s")
                'ش' -> sb.append("ş")
                'ض' -> sb.append("d")
                'ط' -> sb.append("t")
                'ع' -> sb.append("'")
                'غ' -> sb.append("g")
                'ف' -> sb.append("f")
                'ق', 'ك' -> sb.append("k")
                'ل' -> sb.append("l")
                'م' -> sb.append("m")
                'ن' -> sb.append("n")
                'ه', 'ة' -> sb.append("h")
                'و' -> {
                    if (sb.isNotEmpty() && sb.last() == 'u') {
                        sb.deleteCharAt(sb.length - 1)
                        sb.append("û")
                    } else {
                        sb.append("v")
                    }
                }
                'ي', 'ى' -> {
                    if (sb.isNotEmpty() && sb.last() == 'i') {
                        sb.deleteCharAt(sb.length - 1)
                        sb.append("î")
                    } else {
                        sb.append("y")
                    }
                }
                'َ' -> { // Fatha
                    if (sb.isNotEmpty() && sb.last() != 'â' && sb.last() != 'e' && sb.last() != 'a') {
                        sb.append("e")
                    }
                }
                'ُ' -> { // Damma
                    if (sb.isNotEmpty() && sb.last() != 'û' && sb.last() != 'u') {
                        sb.append("u")
                    }
                }
                'ِ' -> { // Kasra
                    if (sb.isNotEmpty() && sb.last() != 'î' && sb.last() != 'i') {
                        sb.append("i")
                    }
                }
                'ً' -> sb.append("en")
                'ٌ' -> sb.append("un")
                'ٍ' -> sb.append("in")
                'ٰ' -> { // Dagger alif
                    if (sb.isNotEmpty() && (sb.last() == 'e' || sb.last() == 'a')) {
                        sb.deleteCharAt(sb.length - 1)
                    }
                    sb.append("â")
                }
                ' ' -> {
                    if (sb.isNotEmpty() && sb.last() != ' ') {
                        sb.append(" ")
                    }
                }
                else -> {
                    if (ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9') {
                        sb.append(ch)
                    }
                }
            }
            i++
        }

        val result = sb.toString().trim().replace(Regex("\\s+"), " ")
        return if (result.isEmpty()) "Bismillâhi-rrahmâni-rrahîm" else result.replaceFirstChar { it.uppercase() }
    }
}
