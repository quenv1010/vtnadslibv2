package com.vtnadslibrary;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

import com.nlbn.ads.adstype.AdSplashType;
import com.nlbn.ads.billing.AppPurchase;
import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.callback.BillingListener;
import com.nlbn.ads.config.AdSplashConfig;
import com.nlbn.ads.util.Admob;
import com.nlbn.ads.util.RemoteAdmob;

import java.util.ArrayList;
import java.util.List;

public class Splash extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    public static String PRODUCT_ID_MONTH = "android.test.purchased";
    public AdCallback adCallback;
    public AdSplashConfig adSplashConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        //Admob.getInstance().setOpenShowAllAds(true);
        //Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        //Admob.getInstance().setOpenEventLoadTimeLoadAdsSplash(true);
        // Admob.getInstance().setOpenEventLoadTimeShowAdsInter(true);
        // Admob.getInstance().setOpenActivityAfterShowInterAds(false);

        adCallback = new AdCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                startActivity(new Intent(Splash.this, MainActivity.class));
                finish();
            }
        };
        System.out.println("SONTT" + Admob.getInstance().isLoadFullAds());


        // Admob
        AppPurchase.getInstance().setBillingListener(new BillingListener() {
            @Override
            public void onInitBillingListener(int code) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShowSplashInter();
                        //ShowSplashInterFloor();
                        //ShowSplashOpen();
                        //ShowSplashOpenFloor();
                    }
                });
            }
        }, 5000);

        initBilling();
    }

    @Override
    protected void onStop() {
        super.onStop();
        RemoteAdmob.getInstance().dismissLoadingDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RemoteAdmob.getInstance().dismissLoadingDialog();
    }

    private void initBilling() {
        List<String> listINAPId = new ArrayList<>();
        listINAPId.add(PRODUCT_ID_MONTH);
        List<String> listSubsId = new ArrayList<>();
        AppPurchase.getInstance().initBilling(getApplication(), listINAPId, listSubsId);

    }

    @Override
    protected void onResume() {
        super.onResume();
        RemoteAdmob.getInstance().onCheckShowSplashWhenFailWithConfig(this, adSplashConfig, 1000);
    }

    public void ShowSplashInter() {
        adSplashConfig = new AdSplashConfig.Builder()
                .setKey(AdsConfig.key_ad_interstitial_id)
                .setAdSplashType(AdSplashType.SPLASH_INTER)
                .setTimeDelay(3000)
                .setCallback(adCallback)
                .build();
        Admob.getInstance().loadSplashInterAds2(this, getString(R.string.ad_interstitial_id), 1000, adCallback);

    }

    public void ShowSplashInterFloor() {
        adSplashConfig = new AdSplashConfig.Builder()
                .setKey(AdsConfig.key_ad_splash_floor_id)
                .setAdSplashType(AdSplashType.SPLASH_INTER_FLOOR)
                .setTimeDelay(3000)
                .setCallback(adCallback)
                .build();
        RemoteAdmob.getInstance().loadAdSplashWithConfig(this, adSplashConfig);

    }

    public void ShowSplashOpen() {
        adSplashConfig = new AdSplashConfig.Builder()
                .setKey(AdsConfig.KEY_AD_APP_OPEN_AD_ID)
                .setAdSplashType(AdSplashType.SPLASH_OPEN)
                .setTimeOut(15000)
                .setTimeDelay(3000)
                .setShowAdIfReady(true)
                .setCallback(adCallback)
                .build();
        RemoteAdmob.getInstance().loadAdSplashWithConfig(this, adSplashConfig);

    }

    public void ShowSplashOpenFloor() {
        adSplashConfig = new AdSplashConfig.Builder()
                .setKey(AdsConfig.key_ad_open_floor_id)
                .setAdSplashType(AdSplashType.SPLASH_OPEN_FLOOR)
                .setShowAdIfReady(true)
                .setCallback(adCallback)
                .build();
        RemoteAdmob.getInstance().loadAdSplashWithConfig(this, adSplashConfig);

    }
}