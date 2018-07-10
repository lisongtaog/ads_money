package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.utils.NumberUtil;
import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.common.database.services.DB;
import com.bestgo.common.database.utils.JSObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author mengjun
 * @date 2018/7/5 14:25
 * @description 应用广告收入统计
 */
@WebServlet(name = "QueryAppAdsRevenueStatistics", urlPatterns = {"/query_app_ads_revenue_statistics"})
public class QueryAppAdsRevenueStatistics extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;
        String date = request.getParameter("date");
        String appId = request.getParameter("appId");
        String countryCode = request.getParameter("countryCode");

//        date = "2018-06-20";
//        appId = "com.androapplite.vpn10";
//        countryCode = "TM";
        JsonObject json = new JsonObject();
        try {
            //计算实际花费，总安装，购买安装，购买安装在总安装（购买安装+自然量）中的占比
            String sql = "SELECT SUM(b.spend) AS total_spend,SUM(a.installed) AS total_installed,SUM(b.installed) AS purchase_installed " +
                    "FROM app_firebase_daily_metrics_history a,app_ads_daily_metrics_history b " +
                    "WHERE a.date = b.date AND a.app_id = b.app_id AND a.country_code = b.country_code " +
                    "AND a.date = '" + date + "' " +
                    ("all".equals(appId) || appId.isEmpty() ? " " : "AND a.app_id = '" + appId + "' ") +
                    ("all".equals(countryCode) || countryCode.isEmpty() ? " " : "AND a.country_code = '" + countryCode + "' ");
            JSObject one = DB.findOneBySql(sql);
            double actualInstallationRatio = 0;
            double totalSpend = 0;
            double totalInstalled = 0;
            double purchaseInstalled = 0;
            JsonArray dateArray = new JsonArray();
            JsonArray dataArray = new JsonArray();
            JsonArray dataTable = new JsonArray();
            JsonArray item = null;
            if (one.hasObjectData()) {
                totalSpend = NumberUtil.convertDouble(one.get("total_spend"),0);
                totalInstalled = NumberUtil.convertDouble(one.get("total_installed"),0);
                purchaseInstalled = NumberUtil.convertDouble(one.get("purchase_installed"),0);
                actualInstallationRatio = totalInstalled == 0 ? 0 : purchaseInstalled / totalInstalled;
            }
            //计算在某个安装日期内（的某个应用在某个国家中）的每展示日期总收入
            sql = "SELECT event_date,sum(revenue) AS total_revenue FROM app_ads_impressions_statistics " +
                    "WHERE installed_date = '" + date + "' " +
                    ("all".equals(appId) || appId.isEmpty() ? " " : "AND app_id = '" + appId + "' ") +
                    ("all".equals(countryCode) || countryCode.isEmpty() ? " " : "AND country_code = '" + countryCode + "' ") +
                    "AND event_date >= '" + date + "' " +
                    "GROUP BY event_date ORDER BY event_date";
            List<JSObject> revenueList = DB.findListBySql(sql);

            double sumRevenue = 0;
            totalSpend = NumberUtil.trimDouble(totalSpend,5);
            for (int i = 0,len = revenueList.size();i < len;i++) {
                JSObject revenueJS = revenueList.get(i);
                if (revenueJS.hasObjectData()) {
                    item = new JsonArray();
                    String eventDate = revenueJS.get("event_date").toString();
                    dateArray.add(eventDate);
                    double totalRevenue = revenueJS.get("total_revenue");
                    sumRevenue += totalRevenue * actualInstallationRatio;
                    double sumRevenueTrim = NumberUtil.trimDouble(sumRevenue,5);
                    dataArray.add(sumRevenueTrim);

                    //安装日期-花费-总安装-购买安装-统计日期-累计收入-收支比例
                    item.add(date);
                    item.add(totalSpend);
                    item.add(totalInstalled);
                    item.add(purchaseInstalled);
                    item.add(eventDate);
                    item.add(sumRevenueTrim);
                    double revenueCostRatio = totalSpend == 0 ? 0 : sumRevenue / totalSpend;
                    item.add(NumberUtil.trimDouble(revenueCostRatio,5));
                    dataTable.add(item);
                }
            }

            json.add("date_array",dateArray);
            json.add("data_array",dataArray);
            json.add("data_table",dataTable);
            json.addProperty("ret", 1);

        } catch (Exception ex) {
            json.addProperty("ret", 0);
            json.addProperty("message", ex.getMessage());
        }

        response.getWriter().write(json.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }
}
