package com.nlbn.ads.nativeadvance;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.nlbn.ads.banner.BaseAdView;
import com.nlbn.ads.callback.NativeCallback;
import com.nlbn.ads.util.Admob;

public class NativeAdmobView extends BaseAdView {
    String adUnitId;
    NativeAdView nativeAdView;
    Context context;
    ViewGroup shimmer, adContainer;


    public NativeAdmobView(Context context, String adUnitId, int refreshRateSec, NativeAdView nativeAdView, ViewGroup shimmer) {
        super(context, refreshRateSec, shimmer);
        this.adUnitId = adUnitId;
        this.nativeAdView = nativeAdView;
        this.context = context;
        this.shimmer = shimmer;
    }

    @Override
    protected void loadAdInternal(Runnable onDone) {
        Admob.getInstance().loadNativeAd(context, adUnitId, new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                shimmer.removeAllViews();
                shimmer.setVisibility(View.GONE);
                NativeAdmobView.this.removeAllViews();
                NativeAdmobView.this.addView(nativeAdView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, nativeAdView);
                onDone.run();
            }
        });
    }
}
