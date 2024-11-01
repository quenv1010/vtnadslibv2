package com.nlbn.ads.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adjust.sdk.AdjustAdRevenue;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.LogLevel;
import com.applovin.mediation.MaxAd;
import com.google.android.gms.ads.AdValue;

class AdjustImpl extends Adjust implements Application.ActivityLifecycleCallbacks {
    private static AdjustImpl INSTANCE;
    AdsApplication adsApplication;

    public static AdjustImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdjustImpl();
        }
        return INSTANCE;
    }

    @Override
    public void init(AdsApplication application, String appToken, Boolean isDebug) {
        this.adsApplication = application;
        String environment = isDebug ? AdjustConfig.ENVIRONMENT_SANDBOX : AdjustConfig.ENVIRONMENT_PRODUCTION;
        AdjustConfig config = new AdjustConfig(application, appToken, environment);

        config.setOnAttributionChangedListener(adjustAttribution -> {
            PreferenceManager.getInstance().putString(PreferenceManager.PREF_ADMOB_NETWORK, adjustAttribution.network.toLowerCase());
//            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//            firestore.collection("adjust").document().set(adjustAttribution);
//            firestore.collection("networks").document(adjustAttribution.network).get().addOnSuccessListener(documentSnapshot -> {
//                AdjustNetworkInfo adjustNetworkInfo = documentSnapshot.toObject(AdjustNetworkInfo.class);
//                if (adjustNetworkInfo != null) {
//                    adjustNetworkInfo.setCount(adjustNetworkInfo.getCount() + 1);
//                    firestore.collection("networks").document(adjustAttribution.network).set(adjustNetworkInfo);
//                } else {
//                    AdjustNetworkInfo adjustNetwork = new AdjustNetworkInfo();
//                    adjustNetwork.setNetworkName(adjustAttribution.network);
//                    adjustNetwork.setCount(1);
//                    firestore.collection("networks").document(adjustAttribution.network).set(adjustNetwork);
//                }
//            });

            if (getOnAdjustAttributionChangedListener() != null) {
                com.nlbn.ads.util.AdjustAttribution attribution = new com.nlbn.ads.util.AdjustAttribution();
                attribution.setNetwork(adjustAttribution.network);
                attribution.setCampaign(adjustAttribution.campaign);
                attribution.setAdgroup(adjustAttribution.adgroup);
                attribution.setCreative(adjustAttribution.creative);
                attribution.setClickLabel(adjustAttribution.clickLabel);
                attribution.setTrackerToken(adjustAttribution.trackerToken);
                attribution.setTrackerName(adjustAttribution.trackerName);
                attribution.setCostType(adjustAttribution.costType);
                attribution.setCostAmount(adjustAttribution.costAmount);
                attribution.setCostCurrency(adjustAttribution.costCurrency);
                attribution.setFbInstallReferrer(adjustAttribution.fbInstallReferrer);
                Adjust.getInstance().getOnAdjustAttributionChangedListener().onAttributionChanged(attribution);
            }

        });
        if (isDebug) {
            config.setLogLevel(LogLevel.VERBOSE);
        }
        com.adjust.sdk.Adjust.initSdk(config);
        application.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void init(Context context, String appToken, Boolean isDebug) {
        String environment = isDebug ? AdjustConfig.ENVIRONMENT_SANDBOX : AdjustConfig.ENVIRONMENT_PRODUCTION;
        AdjustConfig config = new AdjustConfig(context, appToken, environment);

        config.setOnAttributionChangedListener(adjustAttribution -> {
            PreferenceManager.getInstance().putString(PreferenceManager.PREF_ADMOB_NETWORK, adjustAttribution.network.toLowerCase());

            if (getOnAdjustAttributionChangedListener() != null) {
                com.nlbn.ads.util.AdjustAttribution attribution = new com.nlbn.ads.util.AdjustAttribution();
                attribution.setNetwork(adjustAttribution.network);
                attribution.setCampaign(adjustAttribution.campaign);
                attribution.setAdgroup(adjustAttribution.adgroup);
                attribution.setCreative(adjustAttribution.creative);
                attribution.setClickLabel(adjustAttribution.clickLabel);
                attribution.setTrackerToken(adjustAttribution.trackerToken);
                attribution.setTrackerName(adjustAttribution.trackerName);
                attribution.setCostType(adjustAttribution.costType);
                attribution.setCostAmount(adjustAttribution.costAmount);
                attribution.setCostCurrency(adjustAttribution.costCurrency);
                attribution.setFbInstallReferrer(adjustAttribution.fbInstallReferrer);
                Adjust.getInstance().getOnAdjustAttributionChangedListener().onAttributionChanged(attribution);
            }
        });
        if (isDebug) {
            config.setLogLevel(LogLevel.VERBOSE);
        }
        com.adjust.sdk.Adjust.initSdk(config);
        ((Application) context).registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void trackAdRevenue(AdValue adValue) {
        if (adsApplication != null && adsApplication.enableAdjustTracking()) {
            AdjustAdRevenue revenue = new AdjustAdRevenue("admob_sdk");
            revenue.setRevenue((double) adValue.getValueMicros() / 1000000, adValue.getCurrencyCode());
            com.adjust.sdk.Adjust.trackAdRevenue(revenue);
            adsApplication.logRevenueAdjustWithCustomEvent((double) adValue.getValueMicros() / 1000000, adValue.getCurrencyCode());
        }
    }

    @Override
    public OnAdjustAttributionChangedListener getOnAdjustAttributionChangedListener() {
        return adsApplication.getAdjustAttributionChangedListener();
    }

    @Override
    public void trackMaxAdRevenue(MaxAd maxAd) {
        AdjustAdRevenue revenue = new AdjustAdRevenue("applovin_max_sdk");
        revenue.setRevenue(maxAd.getRevenue(), "USD");
        revenue.setAdRevenueNetwork(maxAd.getNetworkName());
        revenue.setAdRevenueUnit(maxAd.getAdUnitId());
        com.adjust.sdk.Adjust.trackAdRevenue(revenue);
    }

    @Override
    public void logRevenueWithCustomEvent(String eventName, double revenue, String currency) {
        AdjustEvent event = new AdjustEvent(eventName);
        event.setRevenue(revenue, currency);
        com.adjust.sdk.Adjust.trackEvent(event);
    }

    @Override
    protected boolean isNonOrganic() {
        return !isOrganic();
    }

    @Override
    protected boolean isOrganic() {
        String networkType = PreferenceManager.getInstance().getString(PreferenceManager.PREF_ADMOB_NETWORK, "organic");
        return networkType.equals(PreferenceManager.ORGANIC);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        com.adjust.sdk.Adjust.onResume();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        com.adjust.sdk.Adjust.onPause();
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
