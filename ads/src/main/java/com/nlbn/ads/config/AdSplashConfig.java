package com.nlbn.ads.config;

import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.adstype.AdSplashType;

public class AdSplashConfig {
    public AdSplashType adSplashType;
    public AdCallback callback;
    public String key;
    public long timeDelay;
    public long timeOut;
    public boolean isShowAdIfReady;

    public AdSplashConfig(Builder builder) {
        this.adSplashType = builder.adSplashType;
        this.callback = builder.callback;
        this.key = builder.key;
        this.timeDelay = builder.timeDelay;
        this.timeOut = builder.timeOut;
        this.isShowAdIfReady = builder.isShowAdIfReady;
    }



    public AdSplashType getAdSplashType() {
        return adSplashType;
    }

    public AdCallback getCallback() {
        return callback;
    }

    public String getKey() {
        return key;
    }

    public long getTimeDelay() {
        return timeDelay;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public boolean isShowAdIfReady() {
        return isShowAdIfReady;
    }

    public static class Builder{
        private AdSplashType adSplashType;
        private AdCallback callback;
        private String key;
        private long timeDelay;
        private long timeOut;
        private boolean isShowAdIfReady;

        public Builder() {
        }

        public Builder setAdSplashType(AdSplashType adSplashType) {
            this.adSplashType = adSplashType;
            return this;
        }

        public Builder setCallback(AdCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder setTimeDelay(long timeDelay) {
            this.timeDelay = timeDelay;
            return this;
        }

        public Builder setTimeOut(long timeOut) {
            this.timeOut = timeOut;
            return this;
        }

        public Builder setShowAdIfReady(boolean showAdIfReady) {
            isShowAdIfReady = showAdIfReady;
            return this;
        }

        public AdSplashConfig build(){
            return new AdSplashConfig(this);
        }
    }
}
