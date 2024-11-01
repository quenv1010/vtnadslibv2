package com.vtnadslibrary;

import android.util.Log;

import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.util.AdsApplication;
import com.nlbn.ads.util.AppFlyer;
import com.nlbn.ads.util.AppOpenManager;

import java.util.List;

public class MyApplication extends AdsApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AppOpenManager.getInstance().disableAppResumeWithActivity(Splash.class);

//        AppFlyer.getInstance().initAppFlyer(this, "LikYKU2zUYTct7BoQ7MuJY", true, false, true);

        AppOpenManager.getInstance().setResumeCallback(new AdCallback(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.e("TAG", "adsresume: "+"close" );
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.e("TAG", "adsresume: "+"show" );
            }
        });

    }

    @Override
    public boolean enableAdsResume() {
        return true;
    }

    @Override
    public boolean enableRemoteAdsResume() {
        return true;
    }

    @Override
    protected boolean enableAdjustTracking() {
        return true;
    }

    @Override
    protected String getAdjustToken() {
        return "tu6z93jqjym8";
    }

    @Override
    public String getKeyRemoteIntervalShowInterstitial() {
        return "";
    }

    @Override
    public void logRevenueAppsflyerWithCustomEvent(double revenue, String currency) {
        super.logRevenueAppsflyerWithCustomEvent(revenue, currency);
        AppFlyer.getInstance().logRevenueWithCustomEvent(getApplicationContext(),"", revenue, currency);
    }

    @Override
    public String getKeyRemoteAdsResume() {
        return AdsConfig.KEY_AD_APP_RESUME_ID;
    }


    @Override
    public List<String> getListTestDeviceId() {
        return null;
    }

    @Override
    public String getResumeAdId() {
        return getString(R.string.ad_app_open_resume_ad_id);
    }

    @Override
    public Boolean buildDebug() {
        return BuildConfig.DEBUG;
    }


}
