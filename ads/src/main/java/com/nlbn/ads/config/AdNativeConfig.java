package com.nlbn.ads.config;

import android.view.ViewGroup;

import com.nlbn.ads.adstype.AdNativeType;

public class AdNativeConfig {
    public String[] key;
    public AdNativeType adNativeType;
    public int layout;
    public ViewGroup view;

    public AdNativeConfig(Builder builder) {
        this.key = builder.key;
        this.adNativeType = builder.adNativeType;
        this.layout = builder.layout;
        this.view = builder.view;
    }

    public ViewGroup getView() {
        return view;
    }

    public String[] getKey() {
        return key;
    }

    public AdNativeType getAdNativeType() {
        return adNativeType;
    }

    public int getLayout() {
        return layout;
    }

    public static class Builder{
        private String[] key;
        private AdNativeType adNativeType;
        private int layout;
        private ViewGroup view;

        public Builder setKey(String... key){
            this.key = key;
            return this;
        }
        public Builder setNativeType(AdNativeType adNativeType){
            this.adNativeType = adNativeType;
            return this;
        }

        public Builder setLayout(int layout){
            this.layout = layout;
            return this;
        }
        public Builder setView(ViewGroup view){
            this.view = view;
            return this;
        }

        public AdNativeConfig build(){
            return new AdNativeConfig(this);
        }
    }
}
