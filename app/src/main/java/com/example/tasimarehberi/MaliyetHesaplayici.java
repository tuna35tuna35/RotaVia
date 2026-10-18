package com.example.tasimarehberi;

import java.util.List;
import java.util.Locale;

public class MaliyetHesaplayici {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");

    public static String sehirIci(Sehir sehir, KartTuru kart, int binis, List<String> araclar) {
        StringBuilder sb = new StringBuilder();

        boolean taksiVar = false;
        boolean topluTasimaVar = false;
        boolean dolmusVar = false;
        for (String arac : araclar) {
            String k = arac.toLowerCase(TR);
            if (k.equals("taksi")) {
                taksiVar = true;
            } else {
                topluTasimaVar = true;
                if (k.contains("dolmuş") || k.contains("minibüs")) {
                    dolmusVar = true;
                }
            }
        }

        if (topluTasimaVar) {
            if (kart.ucret == null) {
                sb.append("Toplu taşıma: ücret bilgisi bulunamadı");
            } else if (kart.ucret == 0.0) {
                sb.append("Toplu taşıma: Ücretsiz (").append(kart.ad).append(")");
            } else {
                double toplam = kart.ucret * binis;
                sb.append("Toplu taşıma: ")
                        .append(kart.tahmini ? "yaklaşık " : "")
                        .append(tl(toplam))
                        .append(" (").append(binis).append(" biniş × ")
                        .append(tl(kart.ucret)).append(", ").append(kart.ad).append(")");
            }
        }

        if (taksiVar) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            if (sehir.taksiAcilis != null && sehir.taksiKm != null) {
                sb.append("Taksi: açılış ").append(tl(sehir.taksiAcilis))
                        .append(" + km başına ").append(tl(sehir.taksiKm))
                        .append(sehir.taksiTahmini ? " (tahmini tarife)" : "");
            } else {
                sb.append("Taksi: tarife bilgisi bulunamadı");
            }
        }

        if (dolmusVar) {
            sb.append("\nDolmuş ve minibüs ücretleri hatta göre değişir, kart indirimi geçmeyebilir.");
        }

        return sb.toString();
    }

    public static String sehirlerArasi() {
        return "Şehirler arası ücret, seçtiğin araca göre değiştiği için rota açıklamasında tahmini aralık olarak verilir.";
    }

    private static String tl(double deger) {
        return String.format(TR, "%.2f TL", deger);
    }
}