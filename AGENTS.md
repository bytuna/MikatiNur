# AGENTS.md — Proje Rutinleri ve Kurallar

> Bu dosya ajanlar (OpenCode, Aider, Cline vb.) için kalıcı talimattır.
> Her görevden önce bu kurallara uy. Çelişki olursa kullanıcıya sor.

## 1. Token Disiplini (EN YÜKSEK ÖNCELİK)

- **Önce planla, sonra yaz.** Mimari/yapı kararı gerekirse plan (salt-okunur) modunda al; onay olmadan kod yazma.
- **Dosyaları parça parça oku.** Grep/glob ile hedefi bul, dosyanın tamamını gereksiz yere okuma.
- **Aider kullanıcısıysan:** sadece değişecek dosyaları `/add` et; klasör eklemek yasak.
- **Bağlam şişirme:** AGENTS.md dahil talimat dosyaları 100 satırı geçmesin. Gereksiz MCP sunucusu ekleme (her biri adım başına +5-7k token).
- **Görev sınırı:** Her iş "en fazla N adım" hedefiyle verilir. Sınıra yaklaşınca dur ve durumu raporla.
- Ölü kod, yorum satırına gömülmüş eski kod, kullanılmayan import bırakma — temizlik de görevin parçası.

## 2. Görev Şablonu (kullanıcı her işi bu formatta verir, ajan eksikse sorar)

```
GÖREV: <tek cümlelik amaç>
DOSYALAR: <etkilenecek dosya/klasör listesi>
KABUL KRİTERLERİ: <bitti sayılması için 2-5 ölçüt, örn. "testler geçiyor">
SINIR: <maksimum adım / maksimum token>
```

## 3. Standart Klasör Yapısı

Her yeni proje bu iskeletle başlar. Tek dosyalık scriptler hariç.

```
proje/
├── src/            # tüm kaynak kod
│   ├── <modül1>/   # özellik bazlı klasörler (kullanıcı bazlı değil tip bazlı)
│   └── <modül2>/
├── tests/          # testler src yapısını aynalar
├── docs/           # README dışı tüm dokümantasyon
├── scripts/        # yardımcı komutlar (tek dosyalık araçlar)
├── .env.example    # .env asla commit'lenmez
├── AGENTS.md       # bu dosyanın proje-özel kısa hali
└── README.md       # kurulum + kullanım + proje yapısı
```

Kurallar:
- `src` içinde tip bazlı ayrım (`utils/`, `helpers/`) yerine **özellik bazlı** ayrım (`auth/`, `billing/`).
- Bir dosya 300 satırı geçiyorsa böl; bir fonksiyon tek iş yapsın.
- Kullanılmayan dosya/klasör bırakma; sil.
- Yapılandırma sabitleri (config) kod içine gömülmez — tek bir config/modül veya .env'e gider.

## 4. Kod Stili

- Yeni bağımlılık eklemeden önce kullanıcıya sor ve gerekçesini yaz.
- Hata mesajları insan-okur olsun; çıplak `catch` yasak.
- Fonksiyon/isimlendirme: amaç anlatan isimler; `data`, `temp`, `foo` yasak.
- Her özellikle birlikte en az bir test ekle (test yazılabilirlik tasarım kalitesinin ölçüsüdür).
- Commit mesajı: `<alan>: <fiil + nesne>` örn. `auth: add JWT refresh guard`.

## 5. Bitiş Kontrol Listesi (ajan her görev sonunda işaretler)

- [ ] Kabul kriterleri sağlandı
- [ ] Lint/test geçiyor
- [ ] Ölü kod ve debug çıktısı temizlendi
- [ ] Özet: ne yapıldı, kaç adım, yaklaşık token/maliyet

## 6. Ajan Görev Dağılımı (kim neyi yapar)

| İş tipi | Ajan | Mod/Not |
|---|---|---|
| Mimari karar, zor debug | OpenCode (pahalı model) | plan modunda öneri, onaydan sonra build |
| Küçük düzeltme, rutin edit, test fix | OpenCode / Aider (ucuz model) | Aider'da sadece hedef dosyayı /add et |
| Değerli/risksiz deneme isteyen iş | Cline | auto-approve kapalı, adım adım onay |
| Uçtan uca otonom büyük iş | OpenHands | Docker sandbox'ta, gözetimsiz |
