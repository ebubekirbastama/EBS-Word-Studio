EBS WORD STUDIO v1.1 - DOCX OKUYUCU DUZELTMESI

1) ZIP'i acin, EBS Builder icinde proje dizinini EBS_Word_Studio secin.
2) Android SDK / Java 17 / Gradle yollarini kontrol edin.
3) Derlemeyi Baslat -> app/build/outputs/apk/debug/app-debug.apk
4) APK'yi telefona kurun.

DUZELTME
- DOCX/ODT XML okuma sistemi Android'in yerlesik XmlPullParser aracina gecirildi.
- Uygun olmayan DOM parser ozellikleri kaldirildi.
- Dosya imzasi kontrolu ve daha anlasilir hata mesajlari eklendi.
- DOCX belgesine Kaydet komutu, mevcut dosyanin bicimini kaybetmemesi icin Farkli Kaydet ekranini acar.

ONEMLI
- Metin odakli editor: resimler, sayfa duzeni, tablolarin bicimi ve ustbilgi korunmaz.
- DOCX ve ODT belgelerindeki metin ayristirilir; DOC eski format acilmaz.
- Mevcut orijinal Word belgesini DEGISTIRMEYIN. Farkli Kaydet kullanin.
- APK bu ortamda derlenip cihazda test edilemedi.
- EBS Builder kok build.gradle.kts ve app/build.gradle.kts dosyalarini otomatik olusturur.


Güncelleme v1.2:
- EBS Word Studio için özel uygulama ikonu eklendi.
- Uygulama içi marka logosu eklendi.


v1.3: EBS Builder tarafindan olusturulan com.ebs.forensic.detector namespace icin R kaynagi importu duzeltildi.
