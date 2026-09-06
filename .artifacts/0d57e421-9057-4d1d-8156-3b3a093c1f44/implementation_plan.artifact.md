# Namaz Tesbihatı İçerik Güncellemesi ve Formatlama

Bu çalışma, `sorularlarisale.com` sitesindeki namaz tesbihatı metinlerini uygulamaya entegre etmeyi ve mevcut renklendirme mantığını yeni metin yapısına uygun hale getirmeyi amaçlar.

## User Review Required

*   **İçerik Kaynağı:** Metinler `sorularlarisale.com` sitesinden alınacaktır.
*   **Renklendirme:** Sitedeki renk şeması (zikirler, özel ifadeler) uygulama içerisinde `AnnotatedString` ile korunmaya çalışılacaktır. Renkleri eşleştirmek için manuel bir mapping gerekebilir.
*   **Mevcut İçerik:** Assets klasöründeki eski `.txt` dosyaları silinecektir.

## Open Questions

*   Sitedeki renklendirmeler CSS sınıfları veya stil etiketleri üzerinden geliyor olabilir. Bunları ayıklarken metnin okunabilirliğini bozmadan `AnnotatedString` formatına nasıl dönüştüreceğimiz kritik olacak.

## Proposed Changes

### İçerik Güncelleme
*   [NEW] `app/src/main/assets/{vakıt}_{dil}.txt` dosyaları (10 adet: 5 vakit * 2 dil)
*   [DELETE] Mevcut `app/src/main/assets/*.txt` dosyaları.

### Kod Güncelleme
#### [MODIFY] [TesbihatScreen.kt](file:///C:/Users/Emir Mirza/AndroidStudioProjects/MikatiNur/app/src/main/java/com/example/mkat_nur/ui/tesbihat/TesbihatScreen.kt)
*   `appendZikirStyled` fonksiyonu, yeni metin içeriğindeki özel işaretleri (örneğin `<span>` veya benzeri etiketler) yakalayacak şekilde veya içeriğin metin yapısına göre güncellenecektir.

## Verification Plan

### Automated Tests
*   `TesbihatScreen` composable fonksiyonunu `render_compose_preview` ile test ederek içeriğin düzgün göründüğünü doğrulayacağım.

### Manual Verification
*   Uygulamayı emülatöre deploy edip, tüm vakitlerin Arapça ve Türkçe sekmelerini tek tek kontrol edeceğim.
