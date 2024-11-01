package com.nlbn.ads.util;

import android.content.Context;

import com.applovin.mediation.MaxAd;
import com.google.android.gms.ads.AdValue;

public abstract class Adjust {
    public static Adjust getInstance() {
        return AdjustImpl.getInstance();
    }

    public abstract void init(AdsApplication application, String appToken, Boolean isDebug);

    public abstract void init(Context context, String appToken, Boolean isDebug);

    public abstract void trackAdRevenue(AdValue adValue);

    public abstract OnAdjustAttributionChangedListener getOnAdjustAttributionChangedListener();

    public abstract void trackMaxAdRevenue(MaxAd maxAd);

    public abstract void logRevenueWithCustomEvent(String eventName, double revenue, String currency);

    protected abstract boolean isOrganic();

    protected abstract boolean isNonOrganic();
}
