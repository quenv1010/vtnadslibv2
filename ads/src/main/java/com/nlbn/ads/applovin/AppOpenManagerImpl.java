package com.nlbn.ads.applovin;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdRevenueListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAppOpenAd;
import com.google.android.gms.ads.AdActivity;
import com.nlbn.ads.dialog.ResumeLoadingDialog;
import com.nlbn.ads.util.Adjust;
import com.nlbn.ads.util.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class AppOpenManagerImpl extends AppOpenManager implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private static AppOpenManagerImpl instance;
    private Application myApplication;
    private final List<Class> disabledAppOpenList;
    private boolean isInitialize = false;
    private boolean isAppResumeEnabled = true;
    private Activity currentActivity;
    private boolean isInterstitialShowing = false;
    private boolean isShowingAdResume = false;
    private String appResumeAdId;

    public static synchronized AppOpenManagerImpl getInstance() {
        if (instance == null) instance = new AppOpenManagerImpl();
        return instance;
    }

    private AppOpenManagerImpl() {
        disabledAppOpenList = new ArrayList<>();
    }

    @Override
    public void init(Application application, String appResumeAdId) {
        myApplication = application;
        this.appResumeAdId = appResumeAdId;
        isInitialize = true;
        myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

    }

    @Override
    public boolean isInitialize() {
        return isInitialize;
    }

    @Override
    public void enableAppResumeWithActivity(Class activityClass) {
        disabledAppOpenList.remove(activityClass);
    }

    @Override
    public void disableAppResumeWithActivity(Class activityClass) {
        disabledAppOpenList.add(activityClass);

    }

    @Override
    public void disableAppResume() {
        isAppResumeEnabled = false;
    }

    @Override
    public void enableAppResume() {
        isAppResumeEnabled = true;
    }

    @Override
    public void setAppResumeAdId(String appResumeAdId) {
        this.appResumeAdId = appResumeAdId;
    }

    @Override
    public boolean isInterstitialShowing() {
        return isInterstitialShowing;
    }

    @Override
    public void setInterstitialShowing(boolean interstitialShowing) {
        isInterstitialShowing = interstitialShowing;

    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        if (!isAppResumeEnabled || isInterstitialShowing || isShowingAdResume) return;
        for (Class activity : disabledAppOpenList) {
            if (activity.getName().equals(currentActivity.getClass().getName())) {
                return;
            }
        }
        loadAdsResume();
    }

    Dialog dialog = null;
    private MaxAppOpenAd appOpenAd;

    private void loadAdsResume() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = new ResumeLoadingDialog(currentActivity);
        dialog.show();
        appOpenAd = new MaxAppOpenAd(appResumeAdId, currentActivity);
        appOpenAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(@NonNull MaxAd maxAd) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                if (appOpenAd.isReady())
                    appOpenAd.showAd();
            }

            @Override
            public void onAdDisplayed(@NonNull MaxAd maxAd) {

            }

            @Override
            public void onAdHidden(@NonNull MaxAd maxAd) {
                appOpenAd = null;
            }

            @Override
            public void onAdClicked(@NonNull MaxAd maxAd) {

            }

            @Override
            public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
                System.out.println(maxError.getMessage());
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
            }

            @Override
            public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {

            }
        });
        appOpenAd.setRevenueListener(maxAd -> Adjust.getInstance().trackMaxAdRevenue(maxAd));
        appOpenAd.loadAd();
    }
}
