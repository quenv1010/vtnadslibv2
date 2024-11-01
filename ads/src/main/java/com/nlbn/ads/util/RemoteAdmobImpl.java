package com.nlbn.ads.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.nlbn.ads.adstype.AdSplashType;
import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.callback.NativeCallback;
import com.nlbn.ads.config.AdBannerConfig;
import com.nlbn.ads.config.AdInterConfig;
import com.nlbn.ads.config.AdNativeConfig;
import com.nlbn.ads.config.AdRewardConfig;
import com.nlbn.ads.config.AdSplashConfig;

import java.util.ArrayList;
import java.util.List;

class RemoteAdmobImpl extends RemoteAdmob {

    public String TAG = "AdmobVTN";

    public static RemoteAdmobImpl INSTANCE;

    public static RemoteAdmobImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RemoteAdmobImpl();
        }
        return INSTANCE;
    }



    @Override
    public String getIdAdsWithKey(String key) {
        return FirebaseRemoteConfig.getInstance().getString(key);
    }

    @Override
    public List<String> getListIdAdsWithKey(String... keys) {
        List<String> listID = new ArrayList<>();
        for (String key : keys){
            listID.add(FirebaseRemoteConfig.getInstance().getString(key));
        }
        return listID;
    }



    //splash
    @Override
    public void onCheckShowSplashWhenFailWithConfig(final Activity activity, AdSplashConfig config, int timeDelay) {
        if (config != null) {
            if (config.adSplashType == AdSplashType.SPLASH_INTER || config.adSplashType == AdSplashType.SPLASH_INTER_FLOOR) {
                Admob.getInstance().onCheckShowSplashWhenFail(activity, config.callback, timeDelay);
            } else {
                AppOpenManager.getInstance().onCheckShowSplashWhenFail(activity, config.callback, timeDelay);
            }
        }
    }

    @Override
    public void loadAdSplashWithConfig(final Context context, AdSplashConfig config) {
        if (Helper.haveNetworkConnection(context)) {
            switch (config.adSplashType) {
                case SPLASH_INTER: {
                    Admob.getInstance().loadSplashInterAds2(context, getIdAdsWithKey(config.key), config.timeDelay, config.callback);
                    break;
                }
                case SPLASH_OPEN: {
                    AppOpenManagerImpl.getInstance().loadOpenAppAdSplash(context, getIdAdsWithKey(config.key), config.timeDelay, config.timeOut, config.isShowAdIfReady, config.callback);
                    break;
                }
                case SPLASH_INTER_FLOOR: {
                    Admob.getInstance().loadSplashInterAdsFloor(context, getListIdAdsWithKey(config.key), config.timeDelay, config.callback);
                    break;
                }
                case SPLASH_OPEN_FLOOR: {
                    AppOpenManagerImpl.getInstance().loadOpenAppAdSplashFloor(context, getListIdAdsWithKey(config.key), config.isShowAdIfReady, config.callback);
                    break;
                }
                default:
                    if (config.callback != null) {
                        config.callback.onAdClosed();
                        config.callback.onNextAction();
                    }
                    break;
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (config.callback != null) {
                        config.callback.onAdClosed();
                        config.callback.onNextAction();
                    }
                }
            }, config.timeDelay);
        }


    }

    @Override
    public void dismissLoadingDialog() {
        Admob.getInstance().dismissLoadingDialog();
    }

    //banner
    @Override
    public void loadBannerWithConfig(Activity activity, AdBannerConfig adBannerConfig) {
        if (Helper.haveNetworkConnection(activity)) {
            switch (adBannerConfig.bannerType) {
                case BANNER: {
                    Admob.getInstance().loadBanner(activity, getIdAdsWithKey(adBannerConfig.key), adBannerConfig.view);
                    break;
                }
                case BANNER_COLLAPSE: {
                    Admob.getInstance().loadCollapsibleBanner(activity, getIdAdsWithKey(adBannerConfig.key), adBannerConfig.gravity, adBannerConfig.view);
                    break;
                }
            }
        } else {
            if (adBannerConfig.view != null) {
                adBannerConfig.view.removeAllViews();
            }
        }
    }

    //native
    @Override
    public void loadNativeWithConfig(Context context, AdNativeConfig adNativeConfig, boolean isInvisible) {
        if (Helper.haveNetworkConnection(context)) {
            switch (adNativeConfig.adNativeType) {
                case NATIVE: {
                    Admob.getInstance().loadNativeAd(context, getIdAdsWithKey(adNativeConfig.key[0]), new NativeCallback() {
                        @Override
                        public void onNativeAdLoaded(NativeAd nativeAd) {
                            if (nativeAd != null) {
                                if (adNativeConfig.view != null) {
                                    NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(adNativeConfig.layout, null);
                                    adNativeConfig.view.removeAllViews();
                                    adNativeConfig.view.addView(adView);
                                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);


                                }
                            } else {
                                if (adNativeConfig.view != null) {
                                    if (isInvisible) {
                                        adNativeConfig.view.setVisibility(View.INVISIBLE);
                                    } else {
                                        adNativeConfig.view.removeAllViews();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onAdFailedToLoad() {
                            if (adNativeConfig.view != null) {
                                if (isInvisible) {
                                    adNativeConfig.view.setVisibility(View.INVISIBLE);
                                } else {
                                    adNativeConfig.view.removeAllViews();
                                }
                            }
                        }
                    });
                    break;
                }
                case NATIVE_FLOOR: {
                    Admob.getInstance().loadNativeAdFloor(context, getListIdAdsWithKey(adNativeConfig.key), new NativeCallback() {
                        @Override
                        public void onNativeAdLoaded(NativeAd nativeAd) {
                            if (nativeAd != null) {
                                if (adNativeConfig.view != null) {
                                    NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(adNativeConfig.layout, null);
                                    adNativeConfig.view.removeAllViews();
                                    adNativeConfig.view.addView(adView);
                                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);

                                }
                            } else {
                                if (adNativeConfig.view != null) {
                                    if (isInvisible) {
                                        adNativeConfig.view.setVisibility(View.INVISIBLE);
                                    } else {
                                        adNativeConfig.view.removeAllViews();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onAdFailedToLoad() {
                            if (adNativeConfig.view != null) {
                                if (isInvisible) {
                                    adNativeConfig.view.setVisibility(View.INVISIBLE);
                                } else {
                                    adNativeConfig.view.removeAllViews();
                                }
                            }
                        }
                    });
                    break;
                }
            }
        } else {
            if (adNativeConfig.view != null) {
                if (isInvisible) {
                    adNativeConfig.view.setVisibility(View.INVISIBLE);
                } else {
                    adNativeConfig.view.removeAllViews();
                }
            }
        }
    }

    @Override
    public void loadNativeWithConfigCallback(Context context, AdNativeConfig adNativeConfig, boolean isInvisible,NativeCallback nativeCallback) {
        if (Helper.haveNetworkConnection(context)) {
            switch (adNativeConfig.adNativeType) {
                case NATIVE: {
                    Admob.getInstance().loadNativeAd(context, getIdAdsWithKey(adNativeConfig.key[0]), nativeCallback);
                    break;
                }
                case NATIVE_FLOOR: {
                    Admob.getInstance().loadNativeAdFloor(context, getListIdAdsWithKey(adNativeConfig.key), nativeCallback);
                    break;
                }
            }
        } else {
            if (adNativeConfig.view != null) {
                if (isInvisible) {
                    adNativeConfig.view.setVisibility(View.INVISIBLE);
                } else {
                    adNativeConfig.view.removeAllViews();
                }
            }
        }
    }

    //Inter
    @Override
    public void loadInterWithKey(Context context, String key, AdCallback adCallback) {
        if (Helper.haveNetworkConnection(context)) {
            Admob.getInstance().loadInterAds(context, getIdAdsWithKey(key), adCallback);
        } else {
            adCallback.onInterstitialLoadFaild();
        }
    }

    @Override
    public void showInterWithConfig(Context context, AdInterConfig adInterConfig) {
        if (Helper.haveNetworkConnection(context)) {
            if (adInterConfig.callback != null) {
                if (adInterConfig.mInterstitialAd!=null){
                    Admob.getInstance().showInterAds(context, adInterConfig.mInterstitialAd, adInterConfig.callback);
                }else {
                    adInterConfig.callback.onNextAction();
                    adInterConfig.callback.onAdClosed();
                }

            }
        } else {
            if (adInterConfig.callback != null) {
                adInterConfig.callback.onNextAction();
                adInterConfig.callback.onAdClosed();
            }
        }
    }

    //reward
    @Override
    public void initRewardWithConfig(Context context, AdRewardConfig adRewardConfig) {
        if (Helper.haveNetworkConnection(context)) {
            Admob.getInstance().initRewardAds(context, getIdAdsWithKey(adRewardConfig.key));
        }
    }

    @Override
    public void showRewardWithConfig(Activity context, AdRewardConfig adRewardConfig) {
        if (Helper.haveNetworkConnection(context)) {
            Admob.getInstance().showRewardAds(context, adRewardConfig.rewardCallback);
        } else {
            if (adRewardConfig.rewardCallback != null) {
                adRewardConfig.rewardCallback.onAdClosed();
            }
        }
    }

    //resume
    @Override
    public void disableAppResume() {
        AppOpenManagerImpl.getInstance().disableAppResume();
    }

    @Override
    public void enableAppResume() {
        AppOpenManagerImpl.getInstance().enableAppResume();
    }

    @Override
    public void disableAppResumeWithActivity(Class activityClass) {
        AppOpenManagerImpl.getInstance().disableAppResumeWithActivity(activityClass);
    }

    @Override
    public void enableAppResumeWithActivity(Class activityClass) {
        AppOpenManagerImpl.getInstance().enableAppResumeWithActivity(activityClass);
    }


}
