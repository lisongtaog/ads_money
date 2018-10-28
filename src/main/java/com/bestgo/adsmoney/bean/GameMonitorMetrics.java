package com.bestgo.adsmoney.bean;

import java.util.Date;

/**
 * Created by jikai on 11/6/17.
 */
public class GameMonitorMetrics {
    public Date date;
    public double cost;
    public long purchasedUser;
    public long totalInstalled;
    public long totalUninstalled;
    public long todayUninstalled;
    public float uninstallRate;
    public long totalUser;

    public long activeUser;
    public double revenue;
    public long impression;
    public float arpu;
    public float arpuTrend;
    public double cpa;
    public double ecpm;
    public double incoming;
    public double estimatedRevenue;
    public long recommendImpression;
    public long recommendClick;
    public long recommendInstalled;
    public double avgSumImpression;
    public double cpaDivEcpm;
    public double sumRevenue;
    public double sumCost;

    public long roundStartActiveUserCount;
    public long roundWinActiveUserCount;
    public long roundStartFreashUserCount;
    public long roundWinFreashUserCount;

    public long activeUserRoundStartCount;
    public long activeUserRoundWinCount;
    public long freashUserRoundStartCount;
    public long freashUserRoundWinCount;
    public float avgSumFreasherRoundStart;
    public float avgSumFreasherRoundWin;
    public float avgSumActiverRoundStart;
    public float avgSumActiverRoundWin;

}
