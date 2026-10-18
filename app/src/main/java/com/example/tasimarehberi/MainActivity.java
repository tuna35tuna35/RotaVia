package com.example.tasimarehberi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String SEHIR_ISTEM = "Lütfen şehir seçiniz";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private VeriDeposu veri;
    private final AiProvider ai = new NovaqClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler anaThread = new Handler(Looper.getMainLooper());
    private FusedLocationProviderClient fusedLocationClient;

    private RadioGroup rgTur;
    private View layoutSehirIci, layoutSehirlerArasi, layoutSecimler;
    private Spinner spinnerSehir, spinnerNereden, spinnerNereye;
    private TextInputEditText etFrom, etTo;
    private TextView tvKartBaslik, tvMaliyet, tvRota;
    private ChipGroup chipAraclar, chipKartlar;
    private ProgressBar progress;
    private MaterialCardView cardSonuc;
    private Button btnHesapla, btnHaritada, btnKonumAl;

    private GoogleMap mMap;
    private boolean sehirIciModu = true;
    private String sonSehir, sonNereden, sonNereye;
    private boolean sonSadeceTaksi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            veri = new VeriDeposu(this);
        } catch (Exception e) {
            Toast.makeText(this, "Veri dosyası okunamadı: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        gorunumleriBagla();
        spinnerlariDoldur();
        dinleyicileriKur();

        // Harita fragment'ını başlat
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Varsayılan olarak Türkiye / İzmir merkezli gösterim
        LatLng izmir = newLatLngSafely(38.4192, 27.1287);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(izmir, 12f));
    }

    private LatLng newLatLngSafely(double lat, double lng) {
        return new LatLng(lat, lng);
    }

    private void gorunumleriBagla() {
        rgTur = findViewById(R.id.rgTur);
        layoutSehirIci = findViewById(R.id.layoutSehirIci);
        layoutSehirlerArasi = findViewById(R.id.layoutSehirlerArasi);
        layoutSecimler = findViewById(R.id.layoutSecimler);
        spinnerSehir = findViewById(R.id.spinnerSehir);
        spinnerNereden = findViewById(R.id.spinnerNereden);
        spinnerNereye = findViewById(R.id.spinnerNereye);
        etFrom = findViewById(R.id.etFrom);
        etTo = findViewById(R.id.etTo);
        tvKartBaslik = findViewById(R.id.tvKartBaslik);
        tvMaliyet = findViewById(R.id.tvMaliyet);
        tvRota = findViewById(R.id.tvRota);
        chipAraclar = findViewById(R.id.chipAraclar);
        chipKartlar = findViewById(R.id.chipKartlar);
        progress = findViewById(R.id.progress);
        cardSonuc = findViewById(R.id.cardSonuc);
        btnHesapla = findViewById(R.id.btnHesapla);
        btnHaritada = findViewById(R.id.btnHaritada);
        btnKonumAl = findViewById(R.id.btnKonumAl);
    }

    private void spinnerlariDoldur() {
        List<String> sehirListesi = new ArrayList<>();
        sehirListesi.add(SEHIR_ISTEM);
        sehirListesi.addAll(veri.sehirIsimleri());

        spinnerSehir.setAdapter(adaptor(sehirListesi));
        spinnerSehir.setSelection(0);

        spinnerNereden.setAdapter(adaptor(sehirListesi));
        spinnerNereden.setSelection(0);

        spinnerNereye.setAdapter(adaptor(sehirListesi));
        spinnerNereye.setSelection(0);
    }

    private ArrayAdapter<String> adaptor(List<String> liste) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, liste);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    private void dinleyicileriKur() {
        rgTur.setOnCheckedChangeListener((grup, id) -> {
            sehirIciModu = (id == R.id.rbSehirIci);
            layoutSehirIci.setVisibility(sehirIciModu ? View.VISIBLE : View.GONE);
            layoutSehirlerArasi.setVisibility(sehirIciModu ? View.GONE : View.VISIBLE);
            secimleriSifirla();
        });

        AdapterView.OnItemSelectedListener sifirlayici = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                secimleriSifirla();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerSehir.setOnItemSelectedListener(sifirlayici);
        spinnerNereden.setOnItemSelectedListener(sifirlayici);
        spinnerNereye.setOnItemSelectedListener(sifirlayici);

        Button btnAraclariGoster = findViewById(R.id.btnAraclariGoster);
        if (btnAraclariGoster != null) {
            btnAraclariGoster.setOnClickListener(v -> araclariGoster());
        }

        Button btnArasiSecenekler = findViewById(R.id.btnArasiSecenekler);
        if (btnArasiSecenekler != null) {
            btnArasiSecenekler.setOnClickListener(v -> arasiSecenekleriGoster());
        }

        if (btnHesapla != null) {
            btnHesapla.setOnClickListener(v -> hesapla());
        }

        if (btnHaritada != null) {
            btnHaritada.setOnClickListener(v -> haritadaAc());
        }

        if (btnKonumAl != null) {
            btnKonumAl.setOnClickListener(v -> konumumuOtomatikAl());
        }
    }

    private void konumumuOtomatikAl() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    String koordinat = location.getLatitude() + ", " + location.getLongitude();
                    etFrom.setText(koordinat);
                    if (mMap != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                    }
                    toast("Mevcut konumunuz alındı!");
                } else {
                    toast("GPS açık değil veya konum alınamadı.");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                konumumuOtomatikAl();
            } else {
                toast("Konum izni reddedildi.");
            }
        }
    }

    private void secimleriSifirla() {
        if (chipAraclar != null) chipAraclar.removeAllViews();
        if (chipKartlar != null) chipKartlar.removeAllViews();
        if (layoutSecimler != null) layoutSecimler.setVisibility(View.GONE);
        if (cardSonuc != null) cardSonuc.setVisibility(View.GONE);
    }

    private void araclariGoster() {
        if (spinnerSehir.getSelectedItemPosition() == 0) {
            toast("Lütfen önce şehir seç.");
            return;
        }
        if (metin(etFrom).isEmpty() || metin(etTo).isEmpty()) {
            toast("Başlangıç ve varış konumunu gir.");
            return;
        }

        Sehir sehir = veri.sehir((String) spinnerSehir.getSelectedItem());
        secimleriSifirla();

        if (sehir != null) {
            if (sehir.araclar != null) {
                for (String arac : sehir.araclar) {
                    etiketEkle(chipAraclar, arac, -1, false);
                }
            }
            if (sehir.kartlar != null) {
                for (int i = 0; i < sehir.kartlar.size(); i++) {
                    etiketEkle(chipKartlar, sehir.kartlar.get(i).ad, i, i == 0);
                }
            }
            if (tvKartBaslik != null) {
                tvKartBaslik.setText(sehir.kartAdi != null ? sehir.kartAdi + " kart türün" : "Kart türün");
            }
        }
        if (layoutSecimler != null) {
            layoutSecimler.setVisibility(View.VISIBLE);
        }
    }

    private void arasiSecenekleriGoster() {
        int a = spinnerNereden.getSelectedItemPosition();
        int b = spinnerNereye.getSelectedItemPosition();
        if (a == 0 || b == 0) {
            toast("Nereden ve nereye şehirlerini seç.");
            return;
        }
        if (a == b) {
            toast("Başlangıç ve varış şehri aynı olamaz.");
            return;
        }

        secimleriSifirla();

        for (String arac : veri.sehirlerarasiAraclar()) {
            etiketEkle(chipAraclar, arac, -1, false);
        }
        List<String> kartlar = veri.sehirlerarasiKartlar();
        for (int i = 0; i < kartlar.size(); i++) {
            etiketEkle(chipKartlar, kartlar.get(i), i, i == 0);
        }

        if (tvKartBaslik != null) {
            tvKartBaslik.setText("İndirim / kart türün");
        }
        if (layoutSecimler != null) {
            layoutSecimler.setVisibility(View.VISIBLE);
        }
    }

    private void etiketEkle(ChipGroup grup, String yazi, int sira, boolean secili) {
        if (grup == null) return;
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_chip, grup, false);
        chip.setText(yazi);
        chip.setCheckable(true);
        chip.setTag(sira);
        chip.setChecked(secili);
        grup.addView(chip);
    }

    private List<String> secilenAraclar() {
        List<String> liste = new ArrayList<>();
        if (chipAraclar == null) return liste;
        for (int i = 0; i < chipAraclar.getChildCount(); i++) {
            Chip chip = (Chip) chipAraclar.getChildAt(i);
            if (chip != null && chip.isChecked()) {
                liste.add(chip.getText().toString());
            }
        }
        return liste;
    }

    private int secilenKartSirasi() {
        if (chipKartlar == null) return -1;
        for (int i = 0; i < chipKartlar.getChildCount(); i++) {
            Chip chip = (Chip) chipKartlar.getChildAt(i);
            if (chip != null && chip.isChecked()) {
                Object tag = chip.getTag();
                if (tag instanceof Integer) {
                    return (Integer) tag;
                }
            }
        }
        return -1;
    }

    private String secilenKartAdi() {
        if (chipKartlar == null) return "Tam Kart";
        for (int i = 0; i < chipKartlar.getChildCount(); i++) {
            Chip chip = (Chip) chipKartlar.getChildAt(i);
            if (chip != null && chip.isChecked()) {
                return chip.getText().toString();
            }
        }
        return "Tam Kart";
    }

    private void hesapla() {
        List<String> araclar = secilenAraclar();
        if (araclar.isEmpty()) {
            toast("En az bir araç seç.");
            return;
        }
        int kartSirasi = secilenKartSirasi();
        if (kartSirasi < 0) {
            toast("Kart türünü seç.");
            return;
        }

        String kartAdi = secilenKartAdi();
        String istek;
        String kalkis = metin(etFrom);
        String varis = metin(etTo);

        if (sehirIciModu) {
            Sehir sehir = veri.sehir((String) spinnerSehir.getSelectedItem());
            if (sehir == null) {
                toast("Geçerli bir şehir seçiniz.");
                return;
            }
            istek = RoutePrompt.sehirIci(sehir.ad, kalkis, varis, araclar, kartAdi);
            sonSehir = sehir.ad;
            sonNereden = kalkis;
            sonNereye = varis;
            sonSadeceTaksi = araclar.size() == 1 && araclar.get(0).equals("Taksi");

            // Harita üzerinde örnek bir konum işareti göster
            if (mMap != null) {
                mMap.clear();
                LatLng sampleLoc = new LatLng(38.4192, 27.1287);
                mMap.addMarker(new MarkerOptions().position(sampleLoc).title("Rota Başlangıcı: " + kalkis));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sampleLoc, 13f));
            }

        } else {
            String nereden = (String) spinnerNereden.getSelectedItem();
            String nereye = (String) spinnerNereye.getSelectedItem();
            istek = RoutePrompt.sehirlerArasi(nereden, nereye, araclar);
            sonSehir = null;
            sonNereden = nereden;
            sonNereye = nereye;
            sonSadeceTaksi = false;

            if (mMap != null) {
                mMap.clear();
                LatLng sampleLoc = new LatLng(39.9334, 32.8597);
                mMap.addMarker(new MarkerOptions().position(sampleLoc).title(nereden + " -> " + nereye));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sampleLoc, 7f));
            }
        }

        if (tvMaliyet != null) tvMaliyet.setText("");
        if (tvRota != null) tvRota.setText("");
        if (cardSonuc != null) cardSonuc.setVisibility(View.VISIBLE);
        if (progress != null) progress.setVisibility(View.VISIBLE);
        if (btnHesapla != null) btnHesapla.setEnabled(false);

        final String sonIstek = istek;
        executor.execute(() -> {
            String cevap;
            try {
                cevap = ai.sor(RoutePrompt.SYSTEM, sonIstek);
            } catch (Exception e) {
                cevap = "Yapay zeka rota açıklaması şu an alınamadı: " + e.getMessage();
            }
            final String sonuc = cevap;
            anaThread.post(() -> {
                if (progress != null) progress.setVisibility(View.GONE);
                if (btnHesapla != null) btnHesapla.setEnabled(true);
                if (tvRota != null) tvRota.setText(sonuc);
            });
        });
    }

    private void haritadaAc() {
        if (sonNereden == null) {
            toast("Önce bir rota hesaplamalısınız.");
            return;
        }
        if (sehirIciModu) {
            if (sonSadeceTaksi) {
                MapsLauncher.openDriving(this, sonSehir, sonNereden, sonNereye);
            } else {
                MapsLauncher.openTransit(this, sonSehir, sonNereden, sonNereye);
            }
        } else {
            MapsLauncher.openIntercity(this, sonNereden, sonNereye);
        }
    }

    private String metin(TextInputEditText et) {
        return (et == null || et.getText() == null) ? "" : et.getText().toString().trim();
    }

    private void toast(String mesaj) {
        Toast.makeText(this, mesaj, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}