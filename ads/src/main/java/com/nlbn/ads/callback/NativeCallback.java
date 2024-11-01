package com.nlbn.ads.callback;

import com.google.android.gms.ads.nativead.NativeAd;

public abstract class NativeCallback {
    public void onNativeAdLoaded(NativeAd nativeAd){};
    public void onAdFailedToLoad(){};
    public void onEarnRevenue(Double Revenue){}
    public void onAdImpression(){};
}
