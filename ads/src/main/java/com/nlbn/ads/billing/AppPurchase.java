package com.nlbn.ads.billing;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.nlbn.ads.callback.BillingListener;
import com.nlbn.ads.callback.PurchaseListener;

import java.util.List;

public abstract class AppPurchase {
    public static AppPurchase getInstance() {
        return AppPurchaseImpl.getInstance();
    }

    public abstract void initBilling(final Application application);

    public abstract void initBilling(final Application application, List<String> listINAPId, List<String> listSubsId);

    public abstract void setPurchaseListener(PurchaseListener purchaseListener);

    public abstract void setBillingListener(BillingListener billingListener);

    public abstract boolean isAvailable();

    public abstract Boolean getInitBillingFinish();

    public abstract void setBillingListener(BillingListener billingListener, int timeout);

    public abstract void setPrice(String price);

    public abstract void setConsumePurchase(boolean consumePurchase);

    public abstract void setOldPrice(String oldPrice);

    public abstract void setProductId(String productId);

    public abstract void addSubscriptionId(String id);

    public abstract void addProductId(String id);

    public abstract boolean isPurchased();

    public abstract boolean isPurchased(Context context);

    public abstract void verifyPurchased(boolean isCallback);

    public abstract void purchase(Activity activity);

    public abstract String purchase(Activity activity, String productId);

    public abstract String subscribe(Activity activity, String subsId);

    public abstract void consumePurchase();

    public abstract void consumePurchase(String productId);

    public abstract String getPrice();

    public abstract String getPrice(String productId);

    public abstract String getPriceSub(String productId);

    public abstract String getFormattedPriceSub(String productId);

    public abstract String getFormattedOfferPriceSub(String productId);

    public abstract String getOfferPriceSub(String productId);

    public abstract String getCurrency(String productId, int typeIAP);

    public abstract String getFormattedPriceIAP(String productId);
}
