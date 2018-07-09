package com.bestgo.adsmoney.servlet;

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
import java.util.*;

@WebServlet(name = "QueryCountryDailyMetrics", urlPatterns = "/query_country_daily_metric")
public class QueryCountryDailyMetrics extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String date = request.getParameter("date");
        String app_id = request.getParameter("app_id");

        if ("iLoveMoney".equals(token)) {
            try {
                HashMap<String, ResponseItem> metricsMap = new HashMap<>();
                ArrayList<ResponseItem> resultList = new ArrayList<>();
                JsonArray array = new JsonArray();

                try {
                    String sql = "select app_id, country_code, sum(ad_revenue) as ad_revenue, sum(ad_impression) as ad_impression " +
                            "from app_daily_metrics_history " +
                            "where date between '" + date + "' and '" + date + "' and app_id=? group by app_id, country_code";
                    List<JSObject> list = DB.findListBySql(sql, app_id);

                    for (int i = 0; i < list.size(); i++) {
                        String appId = list.get(i).get("app_id");
                        String countryCode = list.get(i).get("country_code");
                        double revenue = Utils.trimDouble(Utils.convertDouble(list.get(i).get("ad_revenue"), 0));
                        long impression = Utils.convertLong(list.get(i).get("ad_impression"), 0);
                        ResponseItem one = metricsMap.get(getKey(appId, countryCode));
                        if (one == null) {
                            one = new ResponseItem();
                            metricsMap.put(getKey(appId, countryCode), one);
                            resultList.add(one);
                        }
                        one.appId = appId;
                        one.countryCode = countryCode;
                        one.impression = impression;
                        one.revenue = revenue;
                    }

                    sql = "select app_id, country_code, sum(installed) as total_installed, sum(today_uninstalled) as today_uninstalled, sum(uninstalled) as total_uninstalled, sum(total_user) as total_user, sum(active_user) as active_user " +
                            "from app_firebase_daily_metrics_history " +
                            "where date between '" + date + "' and '" + date + "' and app_id=? group by app_id, country_code";
                    list = DB.findListBySql(sql, app_id);

                    for (int i = 0; i < list.size(); i++) {
                        String appId = list.get(i).get("app_id");
                        String countryCode = list.get(i).get("country_code");
                        long totalIntalled = Utils.convertLong(list.get(i).get("total_installed"), 0);
                        long totalUninstalled = Utils.convertLong(list.get(i).get("total_uninstalled"), 0);
                        long todayUninstalled = Utils.convertLong(list.get(i).get("today_uninstalled"), 0);
                        long totalUser = Utils.convertLong(list.get(i).get("total_user"), 0);
                        long activeUser = Utils.convertLong(list.get(i).get("active_user"), 0);
                        ResponseItem one = metricsMap.get(getKey(appId, countryCode));
                        if (one == null) {
                            one = new ResponseItem();
                            metricsMap.put(getKey(appId, countryCode), one);
                            resultList.add(one);
                        }
                        one.appId = appId;
                        one.countryCode = countryCode;
                        one.installed = totalIntalled;
                        one.uninstalled = totalUninstalled;
                        one.todayUninstalled = todayUninstalled;
                        one.totalUser = totalUser;
                        one.activeUser = activeUser;
                        one.uninstallRate = one.installed > 0 ? (one.todayUninstalled * 1.0f / one.installed) : 0;
                    }

                    sql = "select app_id, country_code, sum(spend) as cost, sum(installed) as purchasedUser " +
                            "from app_ads_daily_metrics_history " +
                            "where date between '" + date + "' and '" + date + "' and app_id=? group by app_id, country_code";
                    list = DB.findListBySql(sql, app_id);

                    for (int i = 0; i < list.size(); i++) {
                        String appId = list.get(i).get("app_id");
                        String countryCode = list.get(i).get("country_code");
                        double cost = Utils.convertDouble(list.get(i).get("cost"), 0);
                        long purchasedUser = Utils.convertLong(list.get(i).get("purchasedUser"), 0);
                        ResponseItem one = metricsMap.get(getKey(appId, countryCode));
                        if (one == null) {
                            one = new ResponseItem();
                            metricsMap.put(getKey(appId, countryCode), one);
                            resultList.add(one);
                        }
                        one.appId = appId;
                        one.countryCode = countryCode;
                        one.cost = cost;
                        one.purchasedUser = purchasedUser;
                    }

                    JsonObject json = new JsonObject();
                    for (int i = 0; i < resultList.size(); i++) {
                        JsonObject jsonObject = new JsonObject();
                        ResponseItem one = resultList.get(i);
                        jsonObject.addProperty("date", date);
                        jsonObject.addProperty("app_id", one.appId);
                        jsonObject.addProperty("country_code", one.countryCode);
                        jsonObject.addProperty("cost", Utils.trimDouble(one.cost));
                        jsonObject.addProperty("purchased_user", one.purchasedUser);
                        jsonObject.addProperty("total_installed", one.installed);
                        jsonObject.addProperty("total_uninstalled", one.uninstalled);
                        jsonObject.addProperty("today_uninstalled", one.todayUninstalled);
                        jsonObject.addProperty("total_user", one.totalUser);
                        jsonObject.addProperty("active_user", one.activeUser);
                        jsonObject.addProperty("impression", one.impression);
                        jsonObject.addProperty("revenue", Utils.trimDouble(one.revenue));
                        double arpu = one.activeUser > 0 ? (float) (one.revenue / one.activeUser) : 0;
                        double arpu1 = one.totalUser > 0 ? (float) (one.revenue / one.totalUser) : 0;
                        jsonObject.addProperty("estimated_revenue", Utils.trimDouble(estimateRevenue(one.purchasedUser,
                                one.uninstallRate, arpu, arpu1)));
                        array.add(jsonObject);
                    }

                    json.addProperty("ret", 1);
                    json.addProperty("message", "成功");
                    json.addProperty("total", resultList.size());
                    json.add("data", array);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                response.getWriter().write(array.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    private String getKey(String appId, String countryCode) {
        return appId + "_" + countryCode;
    }

    class ResponseItem {
        public String date;
        public String appId;
        public String countryCode;
        public double cost;
        public long purchasedUser;
        public long installed;
        public long uninstalled;
        public long todayUninstalled;
        public long totalUser;
        public long activeUser;
        public double revenue;
        public double estimatedRevenue14;
        public double uninstallRate;
        public long impression;
    }

    private double estimateRevenue(double installUser, double uninstallRate, double arpu, double arpu1) {
        double user = 0;
        for (int i = 0; i < 14; i++) {
            user += estimateAlivedUser(installUser, uninstallRate, i);
        }
        return installUser * arpu + (user - installUser) * arpu1;
    }

    private double estimateAlivedUser(double installUser, double uninstallRate, int day) {
        ArrayList<Double> uninstallRateList = new ArrayList<>();
        uninstallRateList.add(1.0);
        uninstallRateList.add(1 - uninstallRate);
        uninstallRateList.add((1 - uninstallRate) * 0.85);
        uninstallRateList.add((1 - uninstallRate) * 0.85 * 0.9);
        for (int i = 3; i < 14; i++) {
            uninstallRateList.add(uninstallRateList.get(uninstallRateList.size() - 1) * 0.9);
        }
        double lastUser = installUser * uninstallRateList.get(day);
        return lastUser;
    }
}
