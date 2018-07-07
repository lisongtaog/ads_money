package com.bestgo.adsmoney.bean;

import java.util.Date;

/**
 * @author mengjun
 * @date 2018/7/7 11:28
 * @description 活跃用户统计对象
 */
public class AppActiveUserStatistics {
    private Date installedDate;
    private Date eventDate;
    private String appId;
    private String countryCode;
    private int ActiveNum;

    public AppActiveUserStatistics() {
    }

    public AppActiveUserStatistics(Date installedDate, Date eventDate, String appId, String countryCode, int activeNum) {
        this.installedDate = installedDate;
        this.eventDate = eventDate;
        this.appId = appId;
        this.countryCode = countryCode;
        ActiveNum = activeNum;
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

    public int getActiveNum() {
        return ActiveNum;
    }

    public void setActiveNum(int activeNum) {
        ActiveNum = activeNum;
    }

    @Override
    public String toString() {
        return "AppActiveUserStatistics{" +
                "installedDate=" + installedDate +
                ", eventDate=" + eventDate +
                ", appId='" + appId + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", ActiveNum=" + ActiveNum +
                '}';
    }
}
