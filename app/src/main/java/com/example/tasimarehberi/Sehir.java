package com.example.tasimarehberi;

import java.util.List;

public class Sehir {
    public final String ad;
    public final String kartAdi;
    public final List<String> araclar;
    public final List<KartTuru> kartlar;
    public final Double taksiAcilis;
    public final Double taksiKm;
    public final boolean taksiTahmini;

    public Sehir(String ad, String kartAdi, List<String> araclar, List<KartTuru> kartlar,
                 Double taksiAcilis, Double taksiKm, boolean taksiTahmini) {
        this.ad = ad;
        this.kartAdi = kartAdi;
        this.araclar = araclar;
        this.kartlar = kartlar;
        this.taksiAcilis = taksiAcilis;
        this.taksiKm = taksiKm;
        this.taksiTahmini = taksiTahmini;
    }
}
