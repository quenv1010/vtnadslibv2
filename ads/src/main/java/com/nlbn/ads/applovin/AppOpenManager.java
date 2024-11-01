package com.nlbn.ads.applovin;

import android.app.Application;

public abstract class AppOpenManager {
    public static AppOpenManager getInstance() {
        return AppOpenManagerImpl.getInstance();
    }

    public abstract void init(Application application, String appResumeId);

    public abstract void enableAppResumeWithActivity(Class activityClass);

    public abstract void disableAppResumeWithActivity(Class activityClass);
    public abstract boolean isInitialize();

    public abstract void disableAppResume();

    public abstract void enableAppResume();

    public abstract void setAppResumeAdId(String appResumeAdId);

    public abstract boolean isInterstitialShowing();

    public abstract void setInterstitialShowing(boolean interstitialShowing);
}
