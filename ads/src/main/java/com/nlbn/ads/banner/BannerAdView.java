package com.nlbn.ads.banner;


import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.nlbn.ads.util.AdType;
import com.nlbn.ads.util.FirebaseUtil;

public class BannerAdView extends BaseAdView {

    private long lastCBRequestTime = 0L;
    private final AdView adView;
    private boolean hasSetAdSize = false;
    private final BannerPlugin.BannerType bannerType;
    private Activity activity;
    private long cbFetchIntervalSec;

    public BannerAdView(
            Activity activity,
            String adUnitId,
            BannerPlugin.BannerType bannerType,
            int refreshRateSec,
            int cbFetchIntervalSec,
            ViewGroup shimmer
    ) {
        super(activity, refreshRateSec, shimmer);
        this.bannerType = bannerType;
        adView = new AdView(activity);
        adView.setAdUnitId(adUnitId);
        addView(adView, getCenteredLayoutParams(this));
        this.activity = activity;
        this.cbFetchIntervalSec = cbFetchIntervalSec;
    }

    private ViewGroup.LayoutParams getCenteredLayoutParams(ViewGroup container) {
        if (container instanceof FrameLayout) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;
            return params;
        } else if (container instanceof LinearLayout) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;
            return params;
        } else {
            return new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    protected void loadAdInternal(Runnable onDone) {
        if (!hasSetAdSize) {
            adView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    adView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    AdSize adSize = getAdSize(bannerType);
                    adView.setAdSize(adSize);
                    ViewGroup.LayoutParams params = adView.getLayoutParams();
                    params.width = adSize.getWidthInPixels(activity);
                    params.height = adSize.getHeightInPixels(activity);
                    adView.setLayoutParams(params);
                    hasSetAdSize = true;
                    doLoadAd(onDone);
                }
            });
        } else {
            doLoadAd(onDone);
        }
    }

    private AdSize getAdSize(BannerPlugin.BannerType bannerType) {
        switch (bannerType) {
            case Standard:
                return AdSize.BANNER;
            case LargeBanner:
                return AdSize.MEDIUM_RECTANGLE;
            case Adaptive:
            case CollapsibleBottom:
            case CollapsibleTop:
                return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, getAdWidthInDp());
        }
        return AdSize.BANNER;
    }

    private int getAdWidthInDp() {
        float adWidthPx = getWidth();
        if (adWidthPx == 0f) {
            adWidthPx = getResources().getDisplayMetrics().widthPixels;
        }
        float density = getResources().getDisplayMetrics().density;
        return (int) (adWidthPx / density);
    }

    private void doLoadAd(Runnable onDone) {
        boolean isCollapsibleBannerRequest = false;

        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        switch (bannerType) {
            case CollapsibleTop:
            case CollapsibleBottom:
                if (shouldRequestCollapsible()) {
                    String position = (bannerType == BannerPlugin.BannerType.CollapsibleTop) ? "top" : "bottom";
                    Bundle bundle = new Bundle();
                    bundle.putString("collapsible", position);
                    adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, bundle);
                    isCollapsibleBannerRequest = true;
                }
                break;
        }

        if (isCollapsibleBannerRequest) {
            lastCBRequestTime = System.currentTimeMillis();
        }

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setAdListener(new AdListener() {
                });
                onDone.run();
                adView.setOnPaidEventListener(adValue -> FirebaseUtil.logPaidAdImpression(
                        getContext(),
                        adValue,
                        adView.getAdUnitId(),
                        AdType.BANNER
                ));
            }

            @Override
            public void onAdFailedToLoad(LoadAdError p0) {
                adView.setAdListener(new AdListener() {
                });
                onDone.run();
            }
        });

        adView.loadAd(adRequestBuilder.build());
    }

    private boolean shouldRequestCollapsible() {
        return System.currentTimeMillis() - lastCBRequestTime >= cbFetchIntervalSec * 1000L;
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (isVisible) {
            adView.resume();
        } else {
            adView.pause();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        adView.setAdListener(new AdListener() {
        });
        adView.destroy();
    }
}