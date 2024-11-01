package com.nlbn.ads.banner;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.nlbn.ads.util.CommonFirebase;

public class RemoteConfigManager {

    private Gson gson = new Gson();

    public static void fetchAndActivate() {
        FirebaseRemoteConfig.getInstance().fetchAndActivate();
    }

    public BannerConfig getBannerConfig(Activity activity, String key) {
        return getConfig(activity, key, BannerConfig.class);
    }

    private <T> T getConfig(Activity activity, String configName, Class<T> type) {
        try {
            Log.d("Banner plugin", "getConfig");
            String data = CommonFirebase.getRemoteConfigStringSingle(configName);
            return gson.fromJson(data, type);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public class BannerConfig {
        @SerializedName("ad_unit_id")
        private String adUnitId;
        @SerializedName("type")
        private String type;
        @SerializedName("refresh_rate_sec")
        private Integer refreshRateSec;
        @SerializedName("cb_fetch_interval_sec")
        private Integer cbFetchIntervalSec;

        public String getAdUnitId() {
            return adUnitId;
        }

        public String getType() {
            return type;
        }

        public Integer getRefreshRateSec() {
            return refreshRateSec;
        }

        public Integer getCbFetchIntervalSec() {
            return cbFetchIntervalSec;
        }
    }

    public static final String TYPE_STANDARD = "standard";
    public static final String TYPE_ADAPTIVE = "adaptive";
    public static final String TYPE_COLLAPSIBLE_TOP = "collapsible_top";
    public static final String TYPE_COLLAPSIBLE_BOTTOM = "collapsible_bottom";
    public static final String adUnitId_key = "ad_unit_id";
    public static final String type_key = "type";
    public static final String refreshRateSec_key = "refresh_rate_sec";
    public static final String cbFetchIntervalSec_key = "cb_fetch_interval_sec";
}






