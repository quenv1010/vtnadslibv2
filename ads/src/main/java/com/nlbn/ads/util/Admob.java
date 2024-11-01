package com.nlbn.ads.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.nlbn.ads.banner.BannerPlugin;
import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.callback.NativeCallback;
import com.nlbn.ads.callback.RewardCallback;
import com.nlbn.ads.nativeadvance.NativeAdmobPlugin;

import java.util.List;

public abstract class Admob {

    public static Admob getInstance() {
        return AdmobImpl.getInstance();
    }

    public abstract void initAdmob(Context context, List<String> testDeviceList);

    public abstract void initAdmob(Context context);

    public abstract void setFan(boolean fan);

    public abstract void setIntervalShowInterstitial(int intervalShowInterstitial);

    public abstract Boolean isShowingInterstitial();

    public abstract void setDisableAdResumeWhenClickAds(boolean disableAdResumeWhenClickAds);

    public abstract void setOpenShowAllAds(boolean isShowAllAds);

    public abstract void setOpenEventLoadTimeLoadAdsSplash(boolean logTimeLoadAdsSplash);

    public abstract void setOpenEventLoadTimeShowAdsInter(boolean logLogTimeShowAds);

    public abstract void loadBanner(final Activity mActivity, String id);

    public abstract void loadBannerPlugin(Activity activity, ViewGroup layout, ViewGroup shimmer, BannerPlugin.Config config);

    public abstract void loadBanner(final Activity mActivity, String id, AdCallback callback);

    public abstract void loadBanner(final Activity mActivity, String id, Boolean useInlineAdaptive);

    public abstract void loadBanner(final Activity mActivity, String id, ViewGroup view);

    public abstract void loadInlineBanner(final Activity activity, String id, String inlineStyle);

    public abstract void loadBanner(final Activity mActivity, String id, final AdCallback callback, Boolean useInlineAdaptive);

    public abstract void loadInlineBanner(final Activity activity, String id, String inlineStyle, final AdCallback callback);

    public abstract void loadCollapsibleBanner(final Activity mActivity, String id, String gravity);

    public abstract void loadCollapsibleBanner(final Activity mActivity, String id, String gravity, ViewGroup view);

    public abstract void loadBannerFragment(final Activity mActivity, String id, final View rootView);

    public abstract void loadBannerFragment(final Activity mActivity, String id, final View rootView, final AdCallback callback);

    public abstract void loadBannerFragment(final Activity mActivity, String id, final View rootView, Boolean useInlineAdaptive);

    public abstract void loadInlineBannerFragment(final Activity activity, String id, final View rootView, String inlineStyle);

    public abstract void loadBannerFragment(final Activity mActivity, String id, final View rootView, final AdCallback callback, Boolean useInlineAdaptive);

    public abstract void loadInlineBannerFragment(final Activity activity, String id, final View rootView, String inlineStyle, final AdCallback callback);

    public abstract void loadCollapsibleBannerFragment(final Activity mActivity, String id, final View rootView, String gravity);

    public abstract boolean interstitialSplashLoaded();

    public abstract InterstitialAd getmInterstitialSplash();

    public abstract void loadSplashInterAds(final Context context, String id, long timeOut, long timeDelay, final AdCallback adListener);

    public abstract void loadSplashInterAds2(final Context context, String id, long timeDelay, final AdCallback adListener);

    public abstract void loadSplashInterAdsFloor(final Context context, List<String> listID, long timeDelay, final AdCallback adListener);

    public abstract void dismissLoadingDialog();

    public abstract void loadInterAds(Context context, String id, AdCallback adCallback);

    public abstract void showInterAds(Context context, InterstitialAd mInterstitialAd, final AdCallback callback);

    public abstract void loadAndShowInter(Activity activity, String idInter, int timeDelay, int timeOut, AdCallback callback);

    public abstract void loadAndShowInter(Activity activity, String idInter, boolean isShowFirstTime, AdCallback callback);

    public abstract void initRewardAds(Context context, String id);

    public abstract void showRewardAds(final Activity context, final RewardCallback adCallback);

    public abstract RewardedAd getRewardedAd();

    public abstract void loadNativeAd(Context context, String id, final NativeCallback callback);

    public abstract void loadNativeWithAutRefresh(Context context, NativeAdView nativeAdView, ViewGroup adContainer, ViewGroup shimmer, NativeAdmobPlugin.NativeConfig config);

    public abstract void loadNativeAd(Context context, String id, final NativeCallback callback, int adChoicesPlacement);

    public abstract void loadAndShowNativeWithCheckingNetworkType(Context context, String id, ViewGroup layoutContainer, NativeAdView normalView, NativeAdView customView, final NativeCallback callback);

    public abstract void loadNativeAdFullScreen(Context context, String id, int mediaAspectRatio, final NativeCallback callback);

    public abstract void loadNativeAdFloor(Context context, List<String> listID, final NativeCallback callback);

    public abstract void pushAdsToViewCustom(NativeAd nativeAd, NativeAdView adView);

    public abstract void loadNativeFragment(final Activity mActivity, String id, View parent);

    public abstract void setOpenActivityAfterShowInterAds(boolean openActivityAfterShowInterAds);

    public abstract String getDeviceId(Activity activity);

    public abstract void onCheckShowSplashWhenFail(final Activity activity, final AdCallback callback, int timeDelay);

    public abstract void onCheckShowSplashWhenFailClickButton(final AppCompatActivity activity, InterstitialAd interstitialAd, final AdCallback callback, int timeDelay);

    public abstract boolean isLoadFullAds();

}
