package com.nlbn.ads.util;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.adjust.sdk.OnAttributionChangedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.nlbn.ads.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class AdsApplication extends Application {
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

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.execute(() -> {
            String packages = Helper.loadJSONFromAsset(this);
            String currentPackageApp = this.getApplicationContext().getPackageName();
            if (packages != null) {
                try {
                    JSONArray jsonArray = new JSONArray(packages);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String pckg = jsonArray.getString(i);
                        if (Objects.equals(pckg, currentPackageApp)) {
                            Helper.isOfficiallyApps = true;
                            return;
                        }
                    }
                } catch (JSONException e) {
                    Helper.isOfficiallyApps = false;
                }
            }
        });
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
