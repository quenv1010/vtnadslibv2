package com.nlbn.ads.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.nlbn.ads.callback.AdCallback;

import java.util.List;

public abstract class AppOpenManager {
    public static AppOpenManager getInstance() {
        return AppOpenManagerImpl.getInstance();
    }

    public abstract void init(Application application);

    public abstract boolean isInitialized();

    public abstract void setInitialized(boolean initialized);

    public abstract void setEnableScreenContentCallback(boolean enableScreenContentCallback);

    public abstract boolean isInterstitialShowing();

    public abstract void setInterstitialShowing(boolean interstitialShowing);

    public abstract void disableAdResumeByClickAction();

    public abstract void setDisableAdResumeByClickAction(boolean disableAdResumeByClickAction);

    public abstract boolean isShowingAd();

    public abstract void disableAppResumeWithActivity(Class activityClass);

    public abstract void enableAppResumeWithActivity(Class activityClass);

    public abstract void disableAppResume();

    public abstract void enableAppResume();

    public abstract void setSplashActivity(Class splashActivity, String adId, int timeoutInMillis);

    public abstract void setAppResumeAdId(String appResumeAdId);

    public abstract void setFullScreenContentCallback(FullScreenContentCallback callback);

    public abstract void removeFullScreenContentCallback();

    public abstract void loadAndShowSplashAds(final String adId);

    public abstract void showAppOpenSplash(Context context, AdCallback adCallback);

    public abstract void loadOpenAppAdSplash(Context context, String idResumeSplash, long timeDelay, long timeOut, boolean isShowAdIfReady, AdCallback adCallback);

    public abstract void loadOpenAppAdSplashFloor(Context context, List<String> listIDResume, boolean isShowAdIfReady, AdCallback adCallback);

    public abstract void onCheckShowSplashWhenFail(final Activity activity, final AdCallback callback, int timeDelay);

    public abstract void setResumeCallback(AdCallback callback);
}
