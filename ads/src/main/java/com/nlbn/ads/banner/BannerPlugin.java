package com.nlbn.ads.banner;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import com.nlbn.ads.util.CommonFirebase;

import org.json.JSONException;
import org.json.JSONObject;

public class BannerPlugin {

    private final Activity activity;
    private final ViewGroup adContainer;
    private final ViewGroup shimmer;
    private final Config config;
    private BaseAdView adView;

    public BannerPlugin(Activity activity, ViewGroup adContainer, ViewGroup shimmer, Config config) {
        this.activity = activity;
        this.adContainer = adContainer;
        this.shimmer = shimmer;
        this.config = config;

        initViewAndConfig();

        if (config.loadAdAfterInit) {
            loadAd();
        }
    }

    private void initViewAndConfig() {
        String adUnitId = config.defaultAdUnitId;
        BannerType bannerType = config.defaultBannerType;
        int cbFetchIntervalSec = config.defaultCBFetchIntervalSec;
        int refreshRateSec = config.defaultRefreshRateSec;

        if (config.configKey != null) {
            String data = CommonFirebase.getRemoteConfigAds(activity);
            log("data" + data);
            data = CommonFirebase.getRemoteConfigStringSingle(config.configKey);
            if (!data.isEmpty()) {
                CommonFirebase.setRemoteConfigAds(activity, data);
            } else {
                data = CommonFirebase.getRemoteConfigAds(activity);
            }
            addViewBanner(data);
        } else {
            adView = BaseAdView.Factory.getAdView(
                    activity,
                    adUnitId,
                    bannerType,
                    refreshRateSec,
                    cbFetchIntervalSec,
                    shimmer
            );

            adContainer.addView(
                    adView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            );

            log("\n adUnitId = " + adUnitId +
                    "\n bannerType = " + bannerType +
                    "\n refreshRateSec = " + refreshRateSec +
                    "\n cbFetchIntervalSec = " + cbFetchIntervalSec);
        }
    }

    public void addViewBanner(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);

            if (jsonObject != null) {
                String adUnitId = jsonObject.getString(RemoteConfigManager.adUnitId_key);
                BannerType bannerType = BannerType.valueOf(jsonObject.getString(RemoteConfigManager.type_key));
                int refreshRateSec = jsonObject.getInt(RemoteConfigManager.refreshRateSec_key);
                int cbFetchIntervalSec = jsonObject.getInt(RemoteConfigManager.cbFetchIntervalSec_key);

                log("\n jsonObject " +
                        "\n adUnitId = " + adUnitId +
                        "\n bannerType = " + bannerType +
                        "\n refreshRateSec = " + refreshRateSec +
                        "\n cbFetchIntervalSec = " + cbFetchIntervalSec);

                adView = BaseAdView.Factory.getAdView(
                        activity,
                        adUnitId,
                        bannerType,
                        refreshRateSec,
                        cbFetchIntervalSec,
                        shimmer
                );

                adContainer.addView(
                        adView,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                );
            } else {
                adContainer.removeAllViews();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            adContainer.removeAllViews();
        }
    }

    public void loadAd() {
        if (adView != null) {
            adView.loadAd();
        }
    }

    public static class Config {
        public String defaultAdUnitId;
        public BannerType defaultBannerType;
        public String configKey;
        public int defaultRefreshRateSec = 3000;
        public int defaultCBFetchIntervalSec = 180;
        public boolean loadAdAfterInit = true;

        public String getDefaultAdUnitId() {
            return defaultAdUnitId;
        }

        public void setDefaultAdUnitId(String defaultAdUnitId) {
            this.defaultAdUnitId = defaultAdUnitId;
        }

        public BannerType getDefaultBannerType() {
            return defaultBannerType;
        }

        public void setDefaultBannerType(BannerType defaultBannerType) {
            this.defaultBannerType = defaultBannerType;
        }

        public String getConfigKey() {
            return configKey;
        }

        public void setConfigKey(String configKey) {
            this.configKey = configKey;
        }

        public Integer getDefaultRefreshRateSec() {
            return defaultRefreshRateSec;
        }

        public void setDefaultRefreshRateSec(Integer defaultRefreshRateSec) {
            this.defaultRefreshRateSec = defaultRefreshRateSec;
        }

        public int getDefaultCBFetchIntervalSec() {
            return defaultCBFetchIntervalSec;
        }

        public void setDefaultCBFetchIntervalSec(int defaultCBFetchIntervalSec) {
            this.defaultCBFetchIntervalSec = defaultCBFetchIntervalSec;
        }

        public boolean isLoadAdAfterInit() {
            return loadAdAfterInit;
        }

        public void setLoadAdAfterInit(boolean loadAdAfterInit) {
            this.loadAdAfterInit = loadAdAfterInit;
        }
    }

    public enum BannerType {
        Standard,
        Adaptive,
        CollapsibleTop,
        CollapsibleBottom,
        LargeBanner
    }

    private static boolean LOG_ENABLED = true;

    public static void setLogEnabled(boolean enabled) {
        LOG_ENABLED = enabled;
    }

    private static void log(String message) {
        if (LOG_ENABLED) {
            Log.d("BannerPlugin", message);
        }
    }

    public static void fetchAndActivateRemoteConfig() {
        RemoteConfigManager.fetchAndActivate();
    }
}
