package com.nlbn.ads.util;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.callback.NativeCallback;
import com.nlbn.ads.config.AdBannerConfig;
import com.nlbn.ads.config.AdInterConfig;
import com.nlbn.ads.config.AdNativeConfig;
import com.nlbn.ads.config.AdRewardConfig;
import com.nlbn.ads.config.AdSplashConfig;

import java.util.List;

public abstract class RemoteAdmob {
    public static RemoteAdmob getInstance() {
        return RemoteAdmobImpl.getInstance();
    }

    public abstract String getIdAdsWithKey(String key);

    public abstract List<String> getListIdAdsWithKey(String... keys);

    public abstract void onCheckShowSplashWhenFailWithConfig(final Activity activity, AdSplashConfig config, int timeDelay);

    public abstract void loadAdSplashWithConfig(final Context context, AdSplashConfig config);

    public abstract void dismissLoadingDialog();

    public abstract void loadBannerWithConfig(Activity activity, AdBannerConfig adBannerConfig);

    public abstract void loadNativeWithConfig(Context context, AdNativeConfig adNativeConfig, boolean isInvisible);

    public abstract void loadNativeWithConfigCallback(Context context, AdNativeConfig adNativeConfig, boolean isInvisible, NativeCallback nativeCallback);

    public abstract void loadInterWithKey(Context context, String key, AdCallback adCallback);

    public abstract void showInterWithConfig(Context context, AdInterConfig adInterConfig);

    public abstract void initRewardWithConfig(Context context, AdRewardConfig adRewardConfig);

    public abstract void showRewardWithConfig(Activity context, AdRewardConfig adRewardConfig);

    public abstract void disableAppResume();

    public abstract void enableAppResume();

    public abstract void disableAppResumeWithActivity(Class activityClass);

    public abstract void enableAppResumeWithActivity(Class activityClass);
}
