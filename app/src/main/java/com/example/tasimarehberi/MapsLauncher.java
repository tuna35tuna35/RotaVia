package com.example.tasimarehberi;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class MapsLauncher {

    public static void openTransit(Context context, String city, String from, String to) {
        String origin = from + ", " + city;
        String destination = to + ", " + city;
        openRoute(context, origin, destination, "transit");
    }

    public static void openIntercity(Context context, String fromCity, String toCity) {
        openRoute(context, fromCity, toCity, "transit");
    }

    public static void openDriving(Context context, String city, String from, String to) {
        openRoute(context, from + ", " + city, to + ", " + city, "driving");
    }

    public static void openRoute(Context context, String origin, String destination, String travelMode) {
        Uri uri = Uri.parse("https://www.google.com/maps/dir/").buildUpon()
                .appendQueryParameter("api", "1")
                .appendQueryParameter("origin", origin)
                .appendQueryParameter("destination", destination)
                .appendQueryParameter("travelmode", travelMode)
                .build();

        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapsIntent.setPackage("com.google.android.apps.maps");
        try {
            context.startActivity(mapsIntent);
        } catch (ActivityNotFoundException e) {
            try {
                // Harita uygulaması yoksa tarayıcıda açar
                context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(context, "Harita açılamadı: Google Maps ya da tarayıcı bulunamadı.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}