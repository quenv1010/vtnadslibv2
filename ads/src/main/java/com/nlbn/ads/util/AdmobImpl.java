package com.nlbn.ads.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.nlbn.ads.BuildConfig;
import com.nlbn.ads.R;
import com.nlbn.ads.banner.BannerPlugin;
import com.nlbn.ads.billing.AppPurchase;
import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.callback.NativeCallback;
import com.nlbn.ads.callback.RewardCallback;
import com.nlbn.ads.dialog.LoadingAdsDialog;
import com.nlbn.ads.nativeadvance.NativeAdmobPlugin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

class AdmobImpl extends Admob {
    private static AdmobImpl INSTANCE;
    private static final String TAG = "Admob";
    private LoadingAdsDialog dialog;
    private int currentClicked = 0;
    private int numShowAds = 3;
    private int maxClickAds = 100;
    private Handler handlerTimeout;
    private Runnable rdTimeout;
    private boolean isTimeLimited;
    private boolean isShowLoadingSplash = false; //kiểm tra trạng thái ad splash, ko cho load, show khi đang show loading ads splash
    boolean checkTimeDelay = false;
    private boolean openActivityAfterShowInterAds = true;
    private Context context;
    boolean isTimeDelay = false; //xử lý delay time show ads, = true mới show ads
    private boolean isTimeout; // xử lý timeout show ads
    private long lastTimeShowAds;
    private int intervalShowInterstitial = 20;

    private RewardedAd rewardedAd;
    private String rewardedId;
    InterstitialAd mInterstitialSplash;
    InterstitialAd interstitialAd;
    private boolean disableAdResumeWhenClickAds = false;
    public static final String BANNER_INLINE_SMALL_STYLE = "BANNER_INLINE_SMALL_STYLE";
    public static final String BANNER_INLINE_LARGE_STYLE = "BANNER_INLINE_LARGE_STYLE";
    private static int MAX_SMALL_INLINE_BANNER_HEIGHT = 50;

    public static long timeLimitAds = 0; // Set > 1000 nếu cần limit ads click
    private boolean isShowInter = true;
    private boolean isShowBanner = true;
    private boolean isShowNative = true;
    private boolean logTimeLoadAdsSplash = false;
    private boolean logLogTimeShowAds = false;
    public static boolean isShowAllAds = true;
    private boolean isFan = false;
    private long currentTime;
    private long currentTimeShowAds;

    public static AdmobImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdmobImpl();
        }
        return INSTANCE;
    }

    @Override
    public void initAdmob(Context context, List<String> testDeviceList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
        MobileAds.initialize(context, initializationStatus -> {
        });
        MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().setTestDeviceIds(testDeviceList).build());

        this.context = context;
    }

    @Override
    public void initAdmob(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }

        MobileAds.initialize(context, initializationStatus -> {
        });
        if (BuildConfig.DEBUG) {
            MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList(getDeviceId((Activity) context))).build());
        }

        this.context = context;
    }

    @Override
    public Boolean isShowingInterstitial() {
        return !isShowInter;
    }

    @Override
    public void setFan(boolean fan) {
        isFan = fan;
    }

    @Override
    public void setIntervalShowInterstitial(int intervalShowInterstitial) {
        this.intervalShowInterstitial = intervalShowInterstitial;
    }

    @Override
    public void setDisableAdResumeWhenClickAds(boolean disableAdResumeWhenClickAds) {
        this.disableAdResumeWhenClickAds = disableAdResumeWhenClickAds;
    }

    @Override
    public void setOpenShowAllAds(boolean isShowAllAds) {
        this.isShowAllAds = isShowAllAds;
    }

    @Override
    public void setOpenEventLoadTimeLoadAdsSplash(boolean logTimeLoadAdsSplash) {
        this.logTimeLoadAdsSplash = logTimeLoadAdsSplash;
    }

    @Override
    public void setOpenEventLoadTimeShowAdsInter(boolean logLogTimeShowAds) {
        this.logLogTimeShowAds = logLogTimeShowAds;
    }

    /*=================================Banner ======================================*/
    @Override
    public void loadBanner(Activity mActivity, String id) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    @Override
    public void loadBannerPlugin(Activity activity, ViewGroup layout, ViewGroup shimmer, BannerPlugin.Config config) {
        new BannerPlugin(activity, layout, shimmer, config);
    }

    @Override
    public void loadBanner(Activity mActivity, String id, AdCallback callback) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, callback, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    @Override
    public void loadBanner(Activity mActivity, String id, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, null, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
        }
    }

    @Override
    public void loadInlineBanner(Activity activity, String id, String inlineStyle) {
        final FrameLayout adContainer = activity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = activity.findViewById(R.id.shimmer_container_banner);
        loadBanner(activity, id, adContainer, containerShimmer, null, true, inlineStyle);
    }

    @Override
    public void loadBanner(Activity mActivity, String id, AdCallback callback, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, callback, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
        }
    }

    @Override
    public void loadBanner(final Activity mActivity, String id, ViewGroup view) {
        final FrameLayout adContainer = view.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = view.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    @Override
    public void loadInlineBanner(Activity activity, String id, String inlineStyle, AdCallback callback) {
        final FrameLayout adContainer = activity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = activity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(activity, id, adContainer, containerShimmer, callback, true, inlineStyle);
        }
    }

    @Override
    public void loadCollapsibleBanner(Activity mActivity, String id, String gravity) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadCollapsibleBanner(mActivity, id, gravity, adContainer, containerShimmer);
        }
    }

    @Override
    public void loadCollapsibleBanner(final Activity mActivity, String id, String gravity, ViewGroup view) {
        final FrameLayout adContainer = view.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = view.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadCollapsibleBanner(mActivity, id, gravity, adContainer, containerShimmer);
        }
    }

    @Override
    public void loadBannerFragment(Activity mActivity, String id, View rootView) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    @Override
    public void loadBannerFragment(Activity mActivity, String id, View rootView, AdCallback callback) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, callback, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    @Override
    public void loadBannerFragment(Activity mActivity, String id, View rootView, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, null, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
        }
    }

    @Override
    public void loadInlineBannerFragment(Activity activity, String id, View rootView, String inlineStyle) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(activity, id, adContainer, containerShimmer, null, true, inlineStyle);
        }
    }

    @Override
    public void loadBannerFragment(Activity mActivity, String id, View rootView, AdCallback callback, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, callback, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
        }
    }

    @Override
    public void loadInlineBannerFragment(Activity activity, String id, View rootView, String inlineStyle, AdCallback callback) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(activity, id, adContainer, containerShimmer, callback, true, inlineStyle);
        }
    }

    @Override
    public void loadCollapsibleBannerFragment(Activity mActivity, String id, View rootView, String gravity) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadCollapsibleBanner(mActivity, id, gravity, adContainer, containerShimmer);
    }

    @Override
    public boolean interstitialSplashLoaded() {
        return mInterstitialSplash != null;
    }

    @Override
    public InterstitialAd getmInterstitialSplash() {
        return mInterstitialSplash;
    }

    @Override
    public void loadSplashInterAds(Context context, String id, long timeOut, long timeDelay, AdCallback adListener) {
        isTimeDelay = false;
        isTimeout = false;
        if (!isNetworkConnected()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (adListener != null) {
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    }
                    return;
                }
            }, 3000);
        } else {
            if (logTimeLoadAdsSplash) {
                currentTime = System.currentTimeMillis();
            }
            if (AppPurchase.getInstance().isPurchased(context) || !isShowAllAds) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (adListener != null) {
                            adListener.onAdClosed();
                            adListener.onNextAction();
                        }
                        return;
                    }
                }, 3000);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //check delay show ad splash
                        if (mInterstitialSplash != null) {
                            Log.d(TAG, "loadSplashInterAds:show ad on delay ");
                            onShowSplash((Activity) context, adListener);
                            return;
                        }
                        Log.d(TAG, "loadSplashInterAds: delay validate");
                        isTimeDelay = true;
                    }
                }, timeDelay);
                if (timeOut > 0) {
                    handlerTimeout = new Handler();
                    rdTimeout = new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "loadSplashInterstitalAds: on timeout");
                            isTimeout = true;
                            if (mInterstitialSplash != null) {
                                Log.i(TAG, "loadSplashInterstitalAds:show ad on timeout ");
                                onShowSplash((Activity) context, adListener);
                                return;
                            }
                            if (adListener != null) {
                                adListener.onAdClosed();
                                adListener.onNextAction();
                                isShowLoadingSplash = false;
                            }
                        }
                    };
                    handlerTimeout.postDelayed(rdTimeout, timeOut);
                }

                isShowLoadingSplash = true;
                loadInterAds(context, id, new AdCallback() {
                    @Override
                    public void onInterstitialLoad(InterstitialAd interstitialAd) {
                        super.onInterstitialLoad(interstitialAd);
                        Log.e(TAG, "loadSplashInterstitalAds  end time loading success:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
                        if (isTimeout)
                            return;
                        if (interstitialAd != null) {
                            mInterstitialSplash = interstitialAd;
                            if (isTimeDelay) {
                                onShowSplash((Activity) context, adListener);
                                Log.i(TAG, "loadSplashInterstitalAds:show ad on loaded ");
                            }
                        }
                        if (interstitialAd != null) {
                            interstitialAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "OnPaidEvent loadInterstitialAds:" + adValue.getValueMicros());
                                FirebaseUtil.logPaidAdImpression(context,
                                        adValue,
                                        interstitialAd.getAdUnitId(), AdType.BANNER);
                            });
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        Log.e(TAG, "loadSplashInterstitalAds  end time loading error:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
                        if (isTimeout)
                            return;
                        if (adListener != null) {
                            if (handlerTimeout != null && rdTimeout != null) {
                                handlerTimeout.removeCallbacks(rdTimeout);
                            }
                            if (i != null)
                                Log.e(TAG, "loadSplashInterstitalAds: load fail " + i.getMessage());
                            adListener.onAdFailedToLoad(i);
                            adListener.onNextAction();
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        super.onAdClicked();
                        if (timeLimitAds > 1000)
                            setTimeLimitInter();
                    }
                });
            }
        }
    }

    @Override
    public void loadSplashInterAds2(Context context, String id, long timeDelay, AdCallback adListener) {
        if (!isNetworkConnected() || AppPurchase.getInstance().isPurchased(context) || !isShowAllAds) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (adListener != null) {
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    }
                    return;
                }
            }, 3000);
        } else {
            mInterstitialSplash = null;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    InterstitialAd.load(context, id, getAdRequest(),
                            new InterstitialAdLoadCallback() {
                                @Override
                                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                    super.onAdLoaded(interstitialAd);
                                    mInterstitialSplash = interstitialAd;
                                    AppOpenManager.getInstance().disableAppResume();
                                    isShowInter = false;
                                    onShowSplash((Activity) context, adListener);
                                }

                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                    super.onAdFailedToLoad(loadAdError);
                                    mInterstitialSplash = null;
                                    adListener.onAdFailedToLoad(loadAdError);
                                    adListener.onNextAction();
                                }
                            });
                }
            }, timeDelay);
        }
    }

    @Override
    public void loadSplashInterAdsFloor(Context context, List<String> listID, long timeDelay, AdCallback adListener) {
        if (!isNetworkConnected() || AppPurchase.getInstance().isPurchased(context) || !isShowAllAds) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (adListener != null) {
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    }
                    return;
                }
            }, 3000);
        } else {
            mInterstitialSplash = null;
            if (listID == null) {
                adListener.onAdClosed();
                adListener.onNextAction();
                return;
            }

            if (listID.size() < 1) {
                adListener.onAdClosed();
                adListener.onNextAction();

            } else {

                Log.e("Splash", "load ID :" + listID.get(0));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InterstitialAd.load(context, listID.get(0), getAdRequest(),
                                new InterstitialAdLoadCallback() {
                                    @Override
                                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                        super.onAdLoaded(interstitialAd);
                                        mInterstitialSplash = interstitialAd;
                                        AppOpenManager.getInstance().disableAppResume();
                                        onShowSplash((Activity) context, adListener);
                                        //tracking adjust
                                        interstitialAd.setOnPaidEventListener(adValue -> {
                                            Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                            FirebaseUtil.logPaidAdImpression(context,
                                                    adValue,
                                                    interstitialAd.getAdUnitId(), AdType.INTERSTITIAL);
                                            adListener.onEarnRevenue((double) adValue.getValueMicros());
                                        });
                                    }

                                    @Override
                                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                        super.onAdFailedToLoad(loadAdError);
                                        listID.remove(0);
                                        if (listID.size() == 0) {
                                            mInterstitialSplash = null;
                                            adListener.onAdFailedToLoad(loadAdError);
                                            adListener.onNextAction();
                                        } else {
                                            loadSplashInterAdsFloor(context, listID, 100, adListener);
                                        }

                                    }

                                });
                    }
                }, timeDelay);
            }
        }
    }

    @Override
    public void dismissLoadingDialog() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void loadInterAds(Context context, String id, AdCallback adCallback) {
        if (AppPurchase.getInstance().isPurchased(context) || !isShowAllAds) {
            adCallback.onInterstitialLoad(null);
            return;
        }

        if (isShowInter) {
            isTimeout = false;
            interstitialAd = null;
            InterstitialAd.load(context, id, getAdRequest(),
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            if (adCallback != null) {
                                adCallback.onInterstitialLoad(interstitialAd);
                            }

                            //tracking adjust
                            interstitialAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                FirebaseUtil.logPaidAdImpression(context,
                                        adValue,
                                        interstitialAd.getAdUnitId(), AdType.INTERSTITIAL);
                                adCallback.onEarnRevenue((double) adValue.getValueMicros());
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.i(TAG, loadAdError.getMessage());
                            if (adCallback != null)
                                adCallback.onAdFailedToLoad(loadAdError);
                            adCallback.onNextAction();
                        }

                    });
        }
    }

    @Override
    public void showInterAds(Context context, InterstitialAd mInterstitialAd, AdCallback callback) {
        showInterAds(context, mInterstitialAd, callback, false);
    }

    @Override
    public void loadAndShowInter(Activity activity, String idInter, int timeDelay, int timeOut, AdCallback callback) {
        if (!isNetworkConnected()) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        if (AppPurchase.getInstance().isPurchased(context) && !isShowAllAds && !isShowInter) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }

        if (AppOpenManager.getInstance().isInitialized()) {
            AppOpenManager.getInstance().disableAppResumeWithActivity(activity.getClass());
        }
        Dialog dialog2 = new LoadingAdsDialog(activity);
        dialog2.show();
        InterstitialAd.load(activity, idInter, getAdRequestTimeOut(timeOut), new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                dialog2.dismiss();
                callback.onAdFailedToLoad(loadAdError);
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                }
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                interstitialAd.setOnPaidEventListener(adValue -> {
                    Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                    FirebaseUtil.logPaidAdImpression(context,
                            adValue,
                            interstitialAd.getAdUnitId(), AdType.INTERSTITIAL);
                    callback.onEarnRevenue((double) adValue.getValueMicros());
                });
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            dialog2.dismiss();
                            callback.onAdClosed();
                            callback.onNextAction();
                            if (AppOpenManager.getInstance().isInitialized()) {
                                AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                                AppOpenManagerImpl.getInstance().setInterstitialShowing(false);
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            dialog2.dismiss();
                            callback.onAdClosed();
                            callback.onNextAction();
                            if (AppOpenManager.getInstance().isInitialized()) {
                                AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                            }
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d("TAG", "The ad was shown.");
                        }

                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                            if (disableAdResumeWhenClickAds)
                                AppOpenManager.getInstance().disableAdResumeByClickAction();
                            if (timeLimitAds > 1000) {
                                setTimeLimitInter();
                            }
                            FirebaseUtil.logClickAdsEvent(context, mInterstitialSplash.getAdUnitId());
                        }
                    });
//                    if (activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED) && interstitialAd != null) {
                    interstitialAd.show(activity);
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManagerImpl.getInstance().setInterstitialShowing(true);
                    }
//                    } else {
//                        if (AppOpenManager.getInstance().isInitialized()) {
//                            AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
//                            dialog2.dismiss();
//                        }
//                    }
                }, timeDelay);
            }
        });
    }

    @Override
    public void loadAndShowInter(Activity activity, String idInter, boolean isShowFirstTime, AdCallback callback) {
        if (!isNetworkConnected()) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        if (AppPurchase.getInstance().isPurchased(context) && !isShowAllAds && !isShowInter) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }

        if (!isShowFirstTime && lastTimeShowAds == 0) {
            lastTimeShowAds = Calendar.getInstance().getTimeInMillis();
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }

        if (Calendar.getInstance().getTimeInMillis() - lastTimeShowAds <= intervalShowInterstitial * 1000L) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }

        if (AppOpenManager.getInstance().isInitialized()) {
            AppOpenManager.getInstance().disableAppResumeWithActivity(activity.getClass());
        }
        Dialog dialog2 = new LoadingAdsDialog(activity);
        dialog2.show();
        InterstitialAd.load(activity, idInter, getAdRequestTimeOut(15000), new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                dialog2.dismiss();
                callback.onAdFailedToLoad(loadAdError);
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                }
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                interstitialAd.setOnPaidEventListener(adValue -> {
                    Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                    FirebaseUtil.logPaidAdImpression(context,
                            adValue,
                            interstitialAd.getAdUnitId(), AdType.INTERSTITIAL);
                    callback.onEarnRevenue((double) adValue.getValueMicros());
                });
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        lastTimeShowAds = Calendar.getInstance().getTimeInMillis();
                        dialog2.dismiss();
                        callback.onAdClosed();
                        callback.onNextAction();
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().setInterstitialShowing(false);
                            AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                        }
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        dialog2.dismiss();
                        callback.onAdClosed();
                        callback.onNextAction();
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                        }
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d("TAG", "The ad was shown.");
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        if (timeLimitAds > 1000) {
                            setTimeLimitInter();
                        }
                        FirebaseUtil.logClickAdsEvent(context, mInterstitialSplash.getAdUnitId());
                    }
                });
                interstitialAd.show(activity);
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManagerImpl.getInstance().setInterstitialShowing(true);
                }
            }
        });
    }

    @Override
    public void initRewardAds(Context context, String id) {
        if (AppPurchase.getInstance().isPurchased(context) || !isShowAllAds) {
            return;
        }
        this.rewardedId = id;
        RewardedAd.load(context, id, getAdRequest(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                AdmobImpl.this.rewardedAd = rewardedAd;
                AdmobImpl.this.rewardedAd.setOnPaidEventListener(adValue -> {

                    Log.d(TAG, "OnPaidEvent Reward:" + adValue.getValueMicros());
                    FirebaseUtil.logPaidAdImpression(context,
                            adValue,
                            rewardedAd.getAdUnitId(),
                            AdType.REWARDED);
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.e(TAG, "RewardedAd onAdFailedToLoad: " + loadAdError.getMessage());
            }
        });
    }

    @Override
    public void showRewardAds(Activity context, RewardCallback adCallback) {
        if (!isShowAllAds || !isNetworkConnected()) {
            adCallback.onAdClosed();
            return;
        }
        if (rewardedAd == null) {
            initRewardAds(context, rewardedId);
            adCallback.onAdFailedToShow(0);
            return;
        } else {
            AdmobImpl.this.rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    if (adCallback != null)
                        adCallback.onAdClosed();

                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }

                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    if (adCallback != null)
                        adCallback.onAdFailedToShow(adError.getCode());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().disableAppResume();
                    }
                    initRewardAds(context, rewardedId);
                    rewardedAd = null;
                }

                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, rewardedAd.getAdUnitId());
                }
            });
            rewardedAd.show(context, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    if (adCallback != null) {
                        adCallback.onEarnedReward(rewardItem);

                    }
                }
            });
        }
    }

    @Override
    public RewardedAd getRewardedAd() {
        return rewardedAd;
    }

    @Override
    public void loadNativeAd(Context context, String id, NativeCallback callback) {
        if (AppPurchase.getInstance().isPurchased(context) || !isShowAllAds || !isNetworkConnected()) {
            callback.onAdFailedToLoad();
            return;
        }
        if (isShowNative) {
            if (isNetworkConnected()) {
                VideoOptions videoOptions = new VideoOptions.Builder()
                        .setStartMuted(true)
                        .build();

                NativeAdOptions adOptions = new NativeAdOptions.Builder()
                        .setVideoOptions(videoOptions)
                        .build();
                AdLoader adLoader = new AdLoader.Builder(context, id)
                        .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                            @Override
                            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                                callback.onNativeAdLoaded(nativeAd);
                                nativeAd.setOnPaidEventListener(adValue -> {
                                    Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                    FirebaseUtil.logPaidAdImpression(context,
                                            adValue,
                                            id,
                                            AdType.NATIVE);
                                    callback.onEarnRevenue((double) adValue.getValueMicros());
                                });
                            }
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {
                                Log.e(TAG, "NativeAd onAdFailedToLoad: " + error.getMessage());
                                callback.onAdFailedToLoad();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                FirebaseUtil.logClickAdsEvent(context, id);
                                if (timeLimitAds > 1000) {
                                    setTimeLimitNative();
                                    if (callback != null) {
                                        callback.onAdFailedToLoad();
                                    }
                                }
                            }

                            @Override
                            public void onAdImpression() {
                                super.onAdImpression();
                                callback.onAdImpression();
                            }
                        })
                        .withNativeAdOptions(adOptions)
                        .build();
                adLoader.loadAd(getAdRequest());
            } else {
                callback.onAdFailedToLoad();
            }
        } else {
            callback.onAdFailedToLoad();
        }
    }

    @Override
    public void loadNativeWithAutRefresh(Context context, NativeAdView nativeAdView, ViewGroup adContainer, ViewGroup shimmer, NativeAdmobPlugin.NativeConfig config) {
        new NativeAdmobPlugin(context, nativeAdView, adContainer, shimmer, config);
    }

    @Override
    public void loadNativeAd(Context context, String id, NativeCallback callback, int adChoicesPlacement) {
        if (AppPurchase.getInstance().isPurchased(context) || !isShowAllAds || !isNetworkConnected()) {
            callback.onAdFailedToLoad();
            return;
        }
        if (isShowNative) {
            if (isNetworkConnected()) {
                VideoOptions videoOptions = new VideoOptions.Builder()
                        .setStartMuted(true)
                        .build();

                NativeAdOptions adOptions = new NativeAdOptions.Builder()
                        .setVideoOptions(videoOptions)
                        .setAdChoicesPlacement(adChoicesPlacement)
                        .build();
                AdLoader adLoader = new AdLoader.Builder(context, id)
                        .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                            @Override
                            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                                callback.onNativeAdLoaded(nativeAd);
                                nativeAd.setOnPaidEventListener(adValue -> {
                                    Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                    FirebaseUtil.logPaidAdImpression(context,
                                            adValue,
                                            id,
                                            AdType.NATIVE);
                                    callback.onEarnRevenue((double) adValue.getValueMicros());
                                });
                            }
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {
                                Log.e(TAG, "NativeAd onAdFailedToLoad: " + error.getMessage());
                                callback.onAdFailedToLoad();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                FirebaseUtil.logClickAdsEvent(context, id);
                                if (timeLimitAds > 1000) {
                                    setTimeLimitNative();
                                    if (callback != null) {
                                        callback.onAdFailedToLoad();
                                    }
                                }
                            }

                            @Override
                            public void onAdImpression() {
                                super.onAdImpression();
                                callback.onAdImpression();
                            }
                        })
                        .withNativeAdOptions(adOptions)
                        .build();
                adLoader.loadAd(getAdRequest());
            } else {
                callback.onAdFailedToLoad();
            }
        } else {
            callback.onAdFailedToLoad();
        }
    }

    @Override
    public void loadNativeAdFullScreen(Context context, String id, int mediaAspectRatio, NativeCallback callback) {
        if (AppPurchase.getInstance().isPurchased(context) || !isShowAllAds || !isNetworkConnected()) {
            callback.onAdFailedToLoad();
            return;
        }
        if (isShowNative) {
            if (isNetworkConnected()) {
                VideoOptions videoOptions = new VideoOptions.Builder()
                        .setStartMuted(false)
                        .setCustomControlsRequested(true)
                        .build();

                NativeAdOptions adOptions = new NativeAdOptions.Builder()
                        .setVideoOptions(videoOptions)
                        .setMediaAspectRatio(mediaAspectRatio)
                        .build();
                AdLoader adLoader = new AdLoader.Builder(context, id)
                        .forNativeAd(nativeAd -> {
                            callback.onNativeAdLoaded(nativeAd);
                            nativeAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                FirebaseUtil.logPaidAdImpression(context,
                                        adValue,
                                        id,
                                        AdType.NATIVE);
                                callback.onEarnRevenue((double) adValue.getValueMicros());
                            });
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {
                                Log.e(TAG, "NativeAd onAdFailedToLoad: " + error.getMessage());
                                callback.onAdFailedToLoad();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                FirebaseUtil.logClickAdsEvent(context, id);
                                if (timeLimitAds > 1000) {
                                    setTimeLimitNative();
                                    if (callback != null) {
                                        callback.onAdFailedToLoad();
                                    }
                                }
                            }

                            @Override
                            public void onAdImpression() {
                                super.onAdImpression();
                                callback.onAdImpression();
                            }
                        })
                        .withNativeAdOptions(adOptions)
                        .build();
                adLoader.loadAd(getAdRequest());
            } else {
                callback.onAdFailedToLoad();
            }
        } else {
            callback.onAdFailedToLoad();
        }
    }

    @Override
    public void loadNativeAdFloor(Context context, List<String> listID, NativeCallback callback) {
        if (listID == null || listID.size() == 0) {
            callback.onAdFailedToLoad();
        } else {
            if (AppPurchase.getInstance().isPurchased(context) || !isShowAllAds) {
                callback.onAdFailedToLoad();
                return;
            }
            NativeCallback callback1 = new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    callback.onNativeAdLoaded(nativeAd);
                    nativeAd.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                        FirebaseUtil.logPaidAdImpression(context,
                                adValue,
                                listID.get(0),
                                AdType.NATIVE);
                        callback.onEarnRevenue((double) adValue.getValueMicros());
                    });
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    if (listID.size() > 0) {
                        listID.remove(0);
                        loadNativeAdFloor(context, listID, callback);
                    }
                }
            };
            if (listID.size() > 0) {
                int position = 0;
                Log.e(TAG, "Load Native ID :" + listID.get(position));
                loadNativeAd(context, listID.get(position), callback1);
            } else {
                callback.onAdFailedToLoad();
            }
        }
    }

    @Override
    public void pushAdsToViewCustom(NativeAd nativeAd, NativeAdView adView) {
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        if (adView.getHeadlineView() != null) {
            if (nativeAd.getHeadline() != null) {
                adView.getHeadlineView().setVisibility(View.VISIBLE);
                ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
            } else {
                adView.getHeadlineView().setVisibility(View.INVISIBLE);
            }
        }
        if (adView.getBodyView() != null) {
            if (nativeAd.getBody() == null) {
                adView.getBodyView().setVisibility(View.INVISIBLE);
            } else {
                adView.getBodyView().setVisibility(View.VISIBLE);
                ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
            }
        }

        if (adView.getCallToActionView() != null) {
            if (nativeAd.getCallToAction() == null) {
                adView.getCallToActionView().setVisibility(View.INVISIBLE);
            } else {
                adView.getCallToActionView().setVisibility(View.VISIBLE);
                ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            }
        }

        if (adView.getIconView() != null) {
            if (nativeAd.getIcon() == null) {
                adView.getIconView().setVisibility(View.GONE);
            } else {
                if (nativeAd.getIcon().getDrawable() != null)
                    ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            }
        }

        if (adView.getAdvertiserView() != null) {
            if (nativeAd.getAdvertiser() == null) {
                adView.getAdvertiserView().setVisibility(View.INVISIBLE);
            } else {
                ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
                adView.getAdvertiserView().setVisibility(View.VISIBLE);
            }
        }

        adView.setNativeAd(nativeAd);
    }

    @Override
    public void loadNativeFragment(Activity mActivity, String id, View parent) {
        final FrameLayout frameLayout = parent.findViewById(R.id.fl_load_native);
        final ShimmerFrameLayout containerShimmer = parent.findViewById(R.id.shimmer_container_native);
        loadNative(mActivity, containerShimmer, frameLayout, id, R.layout.native_admob_ad);
    }

    @Override
    public void setOpenActivityAfterShowInterAds(boolean openActivityAfterShowInterAds) {
        this.openActivityAfterShowInterAds = openActivityAfterShowInterAds;
    }

    @Override
    public String getDeviceId(Activity activity) {
        String android_id = Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return md5(android_id).toUpperCase();
    }

    @Override
    public void onCheckShowSplashWhenFail(Activity activity, AdCallback callback, int timeDelay) {
        if (isNetworkConnected()) {
            (new Handler(activity.getMainLooper())).postDelayed(new Runnable() {
                public void run() {
                    if (AdmobImpl.this.interstitialSplashLoaded() && !AdmobImpl.this.isShowLoadingSplash) {
                        Log.i("Admob", "show ad splash when show fail in background");
                        onShowSplash(activity, callback);
                    }

                }
            }, (long) timeDelay);
        }
    }

    @Override
    public void onCheckShowSplashWhenFailClickButton(AppCompatActivity activity, InterstitialAd interstitialAd, AdCallback callback, int timeDelay) {
        if (interstitialAd != null) {
            if (isNetworkConnected()) {
                (new Handler(activity.getMainLooper())).postDelayed(new Runnable() {
                    public void run() {
                        if (AdmobImpl.this.interstitialSplashLoaded() && !AdmobImpl.this.isShowLoadingSplash) {
                            Log.i("Admob", "show ad splash when show fail in background");
                            onShowSplash(activity, interstitialAd, callback);
                        }

                    }
                }, (long) timeDelay);
            }
        }
    }

    private AdRequest getAdRequest() {
        AdRequest.Builder builder = new AdRequest.Builder();
        return builder.build();
    }

    private AdRequest getAdRequestTimeOut(int timeOut) {
        if (timeOut < 5000) timeOut = 5000;
        return (AdRequest) new AdRequest.Builder().setHttpTimeoutMillis(timeOut).build();
    }

    private void loadBanner(final Activity mActivity, String id, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer, final AdCallback callback, Boolean useInlineAdaptive, String inlineStyle) {

        if (AppPurchase.getInstance().isPurchased(mActivity)) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }

        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        try {
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(id);
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, useInlineAdaptive, inlineStyle);
            int adHeight;
            if (useInlineAdaptive && inlineStyle.equalsIgnoreCase(BANNER_INLINE_SMALL_STYLE)) {
                adHeight = MAX_SMALL_INLINE_BANNER_HEIGHT;
            } else {
                adHeight = adSize.getHeight();
            }
            containerShimmer.getLayoutParams().height = (int) (adHeight * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    containerShimmer.stopShimmer();
                    adContainer.setVisibility(View.GONE);
                    containerShimmer.setVisibility(View.GONE);
                }


                @Override
                public void onAdLoaded() {
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    if (adView != null) {
                        adView.setOnPaidEventListener(adValue -> {
                            Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                            FirebaseUtil.logPaidAdImpression(context,
                                    adValue,
                                    adView.getAdUnitId(), AdType.BANNER);
                        });
                    }
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, id);
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                }
            });

            adView.loadAd(getAdRequest());
        } catch (Exception e) {
            e.printStackTrace();
            adContainer.removeAllViews();
        }
    }

    private void loadCollapsibleBanner(final Activity mActivity, String id, String gravity, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer) {

        if (AppPurchase.getInstance().isPurchased(mActivity)) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }

        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        try {
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(id);
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, false, "");
            containerShimmer.getLayoutParams().height = (int) (adSize.getHeight() * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.loadAd(getAdRequestForCollapsibleBanner(gravity));
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    containerShimmer.stopShimmer();
                    adContainer.setVisibility(View.GONE);
                    containerShimmer.setVisibility(View.GONE);

                }

                @Override
                public void onAdLoaded() {
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    adView.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                        FirebaseUtil.logPaidAdImpression(context,
                                adValue,
                                adView.getAdUnitId(), AdType.BANNER);
                    });

                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, id);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AdSize getAdSize(Activity mActivity, Boolean useInlineAdaptive, String inlineStyle) {

        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        if (useInlineAdaptive) {
            if (inlineStyle.equalsIgnoreCase(BANNER_INLINE_LARGE_STYLE)) {
                return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(mActivity, adWidth);
            } else {
                return AdSize.getInlineAdaptiveBannerAdSize(adWidth, MAX_SMALL_INLINE_BANNER_HEIGHT);
            }
        }
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth);

    }

    private AdRequest getAdRequestForCollapsibleBanner(String gravity) {
        AdRequest.Builder builder = new AdRequest.Builder();
        Bundle admobExtras = new Bundle();
        admobExtras.putString("collapsible", gravity);
        builder.addNetworkExtrasBundle(AdMobAdapter.class, admobExtras);
        return builder.build();
    }


    private void onShowSplash(Activity activity, AdCallback adListener) {
        Log.e(TAG, "onShowSplash: start");
        isShowLoadingSplash = true;
        if (mInterstitialSplash == null) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            adListener.onAdClosed();
            adListener.onNextAction();
            return;
        }
        mInterstitialSplash.setOnPaidEventListener(adValue -> {
            Log.d(TAG, "OnPaidEvent splash:" + adValue.getValueMicros());
            FirebaseUtil.logPaidAdImpression(context,
                    adValue,
                    mInterstitialSplash.getAdUnitId(), AdType.INTERSTITIAL);
            adListener.onEarnRevenue((double) adValue.getValueMicros());
        });

        if (handlerTimeout != null && rdTimeout != null) {
            handlerTimeout.removeCallbacks(rdTimeout);
        }

        if (adListener != null) {
            adListener.onAdLoaded();
        }

        mInterstitialSplash.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                isShowLoadingSplash = true;
                Log.e(TAG, "onAdShowedFullScreenContent ");
                if (logTimeLoadAdsSplash) {
                    long timeLoad = System.currentTimeMillis() - currentTime;
                    Log.e(TAG, "load ads time :" + timeLoad);
                    FirebaseUtil.logTimeLoadAdsSplash(activity, round1000(timeLoad));
                }


            }

            @Override
            public void onAdDismissedFullScreenContent() {
                Log.e(TAG, "DismissedFullScreenContent Splash");
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                isShowInter = true;
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                if (adListener != null) {
                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    } else {
                        adListener.onAdClosedByUser();
                    }

                }
                mInterstitialSplash = null;
                isShowLoadingSplash = true;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                mInterstitialSplash = null;
                isShowLoadingSplash = false;
                Log.e(TAG, "onAdFailedToShowFullScreenContent Splash");
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                if (adListener != null) {
                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdFailedToShow(adError);
                        adListener.onNextAction();
                    }


                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (disableAdResumeWhenClickAds)
                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                if (timeLimitAds > 1000) {
                    setTimeLimitInter();
                }
                FirebaseUtil.logClickAdsEvent(context, mInterstitialSplash.getAdUnitId());
            }
        });
        Log.e(TAG, "onShowSplash: dialog");

        if (mInterstitialSplash == null) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            adListener.onAdClosed();
            adListener.onNextAction();
            return;
        }
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            try {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                dialog = new LoadingAdsDialog(activity);
                try {
                    Log.e(TAG, "onShowSplash: dialog.show");
                    dialog.show();
                } catch (Exception e) {
                    adListener.onAdClosed();
                    adListener.onNextAction();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "onShowSplash: dialog.Exception");
                dialog = null;
                e.printStackTrace();
            }
            new Handler().postDelayed(() -> {
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().disableAppResume();
                }

                if (openActivityAfterShowInterAds && adListener != null) {

//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (dialog != null && dialog.isShowing() && !activity.isDestroyed()) {
//                                dialog.dismiss();
//                                Log.e(TAG, "onShowSplash: dialog.dismiss1");
//                            }
//                        }
//                    }, 1500);
                    adListener.onAdClosed();
                    adListener.onNextAction();


                }

                if (activity != null) {
                    if (mInterstitialSplash != null) {
                        mInterstitialSplash.show(activity);
                    } else {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                            Log.e(TAG, "onShowSplash: dialog.dismiss");
                        }
                        adListener.onAdClosed();
                        adListener.onNextAction();
                        isShowLoadingSplash = false;
                    }
                    Log.e(TAG, "onShowSplash: mInterstitialSplash.show");
                } else if (adListener != null) {
                    Log.e(TAG, "onShowSplash: adListener");
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    adListener.onAdClosed();
                    adListener.onNextAction();
                    isShowLoadingSplash = false;
                }
            }, 500);
        } else {
            isShowLoadingSplash = false;
            Log.e(TAG, "onShowSplash: fail on background");
        }
    }

    private void onShowSplash(Activity activity, InterstitialAd interSplash, AdCallback adListener) {
        AppOpenManager.getInstance().disableAppResume();
        isShowLoadingSplash = true;
        mInterstitialSplash = interSplash;
        if (!isNetworkConnected()) {
            adListener.onAdClosed();
            return;
        } else {
            if (mInterstitialSplash == null) {
                adListener.onAdClosed();
                adListener.onNextAction();
                return;
            } else {
                mInterstitialSplash.setOnPaidEventListener(adValue -> {
                    Log.d(TAG, "OnPaidEvent splash:" + adValue.getValueMicros());
                    FirebaseUtil.logPaidAdImpression(context,
                            adValue,
                            mInterstitialSplash.getAdUnitId(), AdType.INTERSTITIAL);
                    adListener.onEarnRevenue((double) adValue.getValueMicros());
                });

                if (handlerTimeout != null && rdTimeout != null) {
                    handlerTimeout.removeCallbacks(rdTimeout);
                }

                if (adListener != null) {
                    adListener.onAdLoaded();
                }

                mInterstitialSplash.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().disableAppResume();
                        }
                        isShowLoadingSplash = true;
                        if (logTimeLoadAdsSplash) {
                            long timeLoad = System.currentTimeMillis() - currentTime;
                            Log.e(TAG, "load ads time :" + timeLoad);
                            FirebaseUtil.logTimeLoadAdsSplash(activity, round1000(timeLoad));
                        }
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.e(TAG, "DismissedFullScreenContent Splash");
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().enableAppResume();
                        }
                        if (adListener != null) {
                            if (!openActivityAfterShowInterAds) {
                                adListener.onAdClosed();
                                adListener.onNextAction();
                            } else {
                                adListener.onAdClosedByUser();
                            }

                            if (dialog != null) {
                                dialog.dismiss();
                            }

                        }
                        mInterstitialSplash = null;
                        isShowLoadingSplash = true;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Log.e(TAG, "onAdFailedToShowFullScreenContent : " + adError);
                        //  mInterstitialSplash = null;
                        if (adError.getCode() == 1) {
                            mInterstitialSplash = null;
                            adListener.onAdClosed();
                        }
                        isShowLoadingSplash = false;
                        if (adListener != null) {
                            adListener.onAdFailedToShow(adError);

                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        if (timeLimitAds > 1000) {
                            setTimeLimitInter();
                        }
                        FirebaseUtil.logClickAdsEvent(context, mInterstitialSplash.getAdUnitId());
                    }
                });
                if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    try {
                        if (dialog != null && dialog.isShowing())
                            dialog.dismiss();
                        dialog = new LoadingAdsDialog(activity);
                        try {
                            dialog.show();
                        } catch (Exception e) {
                            adListener.onAdClosed();
                            adListener.onNextAction();
                            return;
                        }
                    } catch (Exception e) {
                        dialog = null;
                        e.printStackTrace();
                    }
                    new Handler().postDelayed(() -> {
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().disableAppResume();
                        }

                        if (openActivityAfterShowInterAds && adListener != null) {
                            adListener.onAdClosed();
                            adListener.onNextAction();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
                                        dialog.dismiss();
                                }
                            }, 1500);
                        }

                        if (activity != null) {
                            mInterstitialSplash.show(activity);
                            Log.e(TAG, "onShowSplash: mInterstitialSplash.show");
                            isShowLoadingSplash = false;
                        } else if (adListener != null) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            adListener.onAdClosed();
                            adListener.onNextAction();
                            isShowLoadingSplash = false;
                        }
                    }, 300);
                } else {
                    isShowLoadingSplash = false;
                    Log.e(TAG, "onShowSplash: fail on background");
                }
            }

        }

    }

    private void showInterAds(Context context, InterstitialAd mInterstitialAd, final AdCallback callback, boolean shouldReload) {
        currentClicked = numShowAds;
        showInterAdByTimes(context, mInterstitialAd, callback, shouldReload);
    }

    private void showInterAdByTimes(final Context context, InterstitialAd mInterstitialAd, final AdCallback callback, final boolean shouldReloadAds) {
        if (logLogTimeShowAds) {
            currentTimeShowAds = System.currentTimeMillis();
        }
        Helper.setupAdmobData(context);
        if (AppPurchase.getInstance().isPurchased(context) || !isShowAllAds) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        if (mInterstitialAd == null) {
            if (callback != null) {
                callback.onAdClosed();
                callback.onNextAction();
            }
            return;
        }

        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                // Called when fullscreen content is dismissed.
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                isShowInter = true;
                if (callback != null) {
                    if (!openActivityAfterShowInterAds) {
                        callback.onAdClosed();
                        callback.onNextAction();
                    } else {
                        callback.onAdClosedByUser();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }

                }
                Log.e(TAG, "onAdDismissedFullScreenContent");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());


                // Called when fullscreen content failed to show.
                if (callback != null) {
                    if (!openActivityAfterShowInterAds) {
                        callback.onAdClosed();
                        callback.onNextAction();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                // Called when fullscreen content is shown.
                if (logLogTimeShowAds) {
                    long timeLoad = System.currentTimeMillis() - currentTimeShowAds;
                    Log.e(TAG, "show ads time :" + timeLoad);
                    FirebaseUtil.logTimeLoadShowAdsInter(context, (double) timeLoad / 1000);
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (disableAdResumeWhenClickAds)
                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                if (timeLimitAds > 1000)
                    setTimeLimitInter();
                FirebaseUtil.logClickAdsEvent(context, mInterstitialAd.getAdUnitId());
            }
        });

        if (Helper.getNumClickAdsPerDay(context, mInterstitialAd.getAdUnitId()) < maxClickAds) {
            showInterstitialAd(context, mInterstitialAd, callback);
            return;
        }
        if (callback != null) {
            callback.onAdClosed();
            callback.onNextAction();
        }
    }

    private void showInterstitialAd(Context context, InterstitialAd mInterstitialAd, AdCallback callback) {
        if (!isShowInter || !isShowAllAds) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        if (!isNetworkConnected() || mInterstitialAd == null) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        currentClicked++;
        if (currentClicked >= numShowAds && mInterstitialAd != null) {
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    dialog = new LoadingAdsDialog(context);
                    try {
                        dialog.show();
                    } catch (Exception e) {
                        callback.onAdClosed();
                        callback.onNextAction();
                        return;
                    }
                } catch (Exception e) {
                    dialog = null;
                    e.printStackTrace();
                }
                new Handler().postDelayed(() -> {
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().disableAppResume();
                    }

                    if (openActivityAfterShowInterAds && callback != null) {
                        callback.onAdClosed();
                        callback.onNextAction();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog != null && dialog.isShowing() && !((Activity) context).isDestroyed())
                                    dialog.dismiss();
                            }
                        }, 1500);
                    }
                    isShowInter = false;
                    mInterstitialAd.show((Activity) context);

                }, 800);

            }
            currentClicked = 0;
        } else if (callback != null) {
            if (dialog != null) {
                dialog.dismiss();
            }
            callback.onAdClosed();
            callback.onNextAction();
        }
    }

    private void loadNative(final Context context, final ShimmerFrameLayout containerShimmer, final FrameLayout frameLayout, final String id, final int layout) {
        if (AppPurchase.getInstance().isPurchased(context) || !isShowAllAds) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }
        frameLayout.removeAllViews();
        frameLayout.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();


        AdLoader adLoader = new AdLoader.Builder(context, id)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        containerShimmer.stopShimmer();
                        containerShimmer.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.VISIBLE);
                        @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) LayoutInflater.from(context)
                                .inflate(layout, null);
                        pushAdsToViewCustom(nativeAd, adView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                        nativeAd.setOnPaidEventListener(adValue -> {
                            Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                            FirebaseUtil.logPaidAdImpression(context,
                                    adValue,
                                    id,
                                    AdType.NATIVE);
                        });
                    }


                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        Log.e(TAG, "onAdFailedToLoad: " + error.getMessage());
                        containerShimmer.stopShimmer();
                        containerShimmer.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        FirebaseUtil.logClickAdsEvent(context, id);
                    }

                })
                .withNativeAdOptions(adOptions)
                .build();

        adLoader.loadAd(getAdRequest());
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    private void setTimeLimitInter() {
        if (timeLimitAds > 1000) {
            isShowInter = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isShowInter = true;
                }
            }, timeLimitAds);
        }
    }

    private void setTimeLimitNative() {
        if (timeLimitAds > 1000) {
            isShowNative = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isShowNative = true;
                }
            }, timeLimitAds);
        }

    }

    private int round1000(long time) {
        return (int) (Math.round(time / 1000));
    }

    private String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }

    @Override
    public void loadAndShowNativeWithCheckingNetworkType(Context context, String id, ViewGroup layoutContainer, NativeAdView normalView, NativeAdView customView, NativeCallback callback) {
        if (AppPurchase.getInstance().isPurchased(context) || !isShowAllAds || !isNetworkConnected()) {
            callback.onAdFailedToLoad();
            return;
        }
        if (isShowNative) {
            if (isNetworkConnected()) {
                VideoOptions videoOptions = new VideoOptions.Builder()
                        .setStartMuted(true)
                        .build();

                NativeAdOptions adOptions = new NativeAdOptions.Builder()
                        .setVideoOptions(videoOptions)
                        .build();
                AdLoader adLoader = new AdLoader.Builder(context, id)
                        .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                            @Override
                            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                                callback.onNativeAdLoaded(nativeAd);
                                layoutContainer.removeAllViews();
                                if (isLoadFullAds()) {
                                    layoutContainer.addView(customView);
                                    pushAdsToViewCustom(nativeAd, customView);
                                } else {
                                    layoutContainer.addView(normalView);
                                    pushAdsToViewCustom(nativeAd, normalView);
                                }
                                nativeAd.setOnPaidEventListener(adValue -> {
                                    Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                    FirebaseUtil.logPaidAdImpression(context,
                                            adValue,
                                            id,
                                            AdType.NATIVE);
                                    callback.onEarnRevenue((double) adValue.getValueMicros());
                                });
                            }
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {
                                Log.e(TAG, "NativeAd onAdFailedToLoad: " + error.getMessage());
                                callback.onAdFailedToLoad();
                                layoutContainer.removeAllViews();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                FirebaseUtil.logClickAdsEvent(context, id);
                                if (timeLimitAds > 1000) {
                                    setTimeLimitNative();
                                    if (callback != null) {
                                        callback.onAdFailedToLoad();
                                    }
                                }
                            }

                            @Override
                            public void onAdImpression() {
                                super.onAdImpression();
                                callback.onAdImpression();
                            }
                        })
                        .withNativeAdOptions(adOptions)
                        .build();
                adLoader.loadAd(getAdRequest());
            } else {
                callback.onAdFailedToLoad();
            }
        } else {
            callback.onAdFailedToLoad();
        }
    }

    @Override
    public boolean isLoadFullAds() {
        if (!Helper.isOfficiallyApps) {
            return false;
        } else if (((AdsApplication) context).buildDebug()) {
            return true;
        } else if (AppFlyer.getInstance().enableAppsFlyer()) {
            return AppFlyer.getInstance().isNonOrganic();
        } else if (((AdsApplication) context).enableAdjustTracking()) {
            return Adjust.getInstance().isNonOrganic();
        } else {
            return false;
        }
    }
}
