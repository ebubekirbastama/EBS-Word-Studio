# EBS Word Studio

<p align="center">
  <img src="app/src/main/res/drawable/ebs_logo.png" alt="EBS Word Studio" width="340">
</p>

<p align="center"><strong>Android için çevrimdışı belge metni düzenleyicisi</strong></p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Build-Gradle%20Kotlin%20DSL-02303A?logo=gradle&logoColor=white" alt="Gradle">
  <img src="https://img.shields.io/badge/Mode-Offline-2E8B57" alt="Offline">
  <img src="https://img.shields.io/badge/License-Apache--2.0-blue" alt="Apache 2.0">
</p>

## 📌 Proje hakkında

**EBS Word Studio**, Java ile geliştirilen Android belge/metin düzenleyici prototipidir. Koyu temalı arayüzü; dosya açma, metin düzenleme, kelime ve karakter sayacı, yazı boyutunu değiştirme, panoya kopyalama ve farklı biçimlerde kaydetme özelliklerine odaklanır.

> **Not:** Bu uygulama tam özellikli bir Microsoft Word alternatifi değildir. DOCX/ODT biçimlendirmesini, tabloları, görselleri veya sayfa düzenini korumaz; temel olarak metin çıkarma ve düz metin düzenleme sağlar.

## 🧰 Teknoloji kartları

| Teknoloji | Kullanım |
|---|---|
| Java / Android SDK | Activity tabanlı uygulama ve arayüz |
| Android Storage Access Framework | Sistem dosya seçicisi ile belge açma/kaydetme |
| `DocumentCodec` | Belge okuma, metin çıkarma ve yazma |
| ZIP / XML Pull Parser | DOCX ve ODT içinden metin çıkarma |
| Android `Html.fromHtml` | HTML'i metne dönüştürme |
| Gradle Kotlin DSL | Proje ayarları (`settings.gradle.kts`) |

## ✨ Özellikler

- Koyu temalı, çevrimdışı çalışmaya yönelik arayüz.
- Sistem dosya seçicisiyle belge açma ve kaydetme.
- Metin düzenleme, kelime/karakter sayacı ve yazı boyutu kontrolü.
- Metni panoya kopyalama ve yeni belge oluşturma.
- Kaydedilmemiş değişiklikler için uyarı.
- DOCX arşivinden `word/document.xml`, ODT arşivinden `content.xml` içeriğini okumaya yönelik destek.
- TXT/MD, HTML ve RTF için metin odaklı okuma/yazma.

## 📁 Proje yapısı

```text
EBS-Word-Studio/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/ebs/wordstudio/
│   │   ├── MainActivity.java
│   │   └── DocumentCodec.java
│   └── res/
│       ├── drawable/
│       │   ├── ebs_app_icon.png
│       │   └── ebs_logo.png
│       └── mipmap-*/ic_launcher.png
├── settings.gradle.kts
├── LICENSE
└── README.md
```

## 🚀 Kurulum ve derleme

### Gereksinimler
- Android Studio
- Uyumlu JDK ve Android SDK
- Android Gradle Plugin ve Gradle yapılandırması

### Depodaki mevcut durum

Mevcut dosya ağacında `app/build.gradle.kts`, kök `build.gradle.kts` ve Gradle Wrapper dosyaları görünmüyor. Bu nedenle proje, depodan klonlandığı haliyle doğrudan derlenebilir olmayabilir. Önce eksik Android modülü/derleme yapılandırması eklenmelidir.

Ayrıca `MainActivity.java`, `com.ebs.forensic.detector.R` paketini içe aktarıyor; manifest Activity adı ise `com.ebs.wordstudio.MainActivity`. Android modülünün `namespace` ayarı ve kaynak sınıfı importu derleme öncesinde kontrol edilmelidir.

1. Depoyu klonlayın:
   ```bash
   git clone https://github.com/ebubekirbastama/EBS-Word-Studio.git
   cd EBS-Word-Studio
   ```
2. Android Studio ile açın.
3. Eksik Gradle/Android modülü yapılandırmasını ve namespace ayarını tamamlayın.
4. Gradle senkronizasyonu ve derleme hatalarını giderdikten sonra emülatör veya Android cihazda çalıştırın.

## ▶️ Kullanım

1. Uygulamayı başlatın.
2. **Aç** düğmesiyle sistem dosya seçicisinden bir dosya seçin.
3. Metni düzenleyin; **A− / A+** yazı boyutunu, **Kopyala** ise pano içeriğini yönetir.
4. **Kaydet** mevcut URI'ye yazmayı dener; **Farklı Kaydet** yeni bir hedef seçtirir.
5. Yeni belge oluştururken kaydedilmemiş değişiklik uyarısını kontrol edin.

## 🔍 Çalışma akışı

1. `MainActivity` arayüzü oluşturur ve kullanıcı işlemlerini yönetir.
2. Android Storage Access Framework dosya URI'sini sağlar.
3. `DocumentCodec` uzantıyı ve ZIP imzasını kontrol eder.
4. DOCX/ODT içindeki ilgili XML bölümünden metin çıkarılır; HTML/RTF/TXT metin odaklı işlenir.
5. Kullanıcı metni düzenler; sayaç ve kaydedilmemiş değişiklik durumu güncellenir.
6. Kaydetme sırasında içerik seçilen uzantıya uygun kodlayıcıyla yazılır.

## ⚠️ Teknik sınırlamalar

- DOCX/ODT biçimlendirmesi, tablolar, görseller, üstbilgi/altbilgi ve sayfa düzeni korunmaz.
- Eski ikili `.doc` biçimi desteklenmez.
- DOCX okuma esas olarak `word/document.xml` paragraf metnini çıkarır; tüm Word özelliklerini işlemez.
- ODT okuma basit bir `content.xml` metin çıkarımıdır; karmaşık belgeler birebir korunmayabilir.
- RTF ayrıştırması düzenli ifadelerle basitleştirilmiştir; karmaşık biçimlendirme/kodlama kaybolabilir.
- HTML açılırken biçimlendirme metne indirgenir; HTML dışa aktarımı basit bir metin gövdesi oluşturur.
- ODT kaydetme desteği yoktur; yazma işlevi DOCX, TXT/MD, HTML ve RTF uzantılarını işler.
- Kod, bazı belge parçaları için 24 MB okuma sınırı uygular; bu sınır tüm ZIP arşivinin toplam boyutunu tek başına sınırlamaz.
- Eksik Gradle yapılandırması ve olası `R` namespace uyuşmazlığı ilk derlemeyi engelleyebilir.

## 🔐 Güvenli kullanım

- Manifestte ağ izni tanımlı değildir; açık metin trafiği kapalıdır.
- Bilinmeyen kaynaklardan gelen belgeleri açarken dikkatli olun.
- Uygulama otomatik yedek oluşturmaz; önemli belgelerin orijinalini saklayıp kopya üzerinde çalışın.
- Büyük veya bozuk dosyalar cihaz belleğini ve işlem süresini etkileyebilir.

## 🛠️ Modernizasyon önerileri

- Gradle Wrapper, `app/build.gradle.kts`, SDK sürümleri ve namespace ayarlarını ekleyip temiz derlemeyi doğrulamak.
- `R` importunu Android modülünün gerçek namespace'iyle eşleştirmek.
- DOCX/ODT için daha güvenilir metin ayrıştırma ve format doğrulama eklemek.
- ODT kaydetme düğmesini desteklenmediği açıkça belirtilene kadar gizlemek veya gerçek yazma desteği geliştirmek.
- Büyük dosyalarda akış tabanlı işleme, hata günlükleme ve daha anlaşılır hata mesajları eklemek.
- Ekran döndürme/süreç yeniden oluşturma sırasında taslak metni korumak.
- DOCX/XML, RTF kaçışları, HTML karakterleri, boş dosya ve bozuk arşiv senaryoları için testler ve CI derlemesi eklemek.

## 📄 Lisans

Depodaki `LICENSE` dosyası Apache License 2.0 metnini içerir. Ayrıntılar için [LICENSE](LICENSE) dosyasına bakın.

## 👨‍💻 Geliştirici

**Ebubekir Bastama** · [GitHub](https://github.com/ebubekirbastama)

---

<p align="center">EBS Word Studio · Android için çevrimdışı belge metni düzenleme</p>
