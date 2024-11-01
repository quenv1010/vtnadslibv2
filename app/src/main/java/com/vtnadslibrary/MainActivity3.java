package com.vtnadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.nlbn.ads.callback.NativeCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.nlbn.ads.nativeadvance.NativeAdmobPlugin;
import com.nlbn.ads.util.Admob;

public class MainActivity3 extends AppCompatActivity {
    FrameLayout fr_ads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        fr_ads = findViewById(R.id.fr_ads);
        Admob.getInstance().loadBanner(this, getString(R.string.admod_banner_id));
//        Admob.getInstance().loadNativeAd(this, getString(R.string.admod_native_id), new NativeCallback(){
//            @Override
//            public void onNativeAdLoaded(NativeAd nativeAd) {
//                NativeAdView adView = ( NativeAdView) LayoutInflater.from(MainActivity3.this).inflate(R.layout.layout_native_custom, null);
//                fr_ads.removeAllViews();
//                fr_ads.addView(adView);
//                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
//            }
//
//            @Override
//            public void onAdFailedToLoad() {
//                fr_ads.removeAllViews();
//            }
//        });
        NativeAdmobPlugin.NativeConfig nativeConfig = new NativeAdmobPlugin.NativeConfig();
        nativeConfig.setDefaultAdUnitId(getString(R.string.ad_native_id));
        nativeConfig.setDefaultRefreshRateSec(15);
        NativeAdView adView = (NativeAdView) LayoutInflater.from(MainActivity3.this).inflate(R.layout.layout_native_custom, null);
        Admob.getInstance().loadNativeWithAutRefresh(this, adView, findViewById(R.id.fr_ads), findViewById(R.id.shimmer), nativeConfig);

    }
}