package com.nlbn.ads.rate;

import android.app.Activity;
import android.graphics.Typeface;

import com.nlbn.ads.R;
import com.nlbn.ads.callback.IClickBtn;


public class RateBuilder {
    private String title, content, rateUs, notNow;
    private int titleColor = 0, contentColor = 0, rateUsDra, rateUsColor = 0, notNowColor = 0;
    private String colorStart, colorEnd;
    private int titleSize = 0, contentSize = 0 , rateNowSize = 0 , notNowSize = 0;
    private final Activity context;
    private int drawableRateUs = 0;
    private IClickBtn onClickBtn;
    private boolean isExitApp = false;
    private boolean isRateInApp = true;
    private int numberRateInApp = 4;
    private String colorRatingBar;
    private String colorRatingBarBg;
    private Typeface typeface = null;
    private Typeface typefaceTitle = null;
    private Typeface typefaceContent = null;
    private Typeface typefaceRateUs = null;
    private Typeface typefaceNotNow = null;
    private int drawableDialog = 0;
    private int drawableBgStar = 0;
    private int numberRateDefault = 5;
    public RateAppDiaLog rateAppDiaLog;

    public RateAppDiaLog getRateAppDiaLog() {
        return rateAppDiaLog;
    }

    private int[] arrStar = {R.drawable.ic_star_0, R.drawable.ic_star_1, R.drawable.ic_star_2, R.drawable.ic_star_3, R.drawable.ic_star_4, R.drawable.ic_star_5};

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getRateUs() {
        return rateUs;
    }

    public String getNotNow() {
        return notNow;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public int getContentColor() {
        return contentColor;
    }

    public int getRateUsDra() {
        return rateUsDra;
    }

    public int getRateUsColor() {
        return rateUsColor;
    }

    public int getNotNowColor() {
        return notNowColor;
    }

    public String getColorStart() {
        return colorStart;
    }

    public String getColorEnd() {
        return colorEnd;
    }

    public int getTitleSize() {
        return titleSize;
    }

    public int getContentSize() {
        return contentSize;
    }

    public int getRateNowSize() {
        return rateNowSize;
    }

    public int getNotNowSize() {
        return notNowSize;
    }

    public Activity getContext() {
        return context;
    }

    public int getDrawableRateUs() {
        return drawableRateUs;
    }

    public IClickBtn getOnClickBtn() {
        return onClickBtn;
    }

    public boolean isExitApp() {
        return isExitApp;
    }

    public boolean isRateInApp() {
        return isRateInApp;
    }

    public int getNumberRateInApp() {
        return numberRateInApp;
    }

    public String getColorRatingBar() {
        return colorRatingBar;
    }

    public String getColorRatingBarBg() {
        return colorRatingBarBg;
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public Typeface getTypefaceTitle() {
        return typefaceTitle;
    }

    public Typeface getTypefaceContent() {
        return typefaceContent;
    }

    public Typeface getTypefaceRateUs() {
        return typefaceRateUs;
    }

    public Typeface getTypefaceNotNow() {
        return typefaceNotNow;
    }

    public int getDrawableDialog() {
        return drawableDialog;
    }

    public int getDrawableBgStar() {
        return drawableBgStar;
    }

    public int getNumberRateDefault() {
        return numberRateDefault;
    }

    public int[] getArrStar() {
        return arrStar;
    }

    public RateBuilder(Activity context) {
        this.context = context;
    }

    public RateBuilder setTextTitle(String title) {
        this.title = title;
        return this;
    }

    public RateBuilder setTextContent(String content) {
        this.content = content;
        return this;
    }

    public RateBuilder setTextButton(String rateUs, String notNow) {
        this.rateUs = rateUs;
        this.notNow = notNow;
        return this;
    }

    public RateBuilder setRateInApp(Boolean isRateInApp) {
        this.isRateInApp = isRateInApp;
        return this;
    }

    public RateBuilder setTextTitleColorLiner(String colorStart, String colorEnd) {
        this.colorStart = colorStart;
        this.colorEnd = colorEnd;
        return this;
    }

    public RateBuilder setDrawableButtonRate(int drawable) {
        this.drawableRateUs = drawable;
        return this;
    }

    public RateBuilder setTextTitleColor(int color) {
        this.titleColor = color;
        return this;
    }

    public RateBuilder setTextRateUsColor(int color) {
        this.rateUsColor = color;
        return this;
    }

    public RateBuilder setTextNotNowColor(int color) {
        this.notNowColor = color;
        return this;
    }

    public RateBuilder setTextTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    public RateBuilder setTextContentSize(int contentSize) {
        this.contentSize = contentSize;
        return this;
    }

    public RateBuilder setTextRateSize(int rateSize) {
        this.rateNowSize= rateSize;
        return this;
    }

    public RateBuilder setTextNotNowSize(int notNSize) {
        this.notNowSize= notNSize;
        return this;
    }

    public RateBuilder setTextContentColor(int color) {
        this.contentColor = color;
        return this;
    }

    public RateBuilder setColorRatingBar(String color) {
        this.colorRatingBar = color;
        return this;
    }

    public RateBuilder setColorRatingBarBG(String color) {
        this.colorRatingBarBg = color;
        return this;
    }

    public RateBuilder setOnclickBtn(IClickBtn onClickBtn) {
        this.onClickBtn = onClickBtn;
        return this;
    }

    /* public Builder setExitApp(Boolean isExitApp) {
         this.isExitApp = isExitApp;
         return this;
     }*/
    public RateBuilder setNumberRateInApp(int numberRate) {
        this.numberRateInApp = numberRate;
        return this;
    }

    public RateBuilder setFontFamily(Typeface typeface) {
        this.typeface = typeface;
        return this;
    }

    public RateBuilder setFontFamilyTitle(Typeface typeface) {
        this.typefaceTitle = typeface;
        return this;
    }

    public RateBuilder setFontFamilyContent(Typeface typeface) {
        this.typefaceContent = typeface;
        return this;
    }

    public RateBuilder setFontFamilyRateUs(Typeface typeface) {
        this.typefaceRateUs = typeface;
        return this;
    }

    public RateBuilder setFontFamilyNotNow(Typeface typeface) {
        this.typefaceNotNow = typeface;
        return this;
    }

    public RateBuilder setBackgroundDialog(int drawable) {
        this.drawableDialog = drawable;
        return this;
    }

    public RateBuilder setBackgroundStar(int drawable) {
        this.drawableBgStar = drawable;
        return this;
    }

    public RateBuilder setNumberRateDefault(int number) {
        this.numberRateDefault = number;
        return this;
    }

    public RateBuilder setArrStar(int[] arr) {
        if (arr.length == arrStar.length) {
            this.arrStar = arr;
        }
        return this;
    }

    public RateAppDiaLog build() {
        rateAppDiaLog = new RateAppDiaLog(context, this);
        return rateAppDiaLog;
    }
    
}
