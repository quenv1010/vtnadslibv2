<h2>VTNAdsLibrary</h2>
<h3>*build.gradle() - Project</h3>

    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }

<h3>*build.gradle() - App</h3>

    dependencies {
        //ads
        implementation 'com.github.devvtn:vtn_ads_libs:Tag'
        implementation 'com.google.android.gms:play-services-ads:22.2.0'
        //multidex
        implementation "androidx.multidex:multidex:2.0.1"
        implementation 'com.facebook.shimmer:shimmer:0.5.0'
    }

<h3>*In Manifest:</h3>

    <uses-permission android:name="com.google.android.gms.permission.AD_ID"/>
    ...
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="@string/app_id" />

<h3>Add ads with remote config</h3>
<h3>-Create key in file AdsConfig</h3>

    public class AdsConfig {
        public static String KEY_INTER_SPLASH = "inter_splash";
        public static String KEY_AD_APP_RESUME_ID = "app_open_resume";
        public static String KEY_NATIVE_INTRO_1 = "native_intro_1";
        ....
    }

<h3>-In Applicaton</h3>

    class Application : AdsApplication() {
        override fun onCreate() {
            super.onCreate()
            ...
        }
        override fun enableAdsResume(): Boolean {
            return true
        }
        //optional (Chỉ bật khi sử dụng remoteConfig cho ad resume )
        override fun enableRemoteAdsResume(): Boolean {
            return true
        }
        //optional
        override fun getKeyRemoteAdsResume(): String {
            return AdsConfig.key
        }

        override fun getDefaultsAsyncFirebase(): Int {
            return R.xml.remote_config_defaults
        }

        override fun getListTestDeviceId(): MutableList<String>? {
            return null
        }

        override fun getResumeAdId(): String {
            return ""
        }

        override fun buildDebug(): Boolean {
            return false
        }
    }

<h3>Ads Splash</h3>


    var adCallback: AdCallback? = null
    var adSplashConfig: AdSplashConfig? = null

    adCallback = object : AdCallback() {
        override fun onNextAction() {
            super.onNextAction()
            ...
        }
    }

    adSplashConfig = AdSplashConfig.Builder()
        .setKey(AdsConfig.key)
        .setAdSplashType(AdSplashType.SPLASH_INTER) //SPLASH_INTER, SPLASH_OPEN, SPLASH_INTER_FLOOR, SPLASH_OPEN_FLOOR
        .setTimeOut(15000)
        .setTimeDelay(3000)
        .setShowAdIfReady(true)
        .setCallback(adCallback)
        .build()

    RemoteAdmob.getInstance().loadAdSplashWithConfig(this, adSplashConfig)

    override fun onStop() {
        super.onStop()
        RemoteAdmob.getInstance().dismissLoadingDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        RemoteAdmob.getInstance().dismissLoadingDialog()
    }

    override fun onResume() {
        super.onResume()
        RemoteAdmob.getInstance().onCheckShowSplashWhenFailWithConfig(this, adSplashConfig, 1000)
    }

<h3>Ads banner</h3>

    val adBannerConfig = AdBannerConfig.Builder()
            .setKey(AdsConfig.key)
            .setBannerType(AdBannerType.BANNER) //BANNER, BANNER_COLLAPSE
            .setView(findViewById(R.id.banner))
            .setGravity(BannerGravity.bottom)
            .build()
    RemoteAdmob.getInstance().loadBannerWithConfig(this, adBannerConfig)


<h3>Ads Native</h3>

    val adNativeConfig = AdNativeConfig.Builder()
        .setKey(AdsConfig.key) //with native floor .setKey(AdsConfig.key_1, AdsConfig.key_2)
        .setNativeType(AdNativeType.NATIVE) //NATIVE, NATIVE_FLOOR
        .setLayout(R.layout.layout_native_language)
        .setView(findViewById(R.id.native_ads))
        .build()

    RemoteAdmob.getInstance().loadNativeWithConfig(this,adNativeConfig,false) //false when ads Gone, True when ads Invisible

    //load native with callback
    RemoteAdmob.getInstance()
            .loadNativeWithConfigCallback(this, adNativeConfig, false, object : NativeCallback() {
                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                    super.onNativeAdLoaded(nativeAd)
                    val adView = LayoutInflater.from(this)
                        .inflate(adNativeConfig.layout, null) as NativeAdView
                    adNativeConfig.view.removeAllViews()
                    adNativeConfig.view.addView(adView)
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView)
                }

                override fun onAdFailedToLoad() {
                    super.onAdFailedToLoad()
                    adNativeConfig.view.removeAllViews()
                }
            })

<h3>Ads Inter</h3>

    //load inter
    RemoteAdmob.getInstance().loadInterWithKey(this, AdsConfig.key, object : AdCallback() {
    override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
        super.onInterstitialLoad(interstitialAd)
        mInterstitialAd = interstitialAd
        }
    })

    //show inter
    val adInterConfig = AdInterConfig.Builder()
            .setKey(AdsConfig.key)
            .setInterstitialAd(mInterstitialAd)
            .setCallback(object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    ...
                    //load inter
                }
            })
            .build()

    RemoteAdmob.getInstance().showInterWithConfig(this, adInterConfig)

<h3>Ads Reward</h3>

    var adRewardConfig: AdRewardConfig? = null

    adRewardConfig = AdRewardConfig.Builder()
            .setKey(AdsConfig.key_ad_app_reward_id)
            .setRewardCallback(object : RewardCallback {
                override fun onEarnedReward(rewardItem: RewardItem) {
                    //success
                }

                override fun onAdClosed() {
                    //close
                }

                override fun onAdFailedToShow(codeError: Int) {
                    //failed
                }
            })
            .build()  
            
    RemoteAdmob.getInstance().initRewardWithConfig(this, adRewardConfig)
    
    RemoteAdmob.getInstance().showRewardWithConfig(this, adRewardConfig)


