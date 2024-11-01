package com.nlbn.ads.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class Helper {
    private static final String FILE_SETTING = "setting.pref";
    private static final String FILE_SETTING_ADMOD = "setting_admod.pref";
    private static final String IS_FIRST_OPEN = "IS_FIRST_OPEN";
    private static final String KEY_FIRST_TIME = "KEY_FIRST_TIME";
    public static boolean isOfficiallyApps = false;

    /**
     * Trả về số click của 1 ads nào đó
     *
     * @param context
     * @param idAds
     * @return
     */
    public static int getNumClickAdsPerDay(Context context, String idAds) {
        return context.getSharedPreferences(FILE_SETTING_ADMOD, Context.MODE_PRIVATE).getInt(idAds, 0);
    }


    /**
     * nếu lần đầu mở app lưu thời gian đầu tiên vào SharedPreferences
     * nếu thời gian hiện tại so với thời gian đầu được 1 ngày thì reset lại data của admod.
     *
     * @param context
     */
    public static void setupAdmobData(Context context) {
        if (isFirstOpenApp(context)) {
            context.getSharedPreferences(FILE_SETTING_ADMOD, Context.MODE_PRIVATE).edit().putLong(KEY_FIRST_TIME, System.currentTimeMillis()).apply();
            context.getSharedPreferences(FILE_SETTING, Context.MODE_PRIVATE).edit().putBoolean(IS_FIRST_OPEN, true).apply();
            return;
        }
        long firstTime = context.getSharedPreferences(FILE_SETTING_ADMOD, Context.MODE_PRIVATE).getLong(KEY_FIRST_TIME, System.currentTimeMillis());
        long rs = System.currentTimeMillis() - firstTime;
       /*
       qua q ngày reset lại data
        */
        if (rs >= 24 * 60 * 60 * 1000) {
            resetAdmobData(context);
        }
    }


    private static void resetAdmobData(Context context) {
        context.getSharedPreferences(FILE_SETTING_ADMOD, Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(FILE_SETTING_ADMOD, Context.MODE_PRIVATE).edit().putLong(KEY_FIRST_TIME, System.currentTimeMillis()).apply();
    }

    private static boolean isFirstOpenApp(Context context) {
        return context.getSharedPreferences(FILE_SETTING, Context.MODE_PRIVATE).getBoolean(IS_FIRST_OPEN, false);
    }

    public static boolean haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                if (ni.isConnected()) {
                    haveConnectedWifi = true;
                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected()) {
                    haveConnectedMobile = true;
                }
            }
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public static String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream inputStream = context.getAssets().open("package_apps.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
