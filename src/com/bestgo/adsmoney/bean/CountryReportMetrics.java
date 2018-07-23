package com.bestgo.adsmoney.bean;

import java.util.Date;

/**
 * Created by jikai on 11/6/17.
 */
public class CountryReportMetrics {
    public String countryCode;
    public String countryName;
    public double cost;
    public long purchasedUser;
    public long natureUser;
    public long totalInstalled;
    public long totalUninstalled;
    public long todayUninstalled;
    public float uninstallRate;
    public long totalUser;
    public long activeUser;
    public double cpa;
    public double nowRevenue;//当日新安装 总用户的收益
    public double natureRevenue;//当日新安装 自然量用户的收益
    public double revenue;
    public double ecpm;
    public double incoming;
    public double estimatedRevenue;
}
