# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-adaptclassstrings
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepnames class com.facebook.FacebookActivity
-keepnames class com.facebook.CustomTabActivity

-keep class com.facebook.login.Login
-keep class com.facebook.ads.** { *; }
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

-dontwarn com.appsflyer.AFKeystoreWrapper
-keep class com.appsflyer.**
-keepnames class com.nlbn.ads.**

-keep class com.nlbn.ads.util.Admob {public  *;}
-keep class com.nlbn.ads.util.RemoteAdmob {public  *;}
-keep class com.nlbn.ads.util.AppFlyer {public  *;}
-keep class com.nlbn.ads.util.Adjust {public  *;}
-keep class com.nlbn.ads.util.CommonFirebase {public  *;}
-keep class com.nlbn.ads.util.BannerGravity {public  *;}
-keep class com.nlbn.ads.billing.AppPurchase {public  *;}
-keep class com.nlbn.ads.util.AdsApplication {public protected  *;}
-keep class com.nlbn.ads.util.AdsMultiDexApplication {public protected  *;}
-keep class com.nlbn.ads.util.AppOpenManager {public protected  *;}
-keep class com.nlbn.ads.util.ConsentHelper {public protected  *;}
-keep class com.nlbn.ads.rate.** {public  *;}
-keep class com.nlbn.ads.config.** {public *;}
-keep class com.nlbn.ads.adstype.** {public *;}
-keep class com.nlbn.ads.callback.** {public *;}
-keep class com.nlbn.ads.banner.** {public *;}
-keep class com.nlbn.ads.nativeadvance.** {public *;}
-keep class com.nlbn.ads.applovin.ApplovinApplication {public protected *;}
-keep class com.nlbn.ads.applovin.AppLovin {public protected *;}
-keep class com.nlbn.ads.applovin.AppOpenManager  {public protected *;}
-keep class com.nlbn.ads.applovin.OnShowConsentComplete  {public protected *;}
-keep class com.nlbn.ads.util.OnAdjustAttributionChangedListener  {public protected *;}
-keep class com.nlbn.ads.util.AdjustAttribution  {public protected *;}
-keep class com.nlbn.ads.util.AdjustNetworkInfo  {public private protected *;}
-keep class com.nlbn.ads.util.AppsflyerNetworkInfo  {public private protected *;}
-keep class com.nlbn.ads.util.PreferenceManager  {private public protected *;}
-keep class com.nlbn.ads.model.GetDataAppsFlyerSuccessEvent  {private public protected *;}
