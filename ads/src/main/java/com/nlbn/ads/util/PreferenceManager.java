package com.nlbn.ads.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "pref_ads_libs";
    public static final String PREF_ADMOB_NETWORK = "admob_network";
    public static final String PREF_ADMOB_NETWORK_APPSFLYER = "admob_network_appsflyer";
    public static final String ORGANIC = "organic";
    public static final String UNTRUSTED_DEVICES = "untrusted";
    public static String ADS_SPACE_15_SECOND = "ADS_SPACE_15_SECOND";

    private static SharedPreferences.Editor editor;
    private static PreferenceManager instance;
    private static SharedPreferences mShare;

    public static void init(Context context) {
        instance = new PreferenceManager();
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, 0);
        mShare = sharedPreferences;
        editor = sharedPreferences.edit();
    }

    public static PreferenceManager getInstance() {
        return instance;
    }

    public void remove(String str) {
        if (mShare.contains(str)) {
            editor.remove(str);
            editor.commit();
        }
    }

    public void putBoolean(String str, boolean z) {
        mShare.edit().putBoolean(str, z).apply();
    }

    public void putInt(String str, int i) {
        mShare.edit().putInt(str, i).apply();
    }

    public void putFloat(String str, float i) {
        mShare.edit().putFloat(str, i).apply();
    }

    public void putLong(String str, long j) {
        mShare.edit().putLong(str, j).apply();
    }

    public boolean getBoolean(String str) {
        return mShare.getBoolean(str, false);
    }

    public int getInt(String str) {
        return mShare.getInt(str, 0);
    }

    public long getLong(String str) {
        return mShare.getLong(str, 0);
    }

    public float getFloat(String str) {
        return mShare.getFloat(str, 0f);
    }

    public void putString(String str, String str2) {
        mShare.edit().putString(str, str2).apply();
    }

    public String getString(String str, String str2) {
        return mShare.getString(str, str2);
    }
}
