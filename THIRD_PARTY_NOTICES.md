# Üçüncü Taraf Lisanslar ve Kaynaklar

Mîkat-ı Nur'un **kaynak kodu** MIT Lisansı ile paylaşılmıştır (bkz. `LICENSE`).
MIT lisansı yalnızca bu projeye ait kodu kapsar. Aşağıdaki içerik, yazı tipi ve bileşenler kendi sahiplerinin lisanslarına tabidir.
Uygulama içinde aynı liste **Ayarlar → Gizlilik ve Yasal → Lisanslar ve Kaynaklar** bölümünde gösterilir.

## Kur'an-ı Kerim
| Kaynak | Kullanım | Lisans |
|---|---|---|
| Tanzil Quran Text (Uthmani, v1.1), © 2007-2026 Tanzil Project — https://tanzil.net | Arapça Kur'an metni (`assets/quran-uthmani.txt`) | Creative Commons Attribution 3.0. Metin değiştirilemez; kaynak belirtilmeli ve tanzil.net'e bağlantı verilmeli; telif bloğu dosyada korunmalıdır. |
| AlQuran Cloud API (Islamic Network) — https://alquran.cloud | Kur'an-ı Kerim: sure, tecvid, okunuş, Diyanet meali, Alafasy sesleri | Servisin hizmet şartları |
| Quran.com API (Quran Foundation) — https://quran.com | Meal/çeviriler (Diyanet İşleri Başkanlığı Türkçe meali dahil) | Servisin hizmet şartları |
| EveryAyah.com — https://everyayah.com | Kıraat sesleri | Servisin hizmet şartları |
| android.quran.com ve TheQuran/quran-images | Mushaf sayfa görselleri | Kaynak sitelerin kullanım şartları |

## Namaz Vakitleri
- T.C. Diyanet İşleri Başkanlığı vakit verileri (`ezanvakti.emushaf.net` aracılığıyla).
- Mekke/Medine: Ümmü'l-Kurâ hesaplama yöntemi.
- Ezan kayıtları: https://namazvakitleri.net.tr/ezan-sesleri

## Yazı Tipleri
| Yazı tipi | Lisans |
|---|---|
| Scheherazade New, © SIL Global | SIL Open Font License 1.1 |
| KFGQPC Uthman Taha Naskh, © King Fahd Glorious Quran Printing Complex | KFGQPC Son Kullanıcı Lisansı: ücretsiz kullanım, kopyalama ve dağıtım; satış, değiştirme ve tersine mühendislik yasak |
| Me Quran, © 2005 Meor Ridzuan | Yazı tipi dosyasında: "Free for non commercial use" (ticari olmayan kullanım) |

## Yazılım Bileşenleri
AndroidX (Compose, Navigation, WorkManager, Lifecycle, Activity, Core, WebKit, Media3), Kotlin ve Coroutines, Retrofit, OkHttp, Gson, Coil, Material Icons: **Apache License 2.0** (https://www.apache.org/licenses/LICENSE-2.0).
Firebase (Authentication, Firestore): Apache License 2.0 ve Firebase Hizmet Şartları. Google Play Hizmetleri: Google Play Hizmetleri Şartları.

---
### Yayın öncesi doğrulanması gerekenler
1. **Me Quran** yazı tipi ticari olmayan kullanım içindir. Uygulama ücretsiz ve reklamsız ise sorun olmayabilir; para kazanma (reklam, abonelik, ücretli sürüm) planı varsa yazı tipini kaldırın veya yazardan izin alın.
2. **Ezan kayıtları** (`res/raw/*_ezani.mp3`) için kaynak sitenin yeniden dağıtım izni doğrulanmalıdır.
3. **Tecvid verisi** (`assets/tajweed_data.json`) ve **Risale-i Nur** metinlerinin yayın hakları/kaynakları doğrulanmalıdır.
4. Quran.com ve EveryAyah servislerinin güncel kullanım şartları, uygulamanın kullanım biçimine uygun olmalıdır.
