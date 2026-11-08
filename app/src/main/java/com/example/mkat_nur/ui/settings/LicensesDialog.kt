package com.example.mkat_nur.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mkat_nur.ui.theme.LocalAppThemeColors
import com.example.mkat_nur.util.AppConfig

private data class NoticeItem(
    val title: String,
    val usage: String,
    val license: String,
    val url: String? = null
)

private data class NoticeGroup(val title: String, val items: List<NoticeItem>)

private val NOTICE_GROUPS = listOf(
    NoticeGroup(
        "Kur'an-ı Kerim",
        listOf(
            NoticeItem(
                "Tanzil Kur'an Metni (Uthmani, v1.1)",
                "Kur'an-ı Kerim Arapça metni. © 2007-2026 Tanzil Project. Metin değiştirilmeden kullanılmaktadır; güncellemeleri takip etmek için tanzil.net adresini ziyaret edebilirsiniz.",
                "Creative Commons Attribution 3.0",
                "https://tanzil.net"
            ),
            NoticeItem(
                "Quran.com API (Quran Foundation)",
                "Türkçe meal ve diğer çeviriler, sure/cüz bilgileri. Diyanet İşleri Başkanlığı Türkçe meali bu servis üzerinden alınır.",
                "Hizmet şartları: quran.com",
                "https://quran.com"
            ),
            NoticeItem(
                "EveryAyah.com",
                "Ayet ayet kıraat sesleri.",
                "Hizmet şartları: everyayah.com",
                "https://everyayah.com"
            ),
            NoticeItem(
                "Mushaf sayfa görselleri",
                "Mushaf sayfaları Quran for Android (android.quran.com) ve TheQuran/quran-images kaynaklarından gösterilir.",
                "Kaynak sitelerin kullanım şartları",
                "https://android.quran.com"
            )
        )
    ),
    NoticeGroup(
        "Namaz Vakitleri",
        listOf(
            NoticeItem(
                "Diyanet İşleri Başkanlığı",
                "Türkiye ve dünya şehirleri için vakit verileri Diyanet İşleri Başkanlığı verilerine dayanır.",
                "T.C. Diyanet İşleri Başkanlığı",
                "https://vakithesaplama.diyanet.gov.tr"
            ),
            NoticeItem(
                "Ümmü'l-Kurâ Takvimi",
                "Mekke ve Medine vakitleri Ümmü'l-Kurâ (Mekke) yöntemiyle hesaplanır.",
                "Hesaplama yöntemi",
                null
            ),
            NoticeItem(
                "Ezan kayıtları",
                "Ezan sesleri namazvakitleri.net.tr sitesindeki ezan seslerinden alınmıştır.",
                "Kaynak sitenin kullanım şartları",
                "https://namazvakitleri.net.tr"
            )
        )
    ),
    NoticeGroup(
        "Yazı Tipleri",
        listOf(
            NoticeItem(
                "Scheherazade New",
                "Tecvidli Kur'an metni için. © SIL Global, 'Scheherazade' ve 'SIL' ayrılmış yazı tipi adlarıdır.",
                "SIL Open Font License 1.1",
                "https://openfontlicense.org"
            ),
            NoticeItem(
                "KFGQPC Uthman Taha Naskh",
                "© King Fahd Glorious Quran Printing Complex. Ücretsiz kullanım, kopyalama ve dağıtım izni vardır; satılamaz ve değiştirilemez.",
                "KFGQPC Son Kullanıcı Lisansı",
                "https://qurancomplex.gov.sa"
            ),
            NoticeItem(
                "Me Quran",
                "Typeface and data © 2005, Meor Ridzuan. Ticari olmayan kullanım için ücretsizdir.",
                "Ticari olmayan kullanım",
                null
            )
        )
    ),
    NoticeGroup(
        "Yapay Zekâ ve Görsel",
        listOf(
            NoticeItem(
                "Google Gemini (Firebase AI Logic)",
                "Paylaşım kartlarının hazırlanmasında kullanılır.",
                "Google / Firebase hizmet şartları",
                "https://firebase.google.com/terms"
            ),
            NoticeItem(
                "Pollinations.ai",
                "Paylaşım kartı arka plan görselleri bu servisle üretilir.",
                "Hizmet şartları: pollinations.ai",
                "https://pollinations.ai"
            )
        )
    ),
    NoticeGroup(
        "Yazılım Bileşenleri",
        listOf(
            NoticeItem(
                "AndroidX: Jetpack Compose, Navigation, WorkManager, Lifecycle, Activity, Core, WebKit",
                "Arayüz ve arka plan işleri.",
                "Apache License 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0"
            ),
            NoticeItem(
                "Kotlin ve Kotlin Coroutines (JetBrains)",
                "Programlama dili ve eşzamansız işlemler.",
                "Apache License 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0"
            ),
            NoticeItem(
                "Retrofit ve OkHttp (Square)",
                "Ağ iletişimi.",
                "Apache License 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0"
            ),
            NoticeItem(
                "Gson (Google)",
                "JSON işleme.",
                "Apache License 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0"
            ),
            NoticeItem(
                "Coil",
                "Görsel yükleme.",
                "Apache License 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0"
            ),
            NoticeItem(
                "Material Icons (Google)",
                "Arayüz simgeleri.",
                "Apache License 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0"
            ),
            NoticeItem(
                "Firebase (Authentication, Firestore)",
                "Hesap girişi ve kaza takibi senkronizasyonu.",
                "Apache License 2.0 ve Firebase Hizmet Şartları",
                "https://firebase.google.com/terms"
            ),
            NoticeItem(
                "Google Play Hizmetleri (Konum, Google ile Giriş)",
                "Konum bulma ve Google hesabıyla giriş.",
                "Google Play Hizmetleri Şartları",
                "https://developers.google.com/android/guides/terms"
            )
        )
    )
)

/** Açık kaynak lisansları ve içerik kaynakları listesi. */
@Composable
fun LicensesDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val themeColors = LocalAppThemeColors.current

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = themeColors.dialogSurface,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Lisanslar ve Kaynaklar",
                        color = themeColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Kapat", tint = themeColors.textPrimary) }
                }

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${AppConfig.PROJECT_NAME} uygulamasının kaynak kodu MIT Lisansı ile paylaşılmıştır. " +
                            "Aşağıdaki içerik, yazı tipi ve yazılım bileşenleri kendi sahiplerinin lisanslarına tabidir.",
                        color = themeColors.textSecondary, fontSize = 12.sp, lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(4.dp))

                    NOTICE_GROUPS.forEach { group ->
                        Text(
                            group.title,
                            color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        group.items.forEach { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(themeColors.background.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .then(
                                        if (item.url != null) Modifier.clickable {
                                            try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url))) } catch (_: Exception) { }
                                        } else Modifier
                                    )
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        item.title, color = themeColors.textPrimary,
                                        fontWeight = FontWeight.Bold, fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (item.url != null) {
                                        Icon(Icons.Default.OpenInNew, null, tint = themeColors.textSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(item.usage, color = themeColors.textSecondary, fontSize = 11.5.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 3.dp))
                                Text(item.license, color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Kapat", color = Color.White) }
            }
        }
    }
}
