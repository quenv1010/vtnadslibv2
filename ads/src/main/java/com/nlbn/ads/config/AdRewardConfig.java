package com.nlbn.ads.config;

import com.nlbn.ads.callback.RewardCallback;

public class AdRewardConfig {
    public String key;
    public RewardCallback rewardCallback;


    public AdRewardConfig(Builder builder) {
        this.key = builder.key;
        this.rewardCallback = builder.rewardCallback;
    }

    public String getKey() {
        return key;
    }

    public RewardCallback getRewardCallback() {
        return rewardCallback;
    }

    public static class Builder{
        private String key;
        private RewardCallback rewardCallback;

        public Builder() {
        }
        public Builder setKey(String key){
            this.key = key;
            return this;
        }
        public Builder setRewardCallback(RewardCallback rewardCallback){
            this.rewardCallback = rewardCallback;
            return this;
        }

        public AdRewardConfig build(){
            return new AdRewardConfig(this);
        }
    }
}
