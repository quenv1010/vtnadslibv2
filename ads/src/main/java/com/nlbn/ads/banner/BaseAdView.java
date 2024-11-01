package com.nlbn.ads.banner;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;

import com.google.android.gms.ads.nativead.NativeAdView;
import com.nlbn.ads.nativeadvance.NativeAdmobView;


public abstract class BaseAdView extends FrameLayout {

    private long nextRefreshTime = 0L;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());

    private boolean isPausedOrDestroy = false;
    private final int refreshRateSec;
    private final ViewGroup shimmer;

    public BaseAdView(Context context, Integer refreshRateSec, ViewGroup shimmer) {
        super(context);
        this.refreshRateSec = refreshRateSec;
        this.shimmer = shimmer;
    }

    public void loadAd() {
        log("LoadAd ...");
        nextRefreshTime = 0L;
        stopBannerRefreshScheduleIfNeed();

        loadAdInternal(() -> {
            if (shimmer != null) {
                shimmer.removeAllViews();
                shimmer.setVisibility(View.GONE);
            }
            log("On load ad done ...");
            calculateNextBannerRefresh();
            if (!isPausedOrDestroy) {
                scheduleNextBannerRefreshIfNeed();
            }
        });
    }

    protected abstract void loadAdInternal(Runnable onDone);

    @CallSuper
    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (isVisible) {
            onResume();
        } else {
            onPause();
        }
    }

    @CallSuper
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onDestroy();
    }

    private void onResume() {
        isPausedOrDestroy = false;
        scheduleNextBannerRefreshIfNeed();
    }

    private void onPause() {
        isPausedOrDestroy = true;
        stopBannerRefreshScheduleIfNeed();
    }

    private void onDestroy() {
        isPausedOrDestroy = true;
        stopBannerRefreshScheduleIfNeed();
    }

    private void calculateNextBannerRefresh() {
        nextRefreshTime = System.currentTimeMillis() + refreshRateSec * 1000L;
    }

    private void scheduleNextBannerRefreshIfNeed() {
        if (nextRefreshTime <= 0L) {
            return;
        }

        long delay = Math.max(0L, nextRefreshTime - System.currentTimeMillis());

        stopBannerRefreshScheduleIfNeed();
        log("Ads are scheduled to show in " + delay + " mils");
        refreshHandler.postDelayed(this::loadAd, delay);
    }

    private void stopBannerRefreshScheduleIfNeed() {
        refreshHandler.removeCallbacksAndMessages(null);
    }

    // Assume log method is defined somewhere
    private void log(String message) {
        Log.d("Banner Plugin", message);
    }

    public static class Factory {

        public static BaseAdView getAdView(
                Activity activity,
                String adUnitId,
                BannerPlugin.BannerType bannerType,
                int refreshRateSec,
                int cbFetchIntervalSec,
                ViewGroup shimmer
        ) {

            switch (bannerType) {
                case Adaptive:
                case Standard:
                case LargeBanner:
                case CollapsibleBottom:
                case CollapsibleTop:
                    return new BannerAdView(
                            activity,
                            adUnitId,
                            bannerType,
                            refreshRateSec,
                            cbFetchIntervalSec,
                            shimmer
                    );

                default:
                    throw new IllegalArgumentException("Unsupported banner type: " + bannerType);
            }
        }

        public static BaseAdView getNativeAdView(
                Context context, String adUnitId, int refreshRateSec, NativeAdView nativeAdView, ViewGroup shimmer) {
            return new NativeAdmobView(context, adUnitId, refreshRateSec, nativeAdView, shimmer);
        }

    }
}
