package com.example.mkat_nur

import com.example.mkat_nur.util.RisaleImporter
import org.junit.Test

class RisaleImportTest {

    @Test
    fun generateJson() {
        val bookId = "sozler"
        
        // BURAYA PDF'DEN KOPYALADIĞINIZ METNİ YAPIŞTIRIN
        // Sayfa ayırmak için [SAYFA 1], [SAYFA 2] gibi etiketler kullanabilirsiniz.
        val rawText = """
            [SAYFA 1]
            بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّح۪يمِ
            
            BİRİNCİ SÖZ
            
            Bismillah her hayrın başıdır. Biz dahi başta ona başlarız.
            
            [SAYFA 2]
            İkinci sayfa içeriği buraya gelecek...
            بِسْمِ اللّٰهِ ne büyük bir kuvvettir.
        """.trimIndent()

        // JSON'un kaydedileceği yer (Projenizdeki assets klasörü)
        val outputPath = "C:/Users/Emir Mirza/AndroidStudioProjects/MikatiNur/app/src/main/assets/risale/sozler.json"
        
        RisaleImporter.convertRawTextToJson(bookId, rawText, outputPath)
    }
}
