package com.vtnadslibrary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.nlbn.ads.adstype.AdBannerType;
import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.config.AdBannerConfig;
import com.nlbn.ads.util.Admob;
import com.nlbn.ads.util.RemoteAdmob;

public class Fragment1 extends Fragment {
    Button btnclick;
    InterstitialAd mInterstitialAd;
    AdBannerConfig adBannerConfig;
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment1,container,false);
        loadBanner(view);
        return view;

    }

    public void loadBanner(View view){
        adBannerConfig = new AdBannerConfig.Builder()
                .setKey(AdsConfig.key_ad_banner_id)
                .setBannerType(AdBannerType.BANNER)
                .setView(view.findViewById(R.id.include))
                .build();
        RemoteAdmob.getInstance().loadBannerWithConfig(requireActivity(),adBannerConfig);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Admob.getInstance().loadInterAds(getContext(),getString(R.string.admod_interstitial_id), new AdCallback(){
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                mInterstitialAd = interstitialAd;
            }
        });
        btnclick  = view.findViewById(R.id.btnclick);
        btnclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admob.getInstance().showInterAds(getActivity(),mInterstitialAd,new AdCallback(){
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        ((MainActivity2)getActivity()).showFragment(new Fragment2(),"BlankFragment2");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        ((MainActivity2)getActivity()).showFragment(new Fragment2(),"BlankFragment2");
                    }
                });
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }
}
