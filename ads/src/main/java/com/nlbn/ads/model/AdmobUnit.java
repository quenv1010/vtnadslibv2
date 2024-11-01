package com.nlbn.ads.model;

import com.google.android.gms.ads.interstitial.InterstitialAd;

import java.util.List;

public class AdmobUnit {
    public String keyAds;
    public String idAds;
    public List<String> listIdAds;

    public InterstitialAd mInterstitialAd;

    public AdmobUnit() {
    }

    public InterstitialAd getmInterstitialAd() {
        return mInterstitialAd;
    }

    public void setmInterstitialAd(InterstitialAd mInterstitialAd) {
        this.mInterstitialAd = mInterstitialAd;
    }

    public String getKeyAds() {
        return keyAds;
    }

    public void setKeyAds(String keyAds) {
        this.keyAds = keyAds;
    }

    public String getIdAds() {
        return idAds;
    }

    public void setIdAds(String idAds) {
        this.idAds = idAds;
    }

    public List<String> getListIdAds() {
        return listIdAds;
    }

    public void setListIdAds(List<String> listIdAds) {
        this.listIdAds = listIdAds;
    }
}
