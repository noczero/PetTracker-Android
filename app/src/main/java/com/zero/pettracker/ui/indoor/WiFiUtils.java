package com.zero.pettracker.ui.indoor;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;

public final class WiFiUtils {
    private static final double DISTANCE_MHZ_M = 27.55;
    private static final int MIN_RSSI = -100;
    private static final int MAX_RSSI = -55;
    private static final String QUOTE = "\"";

    private WiFiUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static double calculateDistance(int frequency, int level) {
        // Log.d("DISTANCE : ", "Freq. : " + frequency + " - Level : " + level);
        return Math.pow(10.0, (DISTANCE_MHZ_M - (20 * Math.log10(frequency)) + Math.abs(level)) / 20.0);
    }

    public static int calculateSignalLevel(int rssi, int numLevels) {
        if (rssi <= MIN_RSSI) {
            return 0;
        }
        if (rssi >= MAX_RSSI) {
            return numLevels - 1;
        }
        return (rssi - MIN_RSSI) * (numLevels - 1) / (MAX_RSSI - MIN_RSSI);
    }

    @NonNull
    public static String convertSSID(@NonNull String ssid) {
        return StringUtils.removeEnd(StringUtils.removeStart(ssid, QUOTE), QUOTE);
    }



}
