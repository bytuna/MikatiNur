package com.example.mkat_nur.ui.quran1

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Hatim Duası Diyaloğu (Arapça & Türkçe Anlamı)
@Composable
fun HatimDuasiDialog(
    onDismiss: () -> Unit
) {
    var selectedLanguage by remember { mutableIntStateOf(0) } // 0: Arapça, 1: Türkçe

    val arabicDua = """
        الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ، وَالْعَاقِبَةُ لِلْمُتَّقِينَ ، وَلاَ عُدْوَانَ إِلاَّ عَلَى الظَّالِمِينَ .
        وَالصَّلاَةُ وَالسَّلاَمُ عَلَى رَسُولِنَا مُحَمَّدٍ وَآلِهِ وَصَحْبِهِ أَجْمَعِينَ .
        
        اللَّهُمَّ رَبَّنَا يَا رَبَّنَا تَقَبَّلْ مِنَّا إِنَّكَ أَنْتَ السَّمِيعُ الْعَلِيمُ ، وَتُبْ عَلَيْنَا يَا مَوْلاَنَا إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ .
        وَاهْدِنَا وَوَفِّقْنَا إِلَى الْحَقِّ وَإِلَى طَرِيقٍ مُسْتَقِيمٍ بِبَرَكَةِ الْقُرْآنِ الْعَظِيمِ .
        
        اللَّهُمَّ ارْحَمْنَا بِالْقُرْآنِ وَاجْعَلْهُ لَنَا إِمَاماً وَنُوراً وَهُدًى وَرَحْمَةً .
        اللَّهُمَّ ذَكِّرْنَا مِنْهُ مَا نَسِينَا وَعَلِّمْنَا مِنْهُ مَا جَهِلْنَا وَارْزُقْنَا تِلاَوَتَهُ آنَاءَ اللَّيْلِ وَأَطْرَافَ النَّهَارِ وَاجْعَلْهُ لَنَا حُجَّةً يَا رَبَّ الْعَالَمِينَ .
    """.trimIndent()

    val turkishDua = """
        Hamd alemlerin Rabbi olan Allah'a mahsustur. Güzel akıbet takva sahiplerinindir. Salat ve selam Peygamberimiz Hz. Muhammed (s.a.v.)'e, onun âline ve tüm ashabına olsun.
        
        Ey Rabbimiz! Bizden kabul buyur; şüphesiz sen her şeyi işiten ve bilensin. Ey Mevlamız! Tövbelerimizi kabul eyle; şüphesiz sen tövbeleri çokça kabul eden ve merhamet edensin.
        
        Yüce Kur'an'ın bereketiyle bizi hakka ve dosdoğru yola ilet. Allah'ım! Kur'an ile bize merhamet eyle; onu bize önder, nur, hidayet ve rahmet kıl.
        
        Allah'ım! Unuttuklarımızı Kur'an ile bize hatırlat, belmediklerimizi bellet. Gece saatlerinde ve gündüz vakitlerinde onu okumayı nasip eyle. Ey Alemlerin Rabbi! Kur'an'ı lehimize hüccet kıl. Âmin!
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("❖ HATİM DUASI ❖", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF14584A))
                Spacer(Modifier.height(8.dp))
                TabRow(selectedTabIndex = selectedLanguage, containerColor = Color.Transparent, contentColor = Color(0xFF14584A)) {
                    Tab(selected = selectedLanguage == 0, onClick = { selectedLanguage = 0 }) {
                        Text("Arapça", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Tab(selected = selectedLanguage == 1, onClick = { selectedLanguage = 1 }) {
                        Text("Türkçe Anlamı", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (selectedLanguage == 0) {
                    Text(
                        text = arabicDua,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C1810),
                        textAlign = TextAlign.Right,
                        lineHeight = 30.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = turkishDua,
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        lineHeight = 22.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14584A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ÂMİN (KAPAT)", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        containerColor = Color(0xFFF8F1DD),
        shape = RoundedCornerShape(16.dp)
    )
}

