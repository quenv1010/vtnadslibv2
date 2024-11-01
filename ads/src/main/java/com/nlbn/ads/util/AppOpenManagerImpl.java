package com.nlbn.ads.util;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nlbn.ads.billing.AppPurchase;
import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.dialog.LoadingAdsDialog;
import com.nlbn.ads.dialog.ResumeLoadingDialog;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class AppOpenManagerImpl extends AppOpenManager implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private static final String TAG = "AppOpenManager";
    public static final String AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/3419835294";

    private static volatile AppOpenManagerImpl INSTANCE;
    private AppOpenAd appResumeAd = null;
    private AppOpenAd splashAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private FullScreenContentCallback fullScreenContentCallback;
    private boolean isShowLoadingSplash = false; //kiểm tra trạng thái ad splash, ko cho load, show khi đang show loading ads splash
    private String appResumeAdId = "";
    private String splashAdId;

    private Activity currentActivity;

    private Application myApplication;

    private static boolean isShowingAd = false;
    private boolean isShowingAdResume = false;
    private long appResumeLoadTime = 0;
    private long splashLoadTime = 0;
    private int splashTimeout = 0;

    private boolean isInitialized = false;// on  - off ad resume on app
    private boolean isAppResumeEnabled = true;
    private boolean isInterstitialShowing = false;
    private boolean enableScreenContentCallback = false; // default =  true when use splash & false after show splash
    private boolean disableAdResumeByClickAction = false;
    private final List<Class> disabledAppOpenList;
    private Class splashActivity;

    private boolean isTimeout = false;
    private static final int TIMEOUT_MSG = 11;

    private Handler timeoutHandler;

    private AdCallback resumeCallback;

    public void setSplashAdId(String splashAdId) {
        this.splashAdId = splashAdId;
    }

    /**
     * Constructor
     */
    private AppOpenManagerImpl() {
        disabledAppOpenList = new ArrayList<>();
    }

    public static synchronized AppOpenManagerImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppOpenManagerImpl();
        }
        return INSTANCE;
    }

    /**
     * Init AppOpenManager
     *
     * @param application
     */
    @Override
    public void init(Application application) {
        isInitialized = true;
        disableAdResumeByClickAction = false;
        this.myApplication = application;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
//        if (!Purchase.getInstance().isPurchased(application.getApplicationContext()) &&
//                !isAdAvailable(false) && appOpenAdId != null) {
//            fetchAd(false);
//        }
    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    @Override
    public void setEnableScreenContentCallback(boolean enableScreenContentCallback) {
        this.enableScreenContentCallback = enableScreenContentCallback;
    }

    @Override
    public boolean isInterstitialShowing() {
        return isInterstitialShowing;
    }

    @Override
    public void setInterstitialShowing(boolean interstitialShowing) {
        isInterstitialShowing = interstitialShowing;
    }

    /**
     * Call disable ad resume when click a button, auto enable ad resume in next start
     */
    @Override
    public void disableAdResumeByClickAction() {
        disableAdResumeByClickAction = true;
    }

    @Override
    public void setDisableAdResumeByClickAction(boolean disableAdResumeByClickAction) {
        this.disableAdResumeByClickAction = disableAdResumeByClickAction;
    }

    /**
     * Check app open ads is showing
     *
     * @return
     */
    @Override
    public boolean isShowingAd() {
        return isShowingAd;
    }

    /**
     * Disable app open app on specific activity
     *
     * @param activityClass
     */
    @Override
    public void disableAppResumeWithActivity(Class activityClass) {
        Log.d(TAG, "disableAppResumeWithActivity: " + activityClass.getName());
        disabledAppOpenList.add(activityClass);
    }

    @Override
    public void enableAppResumeWithActivity(Class activityClass) {
        Log.d(TAG, "enableAppResumeWithActivity: " + activityClass.getName());
        disabledAppOpenList.remove(activityClass);
    }

    @Override
    public void disableAppResume() {
        isAppResumeEnabled = false;
    }

    @Override
    public void enableAppResume() {
        isAppResumeEnabled = true;
    }

    @Override
    public void setSplashActivity(Class splashActivity, String adId, int timeoutInMillis) {
        this.splashActivity = splashActivity;
        splashAdId = adId;
        this.splashTimeout = timeoutInMillis;
    }

    @Override
    public void setAppResumeAdId(String appResumeAdId) {
        this.appResumeAdId = appResumeAdId;
    }

    @Override
    public void setFullScreenContentCallback(FullScreenContentCallback callback) {
        this.fullScreenContentCallback = callback;
    }

    @Override
    public void removeFullScreenContentCallback() {
        this.fullScreenContentCallback = null;
    }

    private void fetchAd(final boolean isSplash) {
        Log.d(TAG, "fetchAd: isSplash = " + isSplash);
        if (isAdAvailable(isSplash)) {
            return;
        }

        loadCallback =
            new AppOpenAd.AppOpenAdLoadCallback() {

                @Override
                public void onAdLoaded(AppOpenAd ad) {
                    Log.d(TAG, "onAppOpenAdLoaded: isSplash = " + isSplash);
                    if (!isSplash) {
                        AppOpenManagerImpl.this.appResumeAd = ad;
                        AppOpenManagerImpl.this.appResumeAd.setOnPaidEventListener(adValue -> {
                            FirebaseUtil.logPaidAdImpression(
                                myApplication.getApplicationContext(),
                                adValue,
                                ad.getAdUnitId(),
                                AdType.APP_OPEN);
                        });
                        AppOpenManagerImpl.this.appResumeLoadTime = (new Date()).getTime();
                    } else {
                        AppOpenManagerImpl.this.splashAd = ad;
                        AppOpenManagerImpl.this.splashAd.setOnPaidEventListener(adValue ->
                            FirebaseUtil.logPaidAdImpression(
                                myApplication.getApplicationContext(),
                                adValue,
                                ad.getAdUnitId(),
                                AdType.APP_OPEN)
                        );
                        AppOpenManagerImpl.this.splashLoadTime = (new Date()).getTime();
                    }
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    dismissDialogLoading();
                }
            };
        AdRequest request = getAdRequest();
        AppOpenAd.load(
            myApplication, isSplash ? splashAdId : appResumeAdId, request, loadCallback);
    }

    @Override
    public void setResumeCallback(AdCallback resumeCallback) {
        this.resumeCallback = resumeCallback;
    }

    /**
     * Creates and returns ad request.
     */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long loadTime, long numHours) {
        long dateDifference = (new Date()).getTime() - loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    private boolean isAdAvailable(boolean isSplash) {
        long loadTime = isSplash ? splashLoadTime : appResumeLoadTime;
        boolean wasLoadTimeLessThanNHoursAgo = wasLoadTimeLessThanNHoursAgo(loadTime, 4);
        Log.d(TAG, "isAdAvailable: " + wasLoadTimeLessThanNHoursAgo);
        return (isSplash ? splashAd != null : appResumeAd != null)
            && wasLoadTimeLessThanNHoursAgo;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        FirebaseAnalytics.getInstance(activity).logEvent(activity.getClass().getSimpleName(), new Bundle());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
        Log.d(TAG, "onActivityStarted: " + currentActivity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
        Log.d(TAG, "onActivityResumed: " + currentActivity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (!activity.getClass().getName().equals(AdActivity.class.getName())) {
            currentActivity = null;
        }
        Log.d(TAG, "onActivityDestroyed: null");
    }

    private void showAdIfAvailable(final boolean isSplash) {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (currentActivity == null) {
            if (fullScreenContentCallback != null && enableScreenContentCallback) {
                fullScreenContentCallback.onAdDismissedFullScreenContent();
            }
            return;
        }

        Log.d(TAG, "showAdIfAvailable: " + ProcessLifecycleOwner.get().getLifecycle().getCurrentState());
        Log.d(TAG, "showAd isSplash: " + isSplash);
        if (!ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            Log.d(TAG, "showAdIfAvailable: return");
            if (fullScreenContentCallback != null && enableScreenContentCallback) {
                fullScreenContentCallback.onAdDismissedFullScreenContent();
            }

            return;
        }

        if (!isShowingAd && isAdAvailable(isSplash)) {
            Log.d(TAG, "Will show ad isSplash:" + isSplash);
            if (isSplash) {
                showAdsWithLoading();
            } else {
                showResumeAds();
            }

        } else {
            Log.d(TAG, "Ad is not ready");
            if (!isSplash) {
                fetchAd(false);
            }
        }
    }

    private void showAdsWithLoading() {
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            Dialog dialog = null;
            try {
                dismissDialogLoading();
                dialog = new ResumeLoadingDialog(currentActivity);
                try {
                    dialog.show();
                } catch (Exception e) {
                    if (fullScreenContentCallback != null && enableScreenContentCallback) {
                        fullScreenContentCallback.onAdDismissedFullScreenContent();
                    }
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Dialog finalDialog = dialog;
            new Handler().postDelayed(() -> {
                if (splashAd != null) {
                    splashAd.setFullScreenContentCallback(
                        new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Set the reference to null so isAdAvailable() returns false.
                                appResumeAd = null;
                                if (fullScreenContentCallback != null && enableScreenContentCallback) {
                                    fullScreenContentCallback.onAdDismissedFullScreenContent();
                                    enableScreenContentCallback = false;
                                }
                                isShowingAd = false;
                                fetchAd(true);
                                if (resumeCallback != null) {
                                    resumeCallback.onAdClosed();
                                }
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                if (fullScreenContentCallback != null && enableScreenContentCallback) {
                                    fullScreenContentCallback.onAdFailedToShowFullScreenContent(adError);
                                }
                                if (resumeCallback != null) {
                                    resumeCallback.onAdFailedToShow(adError);
                                }
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                if (fullScreenContentCallback != null && enableScreenContentCallback) {
                                    fullScreenContentCallback.onAdShowedFullScreenContent();
                                }
                                isShowingAd = true;
                                splashAd = null;
                                if (resumeCallback != null) {
                                    resumeCallback.onAdLoaded();
                                }
                            }


                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                if (currentActivity != null) {
                                    FirebaseUtil.logClickAdsEvent(currentActivity, splashAdId);
                                    if (fullScreenContentCallback != null) {
                                        fullScreenContentCallback.onAdClicked();
                                    }
                                    if (resumeCallback != null) {
                                        resumeCallback.onAdClicked();
                                    }
                                }
                            }
                        });
                    splashAd.show(currentActivity);
                }

                if (currentActivity != null && !currentActivity.isDestroyed() && finalDialog != null && finalDialog.isShowing()) {
                    Log.d(TAG, "dismiss dialog loading ad open: ");
                    try {
                        finalDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 800);
        }
    }

    Dialog dialog = null;

    private void showResumeAds() {
        if (appResumeAd == null || currentActivity == null) {
            return;
        }
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            try {
                dismissDialogLoading();
                dialog = new ResumeLoadingDialog(currentActivity);
                try {
                    dialog.show();
                } catch (Exception e) {
                    if (fullScreenContentCallback != null && enableScreenContentCallback) {
                        fullScreenContentCallback.onAdDismissedFullScreenContent();

                    }
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            new Handler().postDelayed(() -> {
            if (appResumeAd != null) {
                appResumeAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        appResumeAd = null;
                        if (fullScreenContentCallback != null && enableScreenContentCallback) {
                            fullScreenContentCallback.onAdDismissedFullScreenContent();
                        }
                        isShowingAd = false;
                        fetchAd(false);
                        dismissDialogLoading();
                        Log.e(TAG, "onAdDismissedFullScreenContent: " );
                        if (resumeCallback != null) {
                            Log.e(TAG, "resumeCallback: " );
                            resumeCallback.onAdClosed();
                        }
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                        if (fullScreenContentCallback != null && enableScreenContentCallback) {
                            fullScreenContentCallback.onAdFailedToShowFullScreenContent(adError);
                        }

                        if (currentActivity != null && !currentActivity.isDestroyed() && dialog != null && dialog.isShowing()) {
                            Log.d(TAG, "dismiss dialog loading ad open: ");
                            try {
                                dialog.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        appResumeAd = null;
                        isShowingAd = false;
                        fetchAd(false);
                        if (resumeCallback != null) {
                            resumeCallback.onAdFailedToShow(adError);
                        }
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.e(TAG, "onAdShowedFullScreenContent: " );
                        if (fullScreenContentCallback != null && enableScreenContentCallback) {
                            fullScreenContentCallback.onAdShowedFullScreenContent();
                        }
                        isShowingAd = true;
                        appResumeAd = null;
                        if (resumeCallback != null) {
                            resumeCallback.onAdLoaded();
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (currentActivity != null) {
                            FirebaseUtil.logClickAdsEvent(currentActivity, appResumeAdId);
                            if (fullScreenContentCallback != null) {
                                fullScreenContentCallback.onAdClicked();
                            }
                            if (resumeCallback != null) {
                                resumeCallback.onAdClicked();
                            }
                        }
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        if (currentActivity != null) {
                            if (fullScreenContentCallback != null) {
                                fullScreenContentCallback.onAdImpression();
                            }
                            if (resumeCallback != null) {
                                resumeCallback.onAdImpression();
                            }
                        }
                    }
                });
                appResumeAd.show(currentActivity);
            }
//            }, 1000);
        }
    }

    @Override
    public void loadAndShowSplashAds(final String adId) {
        isTimeout = false;
        enableScreenContentCallback = true;
        if (currentActivity != null) {
            if (fullScreenContentCallback != null && enableScreenContentCallback) {
                fullScreenContentCallback.onAdDismissedFullScreenContent();
            }
            return;
        }

//        if (isAdAvailable(true)) {
//            showAdIfAvailable(true);
//            return;
//        }

        loadCallback =
            new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                    Log.d(TAG, "onAppOpenAdLoaded: splash");

                    timeoutHandler.removeCallbacks(runnableTimeout);

                    if (isTimeout) {
                        Log.e(TAG, "onAppOpenAdLoaded: splash timeout");
//                            if (fullScreenContentCallback != null) {
//                                fullScreenContentCallback.onAdDismissedFullScreenContent();
//                                enableScreenContentCallback = false;
//                            }
                    } else {
                        AppOpenManagerImpl.this.splashAd = appOpenAd;
                        splashLoadTime = new Date().getTime();
                        appOpenAd.setOnPaidEventListener(adValue -> {
                            FirebaseUtil.logPaidAdImpression(myApplication.getApplicationContext(),
                                adValue,
                                appOpenAd.getAdUnitId(),
                                AdType.APP_OPEN);
                        });

                        showAdIfAvailable(true);
                    }
                }

                /**
                 * Called when an app open ad has failed to load.
                 *
                 * @param loadAdError the error.
                 */
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.e(TAG, "onAppOpenAdFailedToLoad: splash " + loadAdError.getMessage());
                    if (isTimeout) {
                        Log.e(TAG, "onAdFailedToLoad: splash timeout");
                        return;
                    }
                    if (fullScreenContentCallback != null && enableScreenContentCallback) {
                        fullScreenContentCallback.onAdDismissedFullScreenContent();
                        enableScreenContentCallback = false;
                    }
                }

            };
        AdRequest request = getAdRequest();
        AppOpenAd.load(
            myApplication, splashAdId, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);

        if (splashTimeout > 0) {
            timeoutHandler = new Handler();
            timeoutHandler.postDelayed(runnableTimeout, splashTimeout);
        }
    }

    Runnable runnableTimeout = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "timeout load ad ");
            isTimeout = true;
            enableScreenContentCallback = false;
            if (fullScreenContentCallback != null) {
                fullScreenContentCallback.onAdDismissedFullScreenContent();
            }
        }
    };

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        if (!isAppResumeEnabled) {
            Log.d(TAG, "onResume: app resume is disabled");
            return;
        }

        if (isInterstitialShowing) {
            Log.d(TAG, "onResume: interstitial is showing");
            return;
        }

        if (disableAdResumeByClickAction) {
            Log.d(TAG, "onResume:ad resume disable ad by action");
            disableAdResumeByClickAction = false;
            return;
        }

        for (Class activity : disabledAppOpenList) {
            if (activity.getName().equals(currentActivity.getClass().getName())) {
                Log.d(TAG, "onStart: activity is disabled");
                return;
            }
        }

        if (splashActivity != null && splashActivity.getName().equals(currentActivity.getClass().getName())) {
            String adId = splashAdId;
            if (adId == null) {
                Log.e(TAG, "splash ad id must not be null");
            }
            Log.d(TAG, "onStart: load and show splash ads");
            loadAndShowSplashAds(adId);
            return;
        }

        if (isShowingAdResume) return;

        Log.d(TAG, "onStart: show resume ads :" + currentActivity.getClass().getName());
        loadAdResume();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        Log.d(TAG, "onStop: app stop");

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        Log.d(TAG, "onPause");
    }

    private void dismissDialogLoading() {
        if (dialog != null) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadAdResume() {
        if (appResumeAdId == null || appResumeAdId.isEmpty()) {
            return;
        }
        try {
            dismissDialogLoading();
            dialog = new ResumeLoadingDialog(currentActivity);
            try {
                dialog.show();
            } catch (Exception e) {
                if (fullScreenContentCallback != null && enableScreenContentCallback) {
                    fullScreenContentCallback.onAdDismissedFullScreenContent();
                }
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadCallback =
            new AppOpenAd.AppOpenAdLoadCallback() {

                @Override
                public void onAdLoaded(AppOpenAd ad) {
                    appResumeAd = ad;
                    ad.setOnPaidEventListener(adValue -> {
                        FirebaseUtil.logPaidAdImpression(currentActivity,
                            adValue,
                            ad.getAdUnitId(),
                            AdType.APP_OPEN);
                    });
                    appResumeLoadTime = (new Date()).getTime();
                    showResumeAdsNew();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    fullScreenContentCallbackNew.onAdFailedToShowFullScreenContent(loadAdError);
                    dismissDialogLoading();
                }


            };
        AdRequest request = getAdRequest();

        AppOpenAd.load(
            myApplication, appResumeAdId, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);

    }


    private void showResumeAdsNew() {
        if (appResumeAd == null || currentActivity == null) {
            return;
        }
        appResumeAd.setFullScreenContentCallback(fullScreenContentCallbackNew);
        isShowingAd = true;
        isShowingAdResume = true;
        appResumeAd.show(currentActivity);

    }

    public FullScreenContentCallback fullScreenContentCallbackNew = new FullScreenContentCallback() {
        @Override
        public void onAdDismissedFullScreenContent() {
            // Set the reference to null so isAdAvailable() returns false.
            Log.e(TAG, "onAdDismissedFullScreenContent: ");
            isShowingAd = false;
            isShowingAdResume = false;
            dismissDialogLoading();
            if (resumeCallback != null) {
                resumeCallback.onAdClosed();
            }
        }

        @Override
        public void onAdFailedToShowFullScreenContent(AdError adError) {
            Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
            isShowingAd = false;
            isShowingAdResume = false;
            dismissDialogLoading();
            if (resumeCallback != null) {
                resumeCallback.onAdFailedToShow(adError);
            }
        }

        @Override
        public void onAdShowedFullScreenContent() {
            if (resumeCallback != null) {
                resumeCallback.onAdLoaded();
            }
            Log.e(TAG, "onAdShowedFullScreenContent: ");
        }

        @Override
        public void onAdClicked() {
            super.onAdClicked();
            if (currentActivity != null) {
                FirebaseUtil.logClickAdsEvent(currentActivity, appResumeAdId);
                if (fullScreenContentCallback != null) {
                    fullScreenContentCallback.onAdClicked();
                }
                if (resumeCallback != null) {
                    resumeCallback.onAdClicked();
                }
            }
        }

        @Override
        public void onAdImpression() {
            super.onAdImpression();
            if (currentActivity != null) {
                if (fullScreenContentCallback != null) {
                    fullScreenContentCallback.onAdImpression();
                }
                super.onAdImpression();
                if (resumeCallback != null) {
                    resumeCallback.onAdImpression();
                }
            }
        }
    };

    @Override
    public void showAppOpenSplash(Context context, AdCallback adCallback) {
        if (this.splashAd == null) {
            adCallback.onNextAction();
            adCallback.onAdFailedToLoad(null);
        } else {
            try {
                dialog = null;
                dialog = new LoadingAdsDialog(context);
                dialog.show();
            } catch (Exception e) {
            }
            /*this.dismissDialogLoading();
            if (this.dialog == null) {
                try {
                    LoadingAdsDialog dialog = new LoadingAdsDialog(context);
                    dialog.setCancelable(false);
                    this.dialog = dialog;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                if (this.dialog != null) {
                    this.dialog.show();
                }
            } catch (Exception e) {}*/

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (AppOpenManagerImpl.this.splashAd != null) {
                        AppOpenManagerImpl.this.splashAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                adCallback.onNextAction();
                                adCallback.onAdClosed();
                                AppOpenManagerImpl.this.splashAd = null;
                                AppOpenManagerImpl.isShowingAd = false;
                                isShowLoadingSplash = false;
                                if (dialog != null && !AppOpenManagerImpl.this.currentActivity.isDestroyed()) {
                                    try {
                                        dialog.dismiss();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                isShowLoadingSplash = true;
                                // adCallback.onNextAction();
                                adCallback.onAdFailedToShow(adError);
                                AppOpenManagerImpl.isShowingAd = false;
                                AppOpenManagerImpl.this.dismissDialogLoading();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                adCallback.onAdImpression();
                                AppOpenManagerImpl.isShowingAd = true;
                                AppOpenManagerImpl.this.splashAd = null;
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                adCallback.onAdClicked();
                            }
                        });

                        AppOpenManagerImpl.this.splashAd.show(AppOpenManagerImpl.this.currentActivity);
                    }
                }
            }, 800L);
        }
    }

    @Override
    public void loadOpenAppAdSplash(Context context, String idResumeSplash, long timeDelay, long timeOut, boolean isShowAdIfReady, AdCallback adCallback) {
        this.splashAdId = idResumeSplash;
        if (!isNetworkConnected(context)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    adCallback.onAdFailedToLoad(null);
                    adCallback.onNextAction();
                }
            }, timeDelay);
        } else {
            if (AppPurchase.getInstance().isPurchased(context)) {
                adCallback.onNextAction();
            } else {
                long currentTimeMillis = System.currentTimeMillis();
                Runnable timeOutRunnable = () -> {
                    Log.d("AppOpenManager", "getAdSplash time out");
                    adCallback.onNextAction();
                    isShowingAd = false;
                };
                Handler handler = new Handler();
                handler.postDelayed(timeOutRunnable, timeOut);
                AdRequest adRequest = getAdRequest();
                String adUnitId = this.splashAdId;
                AppOpenAd.AppOpenAdLoadCallback appOpenAdLoadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        handler.removeCallbacks(timeOutRunnable);
                        adCallback.onAdFailedToLoad(null);
                        adCallback.onNextAction();
                    }

                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                        super.onAdLoaded(appOpenAd);
                        handler.removeCallbacks(timeOutRunnable);
                        AppOpenManagerImpl.this.splashAd = appOpenAd;
                        AppOpenManagerImpl.this.splashAd.setOnPaidEventListener((adValue) -> {
                            //log value
                        });
                        appOpenAd.setOnPaidEventListener(adValue -> {
                            FirebaseUtil.logPaidAdImpression(myApplication.getApplicationContext(),
                                adValue,
                                appOpenAd.getAdUnitId(),
                                AdType.APP_OPEN);
                        });
                        if (isShowAdIfReady) {
                            long elapsedTime = System.currentTimeMillis() - currentTimeMillis;
                            if (elapsedTime >= timeDelay) {
                                elapsedTime = 0L;
                            }
                            Handler handler1 = new Handler();
                            Context appOpenAdContext = context;
                            Runnable showAppOpenSplashRunnable = () -> {
                                AppOpenManagerImpl.this.showAppOpenSplash(appOpenAdContext, adCallback);
                            };
                            handler1.postDelayed(showAppOpenSplashRunnable, elapsedTime);
                        } else {
                            adCallback.onAdSplashReady();
                        }
                    }
                };
                AppOpenAd.load(context, adUnitId, adRequest, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, appOpenAdLoadCallback);
            }
        }

    }

    @Override
    public void loadOpenAppAdSplashFloor(Context context, List<String> listIDResume, boolean isShowAdIfReady, AdCallback adCallback) {
        if (!isNetworkConnected(context)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    adCallback.onAdFailedToLoad(null);
                    adCallback.onNextAction();
                }
            }, 3000);
        } else {
            if (listIDResume == null) {
                Log.e("AppOpenManager", "load ID :" + "null");
                adCallback.onAdFailedToLoad(null);
                adCallback.onNextAction();
                return;
            }
            if (listIDResume.size() > 0) {
                Log.e("AppOpenManager", "load ID :" + listIDResume.get(0));
            }
            if (listIDResume.size() < 1) {
                adCallback.onAdFailedToLoad(null);
                adCallback.onNextAction();
            }
            if (AppPurchase.getInstance().isPurchased(context) || listIDResume.size() < 1) {
                adCallback.onNextAction();
            } else {
                AdRequest adRequest = getAdRequest();
                AppOpenAd.AppOpenAdLoadCallback appOpenAdLoadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        // adCallback.onAdFailedToLoad(loadAdError);
                        listIDResume.remove(0);
                        if (listIDResume.size() == 0) {
                            adCallback.onAdFailedToLoad(null);
                            adCallback.onNextAction();
                        } else {
                            loadOpenAppAdSplashFloor(context, listIDResume, isShowAdIfReady, adCallback);
                        }
                    }

                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                        super.onAdLoaded(appOpenAd);
                        AppOpenManagerImpl.this.splashAd = appOpenAd;
                        AppOpenManagerImpl.this.splashAd.setOnPaidEventListener((adValue) -> {
                            FirebaseUtil.logPaidAdImpression(myApplication.getApplicationContext(),
                                adValue,
                                appOpenAd.getAdUnitId(),
                                AdType.APP_OPEN);
                        });
                        if (isShowAdIfReady) {
                            AppOpenManagerImpl.this.showAppOpenSplash(context, adCallback);
                        } else {
                            adCallback.onAdSplashReady();
                        }
                    }
                };
                AppOpenAd.load(context, listIDResume.get(0), adRequest, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, appOpenAdLoadCallback);
            }
        }
    }

    @Override
    public void onCheckShowSplashWhenFail(Activity activity, AdCallback callback, int timeDelay) {
        (new Handler(activity.getMainLooper())).postDelayed(new Runnable() {
            public void run() {
                if (AppOpenManagerImpl.this.splashAd != null && !AppOpenManagerImpl.isShowingAd) {
                    Log.e("AppOpenManager", "show ad splash when show fail in background");
                    AppOpenManagerImpl.getInstance().showAppOpenSplash(activity, callback);
                }

            }
        }, (long) timeDelay);
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}

