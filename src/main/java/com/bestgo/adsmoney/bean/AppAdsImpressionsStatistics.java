package com.bestgo.adsmoney.bean;

import java.util.Date;

/**
 * @author mengjun
 * @date 2018/7/7 11:34
 * @description 应用展示统计对象
 */
public class AppAdsImpressionsStatistics {
    private Date installedDate;
    private Date eventDate;
    private String appId;
    private String countryCode;
    private String adUnitId;
    private int impressions;
    private double ecpm;
    private double revenue;

    public AppAdsImpressionsStatistics() {
    }

    public AppAdsImpressionsStatistics(Date installedDate, Date eventDate, String appId, String countryCode, String adUnitId, int impressions, double ecpm, double revenue) {
        this.installedDate = installedDate;
        this.eventDate = eventDate;
        this.appId = appId;
        this.countryCode = countryCode;
        this.adUnitId = adUnitId;
        this.impressions = impressions;
        this.ecpm = ecpm;
        this.revenue = revenue;
    }

    public Date getInstalledDate() {
        return installedDate;
    }

    public void setInstalledDate(Date installedDate) {
        this.installedDate = installedDate;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAdUnitId() {
        return adUnitId;
    }

    public void setAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
    }

    public int getImpressions() {
        return impressions;
    }

    public void setImpressions(int impressions) {
        this.impressions = impressions;
    }

    public double getEcpm() {
        return ecpm;
    }

    public void setEcpm(double ecpm) {
        this.ecpm = ecpm;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    @Override
    public String toString() {
        return "AppAdsImpressionsStatistics{" +
                "installedDate=" + installedDate +
                ", eventDate=" + eventDate +
                ", appId='" + appId + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", adUnitId='" + adUnitId + '\'' +
                ", impressions=" + impressions +
                ", ecpm=" + ecpm +
                ", revenue=" + revenue +
                '}';
    }
}
