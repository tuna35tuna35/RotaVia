package com.example.tasimarehberi;

import java.util.List;

public class RoutePrompt {

    public static final String SYSTEM =
            "Sen TeknoLig 2026 'Rotavia' akıllı ulaşım asistanısın. Görevin kullanıcıya girilen kalkış, varış, şehir, araç ve kart tercihlerine göre en mantıklı adım adım toplu taşıma rotasını ve detaylı ücret hesaplamasını sunmaktır.\n\n" +
                    "Sadece Türkçe yanıt ver. Markdown (yıldızlar, kalın yazılar vb. süslü işaretler) KULLANMA. Sade ve net düz metin yaz.\n" +
                    "Asla 'I have no tools' veya 'Google Maps kullanın' deme.";

    public static String sehirIci(String sehir, String nereden, String nereye, List<String> araclar, String kartAdi) {
        String aracListesi = (araclar != null && !araclar.isEmpty()) ? String.join(", ", araclar) : "Toplu Taşıma Araçları";

        return String.format(
                "Şehir içi rota hesaplama isteği:\n" +
                        "Şehir: %s\n" +
                        "Kalkış Noktası: %s\n" +
                        "Varış Noktası: %s\n" +
                        "Tercih Edilen Araçlar: %s\n" +
                        "Kullanılan Kart / Tarife: %s\n\n" +
                        "Bu bilgilere dayanarak; yürüme, biniş, olası aktarmalar, tahmini süre ve seçilen karta uygun bilet/ücret hesaplamasını içeren adım adım yol tarifini tam olarak Türkçe düz metin formatında oluştur.",
                sehir, nereden, nereye, aracListesi, kartAdi
        );
    }

    public static String sehirlerArasi(String nereden, String nereye, List<String> araclar) {
        String aracListesi = (araclar != null && !araclar.isEmpty()) ? String.join(", ", araclar) : "Otobüs, Tren, Uçak";

        return String.format(
                "Şehirler arası rota hesaplama isteği:\n" +
                        "Kalkış Şehri: %s\n" +
                        "Varış Şehri: %s\n" +
                        "Tercih Edilen Ulaşım Araçları: %s\n\n" +
                        "Bu bilgilere dayanarak; otogara/istasyona şehir içi ulaşım, ana hat seyahati ve varış noktasındaki şehir içi geçiş detaylarını, tahmini süreyi ve tahmini bilet maliyetini içeren seyahat planını tam olarak Türkçe düz metin formatında oluştur.",
                nereden, nereye, aracListesi
        );
    }
}