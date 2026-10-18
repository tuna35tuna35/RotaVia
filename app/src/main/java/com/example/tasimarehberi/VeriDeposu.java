package com.example.tasimarehberi;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VeriDeposu {

    private final Map<String, Sehir> sehirler = new LinkedHashMap<>();
    private final List<String> arasiAraclar = new ArrayList<>();
    private final List<String> arasiKartlar = new ArrayList<>();

    public VeriDeposu(Context context) throws Exception {
        JSONObject kok = new JSONObject(dosyaOku(context, "transit_data.json"));

        JSONArray sehirDizisi = kok.getJSONArray("sehirler");
        for (int i = 0; i < sehirDizisi.length(); i++) {
            JSONObject s = sehirDizisi.getJSONObject(i);
            String ad = s.getString("ad");
            String kartAdi = s.isNull("kartAdi") ? null : s.getString("kartAdi");
            List<String> araclar = metinListesi(s.getJSONArray("araclar"));

            List<KartTuru> kartlar = new ArrayList<>();
            JSONArray kartDizisi = s.getJSONArray("kartlar");
            for (int k = 0; k < kartDizisi.length(); k++) {
                JSONObject kart = kartDizisi.getJSONObject(k);
                Double ucret = kart.isNull("ucret") ? null : kart.getDouble("ucret");
                kartlar.add(new KartTuru(kart.getString("ad"), ucret, kart.optBoolean("tahmini", false)));
            }

            JSONObject taksi = s.getJSONObject("taksi");
            Double acilis = taksi.isNull("acilis") ? null : taksi.getDouble("acilis");
            Double km = taksi.isNull("km") ? null : taksi.getDouble("km");

            sehirler.put(ad, new Sehir(ad, kartAdi, araclar, kartlar, acilis, km,
                    taksi.optBoolean("tahmini", false)));
        }

        JSONObject arasi = kok.getJSONObject("sehirlerarasi");
        arasiAraclar.addAll(metinListesi(arasi.getJSONArray("araclar")));
        arasiKartlar.addAll(metinListesi(arasi.getJSONArray("kartlar")));
    }

    public List<String> sehirIsimleri() {
        return new ArrayList<>(sehirler.keySet());
    }

    public Sehir sehir(String ad) {
        return sehirler.get(ad);
    }

    public List<String> sehirlerarasiAraclar() {
        return arasiAraclar;
    }

    public List<String> sehirlerarasiKartlar() {
        return arasiKartlar;
    }

    private static List<String> metinListesi(JSONArray dizi) throws Exception {
        List<String> liste = new ArrayList<>();
        for (int i = 0; i < dizi.length(); i++) {
            liste.add(dizi.getString(i));
        }
        return liste;
    }

    private static String dosyaOku(Context context, String ad) throws Exception {
        try (InputStream in = context.getAssets().open(ad)) {
            ByteArrayOutputStream cikis = new ByteArrayOutputStream();
            byte[] tampon = new byte[4096];
            int okunan;
            while ((okunan = in.read(tampon)) != -1) {
                cikis.write(tampon, 0, okunan);
            }
            return new String(cikis.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
