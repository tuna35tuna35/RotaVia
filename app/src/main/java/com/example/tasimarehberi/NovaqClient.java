package com.example.tasimarehberi;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NovaqClient implements AiProvider {

    private static final String TAG = "NovaqClient";

    private static final String API_URL = "https://api.kedy.dev/v1/chat/completions";
    private static final String MODEL = "novaq";

    @Override
    public String sor(String sistemMesaji, String kullaniciMesaji) throws Exception {
        try {
            return istekAt(API_URL, sistemMesaji, kullaniciMesaji);
        } catch (Exception e) {
            Log.e(TAG, "API isteği başarısız oldu.", e);
            throw new Exception("Yapay zeka sunucusuna bağlanılamadı: " + e.getMessage());
        }
    }

    private String istekAt(String targetUrl, String sistemMesaji, String kullaniciMesaji) throws Exception {
        JSONObject sistem = new JSONObject()
                .put("role", "system")
                .put("content", sistemMesaji);
        JSONObject kullanici = new JSONObject()
                .put("role", "user")
                .put("content", kullaniciMesaji);

        JSONObject govde = new JSONObject()
                .put("model", MODEL)
                .put("messages", new JSONArray().put(sistem).put(kullanici))
                .put("temperature", 0.1)
                .put("tool_choice", "none");

        HttpURLConnection baglanti = (HttpURLConnection) new URL(targetUrl).openConnection();
        baglanti.setRequestMethod("POST");
        baglanti.setConnectTimeout(10000);
        baglanti.setReadTimeout(30000);
        baglanti.setDoOutput(true);
        baglanti.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        baglanti.setRequestProperty("Authorization", "Bearer " + BuildConfig.NOVAQ_API_KEY);
        baglanti.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");

        try (OutputStream os = baglanti.getOutputStream()) {
            os.write(govde.toString().getBytes(StandardCharsets.UTF_8));
        }

        int kod = baglanti.getResponseCode();
        InputStream akis = (kod >= 200 && kod < 300) ? baglanti.getInputStream() : baglanti.getErrorStream();
        String cevap = hepsiniOku(akis);
        baglanti.disconnect();

        if (kod < 200 || kod >= 300) {
            throw new IOException("API hatası (" + kod + "): " + cevap);
        }

        JSONObject json = new JSONObject(cevap);
        return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }

    private String hepsiniOku(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader okuyucu = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String satir;
            while ((satir = okuyucu.readLine()) != null) {
                sb.append(satir).append('\n');
            }
        }
        return sb.toString();
    }
}