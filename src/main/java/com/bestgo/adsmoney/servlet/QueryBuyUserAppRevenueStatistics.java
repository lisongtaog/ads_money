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
 * @description 购买用户应用广告收入统计
 */
@WebServlet(name = "QueryBuyUserAppRevenueStatistics", urlPatterns = {"/query_buy_app_revenue_statistics"})
public class QueryBuyUserAppRevenueStatistics extends HttpServlet {
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
            double totalInstalled = 0;
            double actualInstallationRatio = 0;
            double totalSpend = 0;
            double purchaseInstalled = 0;
            String sql = "SELECT SUM(installed) AS total_installed " +
                    "FROM app_firebase_daily_metrics_history " +
                    "WHERE date = '" + date + "' " +
                    ("all".equals(appId) || appId.isEmpty() ? " " : "AND app_id = '" + appId + "' ") +
                    ("all".equals(countryCode) || countryCode.isEmpty() ? " " : "AND country_code = '" + countryCode + "' ");
            JSObject one = DB.findOneBySql(sql);
            if (one.hasObjectData()) {
                totalInstalled = NumberUtil.convertDouble(one.get("total_installed"),0);
            }
            sql = "SELECT SUM(spend) AS total_spend,SUM(installed) AS purchase_installed " +
                    "FROM app_ads_daily_metrics_history " +
                    "WHERE date = '" + date + "' " +
                    ("all".equals(appId) || appId.isEmpty() ? " " : "AND app_id = '" + appId + "' ") +
                    ("all".equals(countryCode) || countryCode.isEmpty() ? " " : "AND country_code = '" + countryCode + "' ");
            one = DB.findOneBySql(sql);
            if (one.hasObjectData()) {
                totalSpend = NumberUtil.convertDouble(one.get("total_spend"),0);
                purchaseInstalled = NumberUtil.convertDouble(one.get("purchase_installed"),0);
                actualInstallationRatio = totalInstalled == 0 ? 0 : purchaseInstalled / totalInstalled;
            }
            double purchaseInstallRatio = totalInstalled == 0 ? 0 : purchaseInstalled / totalInstalled; //购买安装比例
            String purchaseInstallRatioTrim = NumberUtil.trimDouble(purchaseInstallRatio * 100, 3) + "%";
            JsonArray dateArray = new JsonArray();
            JsonArray dataArray = new JsonArray();
            JsonArray dataTable = new JsonArray();
            JsonArray item = null;

            //计算在某个安装日期内（的某个应用在某个国家中）的每展示日期总收入
            sql = "SELECT event_date,sum(revenue) AS total_revenue FROM app_ads_buy_impressions_statistics " +
                    "WHERE installed_date = '" + date + "' " +
                    ("all".equals(appId) || appId.isEmpty() ? " " : "AND app_id = '" + appId + "' ") +
                    ("all".equals(countryCode) || countryCode.isEmpty() ? " " : "AND country_code = '" + countryCode + "' ") +
                    "AND event_date >= '" + date + "' " +
                    "GROUP BY event_date ORDER BY event_date";
            List<JSObject> revenueList = DB.findListBySql(sql);

            double sumTotalRevenue = 0;
            totalSpend = NumberUtil.trimDouble(totalSpend,5);
            for (int i = 0,len = revenueList.size();i < len;i++) {
                JSObject revenueJS = revenueList.get(i);
                if (revenueJS.hasObjectData()) {
                    item = new JsonArray();
                    String eventDate = revenueJS.get("event_date").toString();
                    dateArray.add(eventDate);
                    double totalRevenue = revenueJS.get("total_revenue");
                    sumTotalRevenue += totalRevenue;
                    double sumRevenueTrim = NumberUtil.trimDouble(sumTotalRevenue,5);
                    dataArray.add(sumRevenueTrim);

                    //安装日期-花费-总安装量-购买安装量-统计日期-购买安装收入-购买安装累计收入-购买累计收支比
                    item.add(date); //安装日期
                    item.add(totalSpend); //花费
                    item.add(totalInstalled); //总安装量
                    item.add(purchaseInstalled); //购买安装量
                    item.add(eventDate); //统计日期
                    item.add(NumberUtil.trimDouble(totalRevenue,5)); //购买安装收入
                    item.add(sumRevenueTrim); // 购买安装累计收入

                    double purchaseRevenueCostRatio = totalSpend == 0 ? 0 : sumTotalRevenue / totalSpend;
                    item.add(NumberUtil.trimDouble(purchaseRevenueCostRatio * 100,3) + "%"); // 购买累计收支比

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
