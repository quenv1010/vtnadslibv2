package com.nlbn.ads.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.nlbn.ads.R;

import java.util.List;

public abstract class AdsMultiDexApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AppUtil.BUILD_DEBUG = buildDebug();
        Log.i("Application", " run debug: " + AppUtil.BUILD_DEBUG);
        Admob.getInstance().initAdmob(this, getListTestDeviceId());
        PreferenceManager.init(this);

        if (enableAdsResume()) {
            AppOpenManager.getInstance().init(this);
        }
        initRemoteConfig(getDefaultsAsyncFirebase(), getMinimumFetch());

        if (enableAdjustTracking()) {
            Adjust.getInstance().init(this, getAdjustToken(), buildDebug());
        }
    }

    private void initRemoteConfig(int defaultAsync, long minimumFetch) {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(minimumFetch)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(defaultAsync);
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    if (getKeyRemoteIntervalShowInterstitial() == null || getKeyRemoteIntervalShowInterstitial().isEmpty()) {
                        Admob.getInstance().setIntervalShowInterstitial(20);
                    } else {
                        Admob.getInstance().setIntervalShowInterstitial((int) mFirebaseRemoteConfig.getLong(getKeyRemoteIntervalShowInterstitial()));
                    }

                    if (enableRemoteAdsResume()) {
                        if (enableAdsResume()) {
                            AppOpenManager.getInstance().setAppResumeAdId(FirebaseRemoteConfig.getInstance().getString(getKeyRemoteAdsResume()));
                        }
                    } else {
                        if (enableAdsResume()) {
                            AppOpenManager.getInstance().setAppResumeAdId(getResumeAdId());
                        }
                    }
                } else {
                    AppOpenManager.getInstance().setAppResumeAdId(getResumeAdId());
                    Admob.getInstance().setIntervalShowInterstitial(20);
                }
            }
        });
    }

    public abstract boolean enableAdsResume();

    protected boolean enableRemoteAdsResume() {
        return false;
    }

    public abstract String getKeyRemoteIntervalShowInterstitial();

    protected String getKeyRemoteAdsResume() {
        return "";
    }

    protected long getMinimumFetch() {
        return 30L;
    }

    protected int getDefaultsAsyncFirebase() {
        return R.xml.remote_config_defaults;
    }

    protected boolean enableAdjustTracking() {
        return false;
    }

    public abstract List<String> getListTestDeviceId();

    public abstract String getResumeAdId();

    protected OnAdjustAttributionChangedListener getAdjustAttributionChangedListener() {
        return null;
    }

    public void logRevenueAdjustWithCustomEvent(double revenue, String currency) {
    }

    public void logRevenueAppsflyerWithCustomEvent(double revenue, String currency) {
    }

    protected String getAdjustToken() {
        return null;
    }

    public abstract Boolean buildDebug();
}
