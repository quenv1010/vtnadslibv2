package com.nlbn.ads.util;

public class AdjustAttribution {
    String trackerToken, trackerName, network, campaign, adgroup, creative, clickLabel, adid, costType,  costCurrency, fbInstallReferrer;
    Double costAmount;

    public String getTrackerToken() {
        return trackerToken;
    }

    public void setTrackerToken(String trackerToken) {
        this.trackerToken = trackerToken;
    }

    public String getTrackerName() {
        return trackerName;
    }

    public void setTrackerName(String trackerName) {
        this.trackerName = trackerName;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public String getAdgroup() {
        return adgroup;
    }

    public void setAdgroup(String adgroup) {
        this.adgroup = adgroup;
    }

    public String getCreative() {
        return creative;
    }

    public void setCreative(String creative) {
        this.creative = creative;
    }

    public String getClickLabel() {
        return clickLabel;
    }

    public void setClickLabel(String clickLabel) {
        this.clickLabel = clickLabel;
    }

    public String getAdid() {
        return adid;
    }

    public void setAdid(String adid) {
        this.adid = adid;
    }

    public String getCostType() {
        return costType;
    }

    public void setCostType(String costType) {
        this.costType = costType;
    }

    public Double getCostAmount() {
        return costAmount;
    }

    public void setCostAmount(Double costAmount) {
        this.costAmount = costAmount;
    }

    public String getCostCurrency() {
        return costCurrency;
    }

    public void setCostCurrency(String costCurrency) {
        this.costCurrency = costCurrency;
    }

    public String getFbInstallReferrer() {
        return fbInstallReferrer;
    }

    public void setFbInstallReferrer(String fbInstallReferrer) {
        this.fbInstallReferrer = fbInstallReferrer;
    }
}
