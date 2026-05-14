Mîkat-ı Nur - Temel Uygulama Özellikleri
1. Akıllı Namaz Vakitleri Takibi:
•Dünya genelinde konuma duyarlı veya seçilen şehir bazlı anlık namaz vakitleri.
•Vakitlerin her gün otomatik güncellenmesi sayesinde her zaman güncel bilgi.

2. Gelişmiş Bildirim ve Ezan Sistemi:
•Her namaz vakti için özel tasarlanmış, estetik bildirim kartları.
•Vakit Geldi Alarmları: Uygulama kapalı olsa bile çalışan güvenilir alarm altyapısı.
•Bildirim üzerinden vakit bilgilerini hızlıca görebilme imkanı.

3. Kıble Pusulası:
•Cihazın sensörlerini kullanarak yüksek hassasiyetle Kıble yönü tayini.
•Dünyanın neresinde olursanız olun ibadet yönünüzü bulmanızı sağlayan modern arayüz.

4. Yapay Zeka Destekli İçerik (AI):
•Google Gemini (Firebase AI) entegrasyonu ile uygulama içinde akıllı asistan desteği.
•Dini konularda soru sorabilme veya içerik oluşturma (Baking/Content Generation) yeteneği.

5. Cuma Mesajları ve Manevi Paylaşımlar:
•Özel günler ve Cuma günleri için hazır, paylaşılabilir görsel ve metin içerikleri.
•Kullanıcıların sevdikleriyle kolayca paylaşabileceği manevi mesajlar arşivi.

6. Modern ve Sade Tasarım:
•Material 3 standartlarında, göz yormayan ve modern bir kullanıcı arayüzü.
•Hızlı geçişler sağlayan alt navigasyon menüsü ve kullanıcı dostu ekran düzeni.

7. Performans ve Güvenlik:
•Offline Çalışma Desteği: İndirilen vakitlerin internet olmasa dahi görüntülenebilmesi.
•Hafif yapı: Cihaz kaynaklarını tüketmeyen, pil dostu arka plan servisleri.


Mîkat-ı Nur - Proje Güncelleme Özeti
1. Namaz Vakitleri ve API Entegrasyonu:
•Retrofit & Gson: Namaz vakitlerini dinamik olarak çekmek için PrayerApiService ve RetrofitClient yapılandırıldı.
•Arka Plan Servisleri: Namaz vakitlerini takip eden ve zamanı geldiğinde tetiklenen PrayerNotificationService ve PrayerAlarmReceiver eklendi.

2. Bildirim Sistemi:
•Özel Bildirim Tasarımı: Kullanıcıya namaz vakitlerini estetik bir şekilde sunan notification_prayer.xml layout dosyası hazırlandı.
•NotificationHelper: Bildirim kanallarını ve gönderim süreçlerini yöneten yardımcı sınıf (NotificationHelper.kt) oluşturuldu.

3. Kıble Pusulası:
•Kullanıcıların kıble yönünü bulabilmesi için QiblaScreen geliştirildi.

4. Yapay Zeka (AI) Entegrasyonu:
•Firebase AI (Gemini): Uygulamaya akıllı özellikler kazandırmak için Google'ın üretken yapay zeka desteği (firebase-ai) eklendi ve BakingScreen üzerinden AI içerik oluşturma altyapısı kuruldu.

5. Uygulama Yapılandırması ve Mimari:
•AppConfig: Uygulamanın versiyon geçmişi, geliştirici bilgileri ve güncelleme zamanlarını yöneten merkezi bir yapı oluşturuldu.
•Modern UI: Jetpack Compose kullanılarak modern, performanslı ve kullanıcı dostu arayüzler geliştirildi.
•Navigasyon: navigation-compose ile ekranlar arası geçiş yönetimi sağlandı.

6. Bağımlılık Yönetimi:
•build.gradle.kts dosyaları güncellenerek WorkManager, Coil (resim yükleme), Retrofit ve Compose kütüphanelerinin en güncel versiyonları projeye dahil edildi.
Commit Mesajı:
feat: add prayer times API, notification service, qibla screen and Gemini AI integration

Teknik İyileştirmeler ve Mimari Detaylar
1. Arka Plan Görev Yönetimi (WorkManager):
•Namaz vakitlerinin her gün düzenli olarak güncellenmesi ve sistemin ayakta kalması için WorkManager altyapısı kuruldu. Bu sayede uygulama kapalı olsa bile alarm ve bildirimler stabil çalışacak.

8. Görsel ve Arayüz Optimizasyonu:
•Coil Compose: Resimlerin asenkron ve performanslı yüklenmesi için coil-compose entegrasyonu yapıldı.
•Material 3 Tasarımı: Uygulamanın tamamında Google'ın en yeni tasarım dili olan Material 3 kullanılarak dinamik renk ve modern bileşenler (TopAppBar, NavigationRail vb.) uygulandı.
•Custom Layouts: Sadece Compose değil, bildirimler gibi sistem seviyesindeki alanlar için XML tabanlı (notification_prayer.xml) özel tasarımlar yapıldı.

9. Hata Yönetimi ve Loglama:
•API isteklerinde ve servislerin çalışması sırasında oluşabilecek hatalar için (örneğin internet yokken vakit çekme denemesi) try-catch blokları ve güvenli hata yakalama mekanizmaları eklendi.

10. Veri Modelleme:
•Namaz vakitleri ve sürüm geçmişi için kullanılan veri sınıfları (Data Class), JSON serileştirme (Gson) ile tam uyumlu hale getirildi.
Commit Mesajı Seçenekleri (Daha detaylı):
Seçenek A (Modüler):
refactor: optimize notification logic and update AppConfig versioning.  feat: implement Qibla sensor integration and enhance AI screen UI.
Seçenek B (Teknik):
feat: integrate Retrofit for prayer times, implement AlarmManager for daily notifications, and add Firebase AI support. UI/UX improvements with Material 3.
