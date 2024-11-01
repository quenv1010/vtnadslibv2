package com.nlbn.ads.util;

import android.app.Application;
import android.util.Log;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.adrevenue.AppsFlyerAdRevenue;
import com.appsflyer.adrevenue.adnetworks.generic.MediationNetwork;
import com.appsflyer.adrevenue.adnetworks.generic.Scheme;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.ads.AdValue;
import com.nlbn.ads.model.GetDataAppsFlyerSuccessEvent;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

class AppFlyerImpl extends AppFlyer {
    private static AppFlyerImpl INSTANCE;
    private boolean enableTrackingAppFlyerRevenue = false;
    private static final String TAG = "AppFlyer";
    private AdsApplication adsApplication;
    private boolean enableAppsFlyer = false;

    public static AppFlyerImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppFlyerImpl();
        }
        return INSTANCE;
    }

    @Override
    public void initAppFlyer(Application context, String devKey, boolean enableTrackingAppFlyerRevenue) {
        this.enableTrackingAppFlyerRevenue = enableTrackingAppFlyerRevenue;
        adsApplication = (AdsApplication) context;
        initAppFlyer(context, devKey, enableTrackingAppFlyerRevenue, false, false);
    }


    @Override
    public void initAppFlyerDebug(Application context, String devKey, boolean enableDebugLog) {
        initAppFlyer(context, devKey, true, enableDebugLog, false);

    }

    @Override
    public void initAppFlyer(Application context, String devKey, boolean enableTrackingAppFlyerRevenue, boolean enableDebug) {
        initAppFlyer(context, devKey, enableTrackingAppFlyerRevenue, enableDebug, false);
    }

    @Override
    public void initAppFlyer(Application context, String devKey, boolean enableTrackingAppFlyerRevenue, boolean enableDebug, boolean enableTrackingNetwork) {
        adsApplication = (AdsApplication) context;
        this.enableTrackingAppFlyerRevenue = enableTrackingAppFlyerRevenue;
        enableAppsFlyer = true;
        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> map) {
                if (map.containsKey("af_status") && map.get("af_status") != null) {
                    try {
                        String status = Objects.requireNonNull(map.get("af_status")).toString();
                        PreferenceManager.getInstance().putString(PreferenceManager.PREF_ADMOB_NETWORK_APPSFLYER, status.toLowerCase());
                        EventBus.getDefault().post(new GetDataAppsFlyerSuccessEvent());

                        if (enableTrackingNetwork) {
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            firestore.collection("networks").document(status).get().addOnSuccessListener(documentSnapshot -> {
                                AppsflyerNetworkInfo appsflyerNetworkInfo = documentSnapshot.toObject(AppsflyerNetworkInfo.class);
                                if (appsflyerNetworkInfo != null) {
                                    appsflyerNetworkInfo.setCount(appsflyerNetworkInfo.getCount() + 1);
                                    firestore.collection("networks").document(status).set(appsflyerNetworkInfo);
                                } else {
                                    AdjustNetworkInfo appsflyerNetwork = new AdjustNetworkInfo();
                                    appsflyerNetwork.setNetworkName(status);
                                    appsflyerNetwork.setCount(1);
                                    firestore.collection("networks").document(status).set(appsflyerNetwork);
                                }
                            });
                        }
                    } catch (NullPointerException e) {
                        PreferenceManager.getInstance().putString(PreferenceManager.PREF_ADMOB_NETWORK_APPSFLYER, "organic");
                    }
                } else {
                    PreferenceManager.getInstance().putString(PreferenceManager.PREF_ADMOB_NETWORK_APPSFLYER, "organic");
                }
            }

            @Override
            public void onConversionDataFail(String s) {
                PreferenceManager.getInstance().putString(PreferenceManager.PREF_ADMOB_NETWORK_APPSFLYER, "organic");
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> map) {

            }

            @Override
            public void onAttributionFailure(String s) {
                PreferenceManager.getInstance().putString(PreferenceManager.PREF_ADMOB_NETWORK_APPSFLYER, "organic");
            }
        };

        AppsFlyerLib.getInstance().init(devKey, conversionListener, context);
        AppsFlyerLib.getInstance().start(context);

        AppsFlyerAdRevenue.Builder afRevenueBuilder = new AppsFlyerAdRevenue.Builder(context);
        AppsFlyerAdRevenue.initialize(afRevenueBuilder.build());
        AppsFlyerLib.getInstance().setDebugLog(enableDebug);
    }

    @Override
    public void pushTrackEventAdmob(AdValue adValue, String adId, String adType) {
        Log.e(TAG, "Log tracking event AppFlyer: enableAppFlyer:" + this.enableTrackingAppFlyerRevenue + " --- AdType: " + adType + " --- value: " + adValue.getValueMicros() / 1000000);
        if (adsApplication != null && enableTrackingAppFlyerRevenue) {
            Map<String, String> customParams = new HashMap<>();
            customParams.put(Scheme.AD_UNIT, adId);
            customParams.put(Scheme.AD_TYPE, adType);
            AppsFlyerAdRevenue.logAdRevenue(
                    "Admob",
                    MediationNetwork.googleadmob,
                    Currency.getInstance(Locale.US),
                    (double) adValue.getValueMicros() / 1000000.0,
                    customParams
            );
            adsApplication.logRevenueAppsflyerWithCustomEvent((double) adValue.getValueMicros() / 1000000.0, adValue.getCurrencyCode());
        }
    }

    @Override
    public void logRevenueWithCustomEvent(Context context, String eventName, double revenue, String currency) {
        Map<String, Object> eventValues = new HashMap<String, Object>();
        eventValues.put(AFInAppEventParameterName.REVENUE, revenue);
        eventValues.put(AFInAppEventParameterName.CURRENCY, currency);
        AppsFlyerLib.getInstance().logEvent(context, eventName, eventValues);
    }

    @Override
    public boolean isOrganic() {
        return PreferenceManager.getInstance().getString(PreferenceManager.PREF_ADMOB_NETWORK_APPSFLYER, "").equals("organic");
    }

    @Override
    public boolean isNonOrganic() {
        return !isOrganic();
    }

    @Override
    public boolean enableAppsFlyer() {
        return enableAppsFlyer;
    }
}
