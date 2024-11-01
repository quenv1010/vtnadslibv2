package com.nlbn.ads.config;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.nlbn.ads.callback.AdCallback;

public class AdInterConfig {
    public String key;
    public InterstitialAd mInterstitialAd;
    public AdCallback callback;

    public AdInterConfig(Builder builder) {
        this.key = builder.key;
        this.callback = builder.callback;
        this.mInterstitialAd = builder.mInterstitialAd;
    }

    public String getKey() {
        return key;
    }

    public AdCallback getCallback() {
        return callback;
    }
    public static class Builder{
        private String key;
        private AdCallback callback;
        private InterstitialAd mInterstitialAd;

        public Builder() {
        }
        public Builder setKey(String key){
            this.key = key;
            return this;
        }
        public Builder setInterstitialAd(InterstitialAd mInterstitialAd){
            this.mInterstitialAd = mInterstitialAd;
            return this;
        }

        public Builder setCallback(AdCallback callback){
            this.callback = callback;
            return this;
        }
        public AdInterConfig build(){
            return new AdInterConfig(this);
        }

    }
}
