package com.nlbn.ads.applovin;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import androidx.core.util.Supplier;

import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.nlbn.ads.callback.AdCallback;

public abstract class AppLovin {
    public static AppLovin getInstance() {
        return AppLovinImpl.getInstance();
    }

    public abstract void init(Context context, String adjustToken);

    public abstract void setOpenActivityAfterShowInterAds(boolean openActivityAfterShowInterAds);

    public abstract void loadBanner(Context context, String adsId, ViewGroup container);

    public abstract void loadAndShowInter(Activity activity, String adsId, AdCallback adCallback);

    public abstract void loadNativeWithDefaultTemplate(Context context, String adsId, ViewGroup container);

    public abstract void loadNativeWithDefaultTemplate(Context context, String adsId, ViewGroup container, MaxNativeAdListener nativeAdListener);

    public abstract void loadNativeWithCustomLayout(Context context, String adsId, int layout, ViewGroup container);

    public abstract void loadNativeWithCustomLayout(Context context, String adsId, int layout, ViewGroup container, MaxNativeAdListener nativeAdListener);

    public abstract void showConsentFlow(Activity activity, OnShowConsentComplete showConsentComplete);

    public abstract boolean userHasNotConsent(Context context);
}
