package com.nlbn.ads.applovin;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Supplier;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdRevenueListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.applovin.sdk.AppLovinCmpError;
import com.applovin.sdk.AppLovinCmpService;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.nlbn.ads.BuildConfig;
import com.nlbn.ads.R;
import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.dialog.LoadingAdsDialog;
import com.nlbn.ads.util.Adjust;

public class AppLovinImpl extends AppLovin {
    private static AppLovinImpl instance;

    private boolean openActivityAfterShowInterAds = true;

    private LoadingAdsDialog loadingAdsDialog;

    public static AppLovinImpl getInstance() {
        if (instance == null) instance = new AppLovinImpl();
        return instance;
    }

    @Override
    public void init(Context context, String adjustToken) {
        AppLovinSdk sdk = AppLovinSdk.getInstance(context);
        sdk.setMediationProvider("max");
        sdk.initializeSdk(appLovinSdkConfiguration -> {
            Adjust.getInstance().init(context, adjustToken, BuildConfig.DEBUG);
        });
    }

    @Override
    public void setOpenActivityAfterShowInterAds(boolean openActivityAfterShowInterAds) {
        this.openActivityAfterShowInterAds = openActivityAfterShowInterAds;
    }

    @Override
    public void showConsentFlow(Activity activity, OnShowConsentComplete showConsentComplete) {
        AppLovinCmpService cmpService = AppLovinSdk.getInstance(activity).getCmpService();
        if (cmpService.hasSupportedCmp()) {
            cmpService.showCmpForExistingUser(activity, new AppLovinCmpService.OnCompletedListener() {
                @Override
                public void onCompleted(@Nullable AppLovinCmpError appLovinCmpError) {
                    showConsentComplete.onShowComplete();
                }
            });
        } else {
            showConsentComplete.onShowComplete();
        }
    }

    @Override
    public boolean userHasNotConsent(Context context) {
        AppLovinSdkConfiguration configuration = AppLovinSdk.getInstance(context).getConfiguration();
        return configuration.getConsentFlowUserGeography() == AppLovinSdkConfiguration.ConsentFlowUserGeography.UNKNOWN;
    }

    @Override
    public void loadBanner(Context context, String adsId, ViewGroup container) {
        MaxAdView adView = new MaxAdView(adsId, context);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int heightPx = context.getResources().getDimensionPixelSize(R.dimen.banner_height);

        adView.setLayoutParams(new ViewGroup.LayoutParams(width, heightPx));
        adView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(@NonNull MaxAd maxAd) {

            }

            @Override
            public void onAdCollapsed(@NonNull MaxAd maxAd) {

            }

            @Override
            public void onAdLoaded(@NonNull MaxAd maxAd) {
                container.removeAllViews();
                container.addView(adView);
            }

            @Override
            public void onAdDisplayed(@NonNull MaxAd maxAd) {

            }

            @Override
            public void onAdHidden(@NonNull MaxAd maxAd) {

            }

            @Override
            public void onAdClicked(@NonNull MaxAd maxAd) {

            }

            @Override
            public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
                container.setVisibility(View.GONE);
            }

            @Override
            public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {

            }
        });
        adView.setRevenueListener(maxAd -> Adjust.getInstance().trackMaxAdRevenue(maxAd));
        adView.loadAd();
    }

    @Override
    public void loadAndShowInter(Activity activity, String adsId, AdCallback adCallback) {
        if (loadingAdsDialog != null && loadingAdsDialog.isShowing()) {
            loadingAdsDialog.dismiss();
            loadingAdsDialog = null;
        }
        loadingAdsDialog = new LoadingAdsDialog(activity);
        loadingAdsDialog.show();
        if (AppOpenManager.getInstance().isInitialize()) {
            AppOpenManager.getInstance().disableAppResume();
        }
        MaxInterstitialAd interstitialAd = new MaxInterstitialAd(adsId, activity);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(@NonNull MaxAd maxAd) {
                if (openActivityAfterShowInterAds && adCallback != null) {
//                    adCallback.onAdClosed();
//                    adCallback.onNextAction();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (loadingAdsDialog != null && loadingAdsDialog.isShowing() && !activity.isDestroyed())
                                loadingAdsDialog.dismiss();
                        }
                    }, 1500);
                }
                interstitialAd.showAd();
            }

            @Override
            public void onAdDisplayed(@NonNull MaxAd maxAd) {

            }

            @Override
            public void onAdHidden(@NonNull MaxAd maxAd) {
//                if (!openActivityAfterShowInterAds) {
                if (AppOpenManager.getInstance().isInitialize()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                adCallback.onAdClosed();
                adCallback.onNextAction();
//                }
            }

            @Override
            public void onAdClicked(@NonNull MaxAd maxAd) {

            }

            @Override
            public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
                System.out.println(maxError.getMessage());
                adCallback.onNextAction();
            }

            @Override
            public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
                adCallback.onNextAction();
            }
        });
        interstitialAd.setRevenueListener(maxAd -> Adjust.getInstance().trackMaxAdRevenue(maxAd));
        interstitialAd.loadAd();
    }

    @Override
    public void loadNativeWithDefaultTemplate(Context context, String adsId, ViewGroup container) {
        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(adsId, context);
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                container.removeAllViews();
                if (nativeAdView != null)
                    container.addView(nativeAdView);
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                container.removeAllViews();
                System.out.println(error);
            }

            @Override
            public void onNativeAdClicked(final MaxAd ad) {
            }
        });
        nativeAdLoader.setRevenueListener(maxAd -> Adjust.getInstance().trackMaxAdRevenue(maxAd));

        nativeAdLoader.loadAd();
    }

    @Override
    public void loadNativeWithDefaultTemplate(Context context, String adsId, ViewGroup container, MaxNativeAdListener nativeAdListener) {
        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(adsId, context);
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                container.removeAllViews();
                if (nativeAdView != null)
                    container.addView(nativeAdView);
                nativeAdListener.onNativeAdLoaded(nativeAdView, ad);
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                container.removeAllViews();
                System.out.println(error);
                nativeAdListener.onNativeAdLoadFailed(adUnitId, error);
            }

            @Override
            public void onNativeAdClicked(final MaxAd ad) {
                nativeAdListener.onNativeAdClicked(ad);
            }
        });
        nativeAdLoader.setRevenueListener(maxAd -> Adjust.getInstance().trackMaxAdRevenue(maxAd));

        nativeAdLoader.loadAd();
    }

    @Override
    public void loadNativeWithCustomLayout(Context context, String adsId, int layout, ViewGroup container) {
        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(layout)
            .setTitleTextViewId(R.id.title_text_view)
            .setBodyTextViewId(R.id.body_text_view)
            .setAdvertiserTextViewId(R.id.advertiser_textView)
            .setIconImageViewId(R.id.icon_image_view)
            .setMediaContentViewGroupId(R.id.media_view_container)
            .setCallToActionButtonId(R.id.cta_button)
            .build();
        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(adsId, context);
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(@Nullable MaxNativeAdView maxNativeAdView, @NonNull MaxAd maxAd) {
                super.onNativeAdLoaded(maxNativeAdView, maxAd);
                container.removeAllViews();
                if (maxNativeAdView != null) {
                    container.addView(maxNativeAdView);
                }
            }

            @Override
            public void onNativeAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
                super.onNativeAdLoadFailed(s, maxError);
                container.removeAllViews();
            }
        });
        nativeAdLoader.setRevenueListener(maxAd -> Adjust.getInstance().trackMaxAdRevenue(maxAd));
        MaxNativeAdView maxNativeAdView = new MaxNativeAdView(binder, context);
        nativeAdLoader.loadAd(maxNativeAdView);
    }

    @Override
    public void loadNativeWithCustomLayout(Context context, String adsId, int layout, ViewGroup container, MaxNativeAdListener nativeAdListener) {
        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(layout)
            .setTitleTextViewId(R.id.title_text_view)
            .setBodyTextViewId(R.id.body_text_view)
            .setAdvertiserTextViewId(R.id.advertiser_textView)
            .setIconImageViewId(R.id.icon_image_view)
            .setMediaContentViewGroupId(R.id.media_view_container)
            .setCallToActionButtonId(R.id.cta_button)
            .build();
        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(adsId, context);
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(@Nullable MaxNativeAdView maxNativeAdView, @NonNull MaxAd maxAd) {
                super.onNativeAdLoaded(maxNativeAdView, maxAd);
                container.removeAllViews();
                if (maxNativeAdView != null) {
                    container.addView(maxNativeAdView);
                }
                nativeAdListener.onNativeAdLoaded(maxNativeAdView, maxAd);
            }

            @Override
            public void onNativeAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
                super.onNativeAdLoadFailed(s, maxError);
                container.removeAllViews();
                nativeAdListener.onNativeAdLoadFailed(s, maxError);
            }
        });
        nativeAdLoader.setRevenueListener(maxAd -> Adjust.getInstance().trackMaxAdRevenue(maxAd));
        MaxNativeAdView maxNativeAdView = new MaxNativeAdView(binder, context);
        nativeAdLoader.loadAd(maxNativeAdView);
    }
}
