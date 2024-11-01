package com.nlbn.ads.nativeadvance;

import android.content.Context;
import android.view.ViewGroup;

import com.google.android.gms.ads.nativead.NativeAdView;
import com.nlbn.ads.banner.BaseAdView;

public class NativeAdmobPlugin {
    private BaseAdView adView;

    public NativeAdmobPlugin(Context context, NativeAdView nativeAdView, ViewGroup adContainer, ViewGroup shimmer, NativeConfig config) {
        String adUnitId = config.defaultAdUnitId;
        int refreshRateSec = config.defaultRefreshRateSec;
        adView = BaseAdView.Factory.getNativeAdView(context, adUnitId, refreshRateSec, nativeAdView, shimmer);
        adContainer.addView(adView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        adView.loadAd();
    }

    public static class NativeConfig {
        public String defaultAdUnitId;
        private int defaultRefreshRateSec = 15;

        public void setDefaultAdUnitId(String defaultAdUnitId) {
            this.defaultAdUnitId = defaultAdUnitId;
        }

        public void setDefaultRefreshRateSec(int defaultRefreshRateSec) {
            this.defaultRefreshRateSec = defaultRefreshRateSec;
        }
    }
}
