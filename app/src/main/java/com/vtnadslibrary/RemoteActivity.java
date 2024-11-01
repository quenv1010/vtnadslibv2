package com.vtnadslibrary;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.nlbn.ads.adstype.AdBannerType;
import com.nlbn.ads.adstype.AdNativeType;
import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.callback.NativeCallback;
import com.nlbn.ads.callback.RewardCallback;
import com.nlbn.ads.config.AdBannerConfig;
import com.nlbn.ads.config.AdInterConfig;
import com.nlbn.ads.config.AdNativeConfig;
import com.nlbn.ads.config.AdRewardConfig;
import com.nlbn.ads.util.Admob;
import com.nlbn.ads.util.BannerGravity;
import com.nlbn.ads.util.RemoteAdmob;

public class RemoteActivity extends AppCompatActivity {
    boolean isBannerCollapse = false;
    boolean isNativeFloor = false;

    InterstitialAd mInterstitialAd;
    AdRewardConfig adRewardConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        checkBanner();
        checkNative();
        loadInter();

         adRewardConfig = new AdRewardConfig.Builder()
                .setKey(AdsConfig.KEY_AD_APP_REWARD_ID)
                .setRewardCallback(new RewardCallback() {
                    @Override
                    public void onEarnedReward(RewardItem rewardItem) {
                        Toast.makeText(RemoteActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdClosed() {
                        Toast.makeText(RemoteActivity.this, "Close ads", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdFailedToShow(int codeError) {
                        Toast.makeText(RemoteActivity.this, "Loa ads err", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
        RemoteAdmob.getInstance().initRewardWithConfig(this, adRewardConfig);

        findViewById(R.id.tv_check_banner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBannerCollapse = !isBannerCollapse;
                checkBanner();
            }
        });

        findViewById(R.id.tv_check_native).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNativeFloor = !isNativeFloor;
                checkNative();
            }
        });

        findViewById(R.id.tv_check_inter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInter();
            }
        });
        findViewById(R.id.tv_check_reward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReward();
            }
        });

    }

    public void checkBanner(){
        if (isBannerCollapse){
            loadBannerCollapseTop();
        }else {
            loadBanner();
        }
    }

    public void loadBanner(){
        AdBannerConfig adBannerConfig = new AdBannerConfig.Builder()
                .setKey(AdsConfig.key_ad_banner_id)
                .setBannerType(AdBannerType.BANNER)
                .setView(findViewById(R.id.banner))
                .setGravity(BannerGravity.top)
                .build();
        RemoteAdmob.getInstance().loadBannerWithConfig(this,adBannerConfig);
    }

    public void loadBannerCollapseTop(){
        AdBannerConfig adBannerConfig = new AdBannerConfig.Builder()
                .setKey(AdsConfig.key_ad_banner_id_collapse)
                .setBannerType(AdBannerType.BANNER_COLLAPSE)
                .setGravity(BannerGravity.top)
                .setView(findViewById(R.id.banner))
                .build();
        RemoteAdmob.getInstance().loadBannerWithConfig(this,adBannerConfig);
    }

    public void checkNative(){
        if (isNativeFloor){
            loadNativeFloor();
        }else {
            loadNative();
        }
    }


    public void loadNative(){
        AdNativeConfig adNativeConfig = new AdNativeConfig.Builder()
                .setKey(AdsConfig.key_ad_native_id)
                .setNativeType(AdNativeType.NATIVE)
                .setLayout(R.layout.layout_native_language)
                .setView(findViewById(R.id.native_ads))
                .build();
       // RemoteAdmob.getInstance().loadNativeWithConfig(this,adNativeConfig,false);

        RemoteAdmob.getInstance().loadNativeWithConfigCallback(this, adNativeConfig, false, new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(RemoteActivity.this).inflate(adNativeConfig.layout, null);
                adNativeConfig.view.removeAllViews();
                adNativeConfig.view.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
            }
        });
    }

    public void loadNativeFloor(){
        Log.e("TAG", "loadNativeFloor: ");
        AdNativeConfig adNativeConfig = new AdNativeConfig.Builder()
                .setKey(AdsConfig.key_ad_native_id_1,AdsConfig.KEY_AD_NATIVE_ID_2)
                .setNativeType(AdNativeType.NATIVE_FLOOR)
                .setLayout(R.layout.layout_native_language)
                .setView(findViewById(R.id.native_ads))
                .build();
        RemoteAdmob.getInstance().loadNativeWithConfig(this,adNativeConfig,false);
    }

    public void loadInter(){
        RemoteAdmob.getInstance().loadInterWithKey(this,AdsConfig.key_ad_interstitial_id,new AdCallback(){
            @Override
            public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                mInterstitialAd =interstitialAd;
            }
        });
    }

    public void showInter(){
        AdInterConfig adInterConfig = new AdInterConfig.Builder()
                .setKey(AdsConfig.key_ad_interstitial_id)
                .setInterstitialAd(mInterstitialAd)
                .setCallback(new AdCallback(){
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        Intent intent = new Intent(RemoteActivity.this, RemoteActivity2.class);
                        startActivity(intent);
                        loadInter();
                    }
                })
                .build();
        RemoteAdmob.getInstance().showInterWithConfig(this,adInterConfig);
    }

    public void showReward(){
        RemoteAdmob.getInstance().showRewardWithConfig(this,adRewardConfig);
    }
}