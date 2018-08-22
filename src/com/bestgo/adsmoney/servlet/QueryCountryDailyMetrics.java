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
public class QueryCountryDailyMetrics extends HttpServlet {//admanager投放系统拉取国家统计数据
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
                    String sql = "select app_id,country_code, " +
                            " sum(user_num_nature) as user_num_nature, " +//自然量 用户数
                            " sum(user_num_purchase) as user_num_purchase, " +//购买量 用户数
                            " sum(user_num_total) as user_num_total, " +//新安装 总用户数
                            " sum(user_num_sample) as user_num_sample, " +//新安装 sample样本用户数
                            " sum(revenue_nature) as revenue_nature, " +//自然量 收益
                            " sum(revenue_purchase) as revenue_purchase, " +//购买量 收益
                            " sum(revenue_total) as revenue_total " +//新安装 总收益
                            " from app_first_install_data " +
                            " where date between '" + date + "' and '" + date + "'"+
                            " and app_id=? group by app_id, country_code";
                    List<JSObject> list = DB.findListBySql(sql, app_id);
                    for (int i = 0; i < list.size(); i++) {
                        String appId = list.get(i).get("app_id");
                        String countryCode = list.get(i).get("country_code");
                        double revenueNature = Utils.convertDouble(list.get(i).get("revenue_nature"), 0);//自然量收益
                        double revenuePurchase = Utils.convertDouble(list.get(i).get("revenue_purchase"), 0); //当日购买用户总、收益
                        double revenueNow = Utils.convertDouble(list.get(i).get("revenue_total"), 0); //当日新安装用户总收益
                        long natureUser = Utils.convertLong(list.get(i).get("user_num_nature"), 0);//自然量用户数
                        long purchaseUser = Utils.convertLong(list.get(i).get("user_num_purchase"), 0);//购买量用户数
                        long sampleUser = Utils.convertLong(list.get(i).get("user_num_sample"), 0);//sample样本用户数

                        ResponseItem one = metricsMap.get(getKey(appId, countryCode));
                        if (one == null) {
                            one = new ResponseItem();
                            metricsMap.put(getKey(appId, countryCode), one);
                            resultList.add(one);
                        }
                        one.appId = appId;
                        one.countryCode = countryCode;
                        one.natureUser = natureUser;//仅新安装用户时使用，与purchasedUser不同
                        one.purchaseUser = purchaseUser;//购买量用户数 //仅新安装用户时使用，与purchasedUser不同
                        one.sampleUser = sampleUser; //sample样本用户数
                        one.natureRevenue = revenueNature;//自然量 用户收益
                        one.purchaseRevenue = revenuePurchase;//购买安装用户收益
                        one.nowRevenue = revenueNow;//当日 购买用户总收益
                    }

                    sql = "select app_id, country_code, sum(ad_revenue) as ad_revenue, sum(ad_impression) as ad_impression,sum(ad_new_revenue) as ad_new_revenue " +
                            " from app_daily_metrics_history " +
                            " where date = '" + date + "' and app_id = '" + app_id + "'  group by app_id, country_code";
                    list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        String appId = list.get(i).get("app_id");
                        String countryCode = list.get(i).get("country_code");
                        double revenue = Utils.trimDouble(Utils.convertDouble(list.get(i).get("ad_revenue"), 0));//当日总收益：包含当日新安装用户和老用户
                        double nowRevenue = Utils.trimDouble(Utils.convertDouble(list.get(i).get("ad_new_revenue"), 0));//当日新安装用户收益
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
                        if(one.nowRevenue == 0){
                            //当日新安装用户收益；当日新安装用户收益 从自然量app_first_install_data中获取，有可能存在无自然量、或未集成sdk的应用，但配置了新的广告单元
                            //如果为0，则从app_daily_metrics_history日表中获取
                            one.nowRevenue = nowRevenue;
                        }
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
                            " where date = '" + date + "' and app_id = '" + app_id + "'  group by app_id, country_code";
                    list = DB.findListBySql(sql);

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

                    //新用户展示和收入
                    sql = "SELECT app_id,country_code,SUM(ad_impression) AS new_user_impression,SUM(ad_revenue) AS new_user_revenue \n" +
                            "FROM app_ad_unit_metrics_history\n" +
                            "WHERE date = '"+date+"' AND app_id = '"+app_id+"'\n" +
                            "AND ad_unit_id in (SELECT ad_unit_id FROM app_ad_unit_config WHERE app_id = '"+app_id+"' AND flag = 1)\n" +
                            "GROUP BY app_id,country_code";
                    list = DB.findListBySql(sql);
                    for (int i = 0; i < list.size(); i++) {
                        String appId = list.get(i).get("app_id");
                        String countryCode = list.get(i).get("country_code");
                        double newUserImpression = Utils.convertDouble(list.get(i).get("new_user_impression"), 0);
                        double newUserRevenue = Utils.convertDouble(list.get(i).get("new_user_revenue"), 0);
                        ResponseItem one = metricsMap.get(getKey(appId, countryCode));
                        if (one == null) {
                            one = new ResponseItem();
                            metricsMap.put(getKey(appId, countryCode), one);
                            resultList.add(one);
                        }
                        one.appId = appId;
                        one.countryCode = countryCode;
                        one.new_user_revenue = newUserRevenue;
                        one.new_user_impression = newUserImpression;
                    }

                    //旧用户展示和收入
                    sql = "SELECT app_id,country_code,SUM(ad_impression) AS old_user_impression,SUM(ad_revenue) AS old_user_revenue \n" +
                            "FROM app_ad_unit_metrics_history\n" +
                            "WHERE date = '"+date+"' AND app_id = '"+app_id+"'\n" +
                            "AND ad_unit_id in (SELECT ad_unit_id FROM app_ad_unit_config WHERE app_id = '"+app_id+"' AND flag = 0)\n" +
                            "GROUP BY app_id,country_code";
                    list = DB.findListBySql(sql);
                    for (int i = 0; i < list.size(); i++) {
                        String appId = list.get(i).get("app_id");
                        String countryCode = list.get(i).get("country_code");
                        double oldUserImpression = Utils.convertDouble(list.get(i).get("old_user_impression"), 0);
                        double oldUserRevenue = Utils.convertDouble(list.get(i).get("old_user_revenue"), 0);
                        ResponseItem one = metricsMap.get(getKey(appId, countryCode));
                        if (one == null) {
                            one = new ResponseItem();
                            metricsMap.put(getKey(appId, countryCode), one);
                            resultList.add(one);
                        }
                        one.appId = appId;
                        one.countryCode = countryCode;
                        one.old_user_revenue = oldUserRevenue;
                        one.old_user_impression = oldUserImpression;
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

                        jsonObject.addProperty("newuser_sample", Utils.trimDouble(one.sampleUser));//sample样本用户数
                        jsonObject.addProperty("newuser_total", Utils.trimDouble(one.natureUser + one.purchaseUser));//当日新安装总用户数

                        jsonObject.addProperty("nowRevenue", Utils.trimDouble(one.nowRevenue));//当日安装用户收益
                        jsonObject.addProperty("natureRevenue", Utils.trimDouble(one.natureRevenue));//自然量 用户收益
                        jsonObject.addProperty("purchaseRevenue", Utils.trimDouble(one.purchaseRevenue));//购买安装用户收益
                        jsonObject.addProperty("new_user_impression",one.new_user_impression); //新用户展示(sample样本)
                        jsonObject.addProperty("old_user_impression",one.old_user_impression); //旧用户展示
                        jsonObject.addProperty("new_user_revenue",one.new_user_revenue); //新用户收入(sample样本)
                        jsonObject.addProperty("old_user_revenue",one.old_user_revenue); //旧用户收入

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
        public long purchaseUser;//仅新安装用户时使用，与purchasedUser不同
        public long natureUser;//仅新安装用户时使用，与purchasedUser不同
        public long sampleUser;//仅新安装用户时使用，与purchasedUser不同；样本用户数

        public long installed;
        public long uninstalled;
        public long todayUninstalled;
        public long totalUser;
        public long activeUser;
        public double revenue;
        public double nowRevenue;//当日新安装 总用户的收益
        public double natureRevenue;//当日新安装 自然量用户的收益
        public double purchaseRevenue;//当日新安装 购买安装用户的收益
        public double estimatedRevenue14;
        public double uninstallRate;
        public long impression;
        public double new_user_revenue;
        public double new_user_impression;
        public double old_user_revenue;
        public double old_user_impression;
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
