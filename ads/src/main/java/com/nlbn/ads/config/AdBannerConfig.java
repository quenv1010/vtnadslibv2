package com.nlbn.ads.config;

import android.view.ViewGroup;

import com.nlbn.ads.adstype.AdBannerType;

public class AdBannerConfig {
    public String key;
    public AdBannerType bannerType;
    public String gravity;
    public ViewGroup view;

    public AdBannerConfig(Builder builder) {
        this.key = builder.key;
        this.gravity = builder.gravity;
        this.bannerType = builder.bannerType;
        this.view = builder.view;
    }

    public String getKey() {
        return key;
    }



    public String getGravity() {
        return gravity;
    }

    public AdBannerType getBannerType() {
        return bannerType;
    }

    public ViewGroup getView() {
        return view;
    }

    public static class Builder {
        private String key;

        private AdBannerType bannerType;
        private String gravity;
        private ViewGroup view;

        public Builder() {
        }
        public Builder setKey(String key){
            this.key = key;
            return this;
        }

        public Builder setGravity(String gravity){
            this.gravity = gravity;
            return this;
        }
        public Builder setBannerType(AdBannerType bannerType){
            this.bannerType = bannerType;
            return this;
        }
        public Builder setView(ViewGroup view){
            this.view = view;
            return this;
        }




        public AdBannerConfig build(){
            return new AdBannerConfig(this);
        }
    }
}
