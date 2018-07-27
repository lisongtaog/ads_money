package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.adsmoney.bean.CountryReportMetrics;
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

@WebServlet(name = "CountryReport", urlPatterns = {"/country_report/*"})
public class CountryReport extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/query")) {
                HashMap<String, CountryReportMetrics> metricsMap = new HashMap<>();
                ArrayList<CountryReportMetrics> resultList = new ArrayList<>();

                int period = Utils.parseInt(request.getParameter("period"), 1);
                String startDate = request.getParameter("start_date");
                String endDate = request.getParameter("end_date");
                int index = Utils.parseInt(request.getParameter("page_index"), 0);
                int size = Utils.parseInt(request.getParameter("page_size"), 20);
                String filter = request.getParameter("filter");
                int order = Utils.parseInt(request.getParameter("order"), 0);
                boolean desc = order < 1000;
                if (order >= 1000) order = order - 1000;

                if (filter == null || filter.isEmpty()) {
                    filter = "";
                }
                ArrayList<String> appIds = new ArrayList<>();
                String[] filters = filter.split(",");
                for (String appId : filters) {
                    if (appId.isEmpty()) continue;
                    appIds.add(appId);
                }

                try {
                    HashMap<String, CountryARPU> arpuHashMap = fetchNearbyARPU(startDate, appIds);

                    String sql = "select country_code, sum(ad_revenue) as ad_revenue, sum(ad_impression) as ad_impression " +
                            "from app_daily_metrics_history " +
                            "where date between '" + startDate + "' and '" + endDate + "' ";
                    if (appIds.size() > 0) {
                        String ss = "";
                        for (int i = 0; i < appIds.size(); i++) {
                            if (i < appIds.size() - 1) {
                                ss += "'" + appIds.get(i) + "',";
                            } else {
                                ss += "'" + appIds.get(i) + "'";
                            }
                        }
                        sql += " and app_id in (" + ss + ")";
                    }
                    sql += " group by country_code";

                    List<JSObject> list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {//收益、ECPM从变现系统数据抓取
                        String countryCode = list.get(i).get("country_code");
                        double revenue = Utils.trimDouble(Utils.convertDouble(list.get(i).get("ad_revenue"), 0));
                        long impression = Utils.convertLong(list.get(i).get("ad_impression"), 0);
                        CountryReportMetrics one = metricsMap.get(countryCode);
                        if (one == null) {
                            one = new CountryReportMetrics();
                            metricsMap.put(countryCode, one);
                            resultList.add(one);
                        }
                        one.countryCode = countryCode;
                        one.countryName = countryCode;
                        one.revenue = revenue;
                        one.ecpm = impression > 0 ? revenue / impression : 0;
                    }

                    sql = "select country_code, sum(installed) as total_installed, sum(today_uninstalled) as today_uninstalled, sum(uninstalled) as total_uninstalled, sum(total_user) as total_user, sum(active_user) as active_user " +
                            "from app_firebase_daily_metrics_history " +
                            "where date between '" + startDate + "' and '" + endDate + "' ";
                    if (appIds.size() > 0) {
                        String ss = "";
                        for (int i = 0; i < appIds.size(); i++) {
                            if (i < appIds.size() - 1) {
                                ss += "'" + appIds.get(i) + "',";
                            } else {
                                ss += "'" + appIds.get(i) + "'";
                            }
                        }
                        sql += " and app_id in (" + ss + ")";
                    }
                    sql += " group by country_code";
                    list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {//安装、卸载、用户数 等从firebase抓取
                        String countryCode = list.get(i).get("country_code");
                        long totalIntalled = Utils.convertLong(list.get(i).get("total_installed"), 0);
                        long totalUninstalled = Utils.convertLong(list.get(i).get("total_uninstalled"), 0);
                        long todayUninstalled = Utils.convertLong(list.get(i).get("today_uninstalled"), 0);
                        long totalUser = Utils.convertLong(list.get(i).get("total_user"), 0);
                        long activeUser = Utils.convertLong(list.get(i).get("active_user"), 0);
                        CountryReportMetrics one = metricsMap.get(countryCode);
                        if (one == null) {
                            one = new CountryReportMetrics();
                            metricsMap.put(countryCode, one);
                            resultList.add(one);
                        }
                        one.countryCode = countryCode;
                        one.countryName = countryCode;
                        one.totalInstalled = totalIntalled;
                        one.totalUninstalled = totalUninstalled;
                        one.todayUninstalled = todayUninstalled;
                        one.totalUser = totalUser;
                        one.activeUser = activeUser;
                        one.uninstallRate = one.totalInstalled > 0 ? (one.todayUninstalled * 1.0f / one.totalInstalled) : 0;
                    }

                    sql = "select country_code, sum(estimated_revenue) as estimated_revenue " +
                            "from app_user_life_time_history " +
                            "where install_date between '" + startDate + "' and '" + endDate + "' ";
                    if (appIds.size() > 0) {
                        String ss = "";
                        for (int i = 0; i < appIds.size(); i++) {
                            if (i < appIds.size() - 1) {
                                ss += "'" + appIds.get(i) + "',";
                            } else {
                                ss += "'" + appIds.get(i) + "'";
                            }
                        }
                        sql += " and app_id in (" + ss + ")";
                    }
                    sql += " group by country_code";
                    list = DB.findListBySql(sql);
                    for (int i = 0; i < list.size(); i++) {
                        String countryCode = list.get(i).get("country_code");
                        double estimatedRevenue = Utils.convertDouble(list.get(i).get("estimated_revenue"), 0);
                        CountryReportMetrics one = metricsMap.get(countryCode);
                        if (one == null) {
                            one = new CountryReportMetrics();
                            metricsMap.put(countryCode, one);
                            resultList.add(one);
                        }
                        one.countryCode = countryCode;
                        one.countryName = countryCode;
                        one.estimatedRevenue = estimatedRevenue;
                    }

                    //花费、成本、安装、点击等数据来源于 投放系统 从广告后台 拉取的数据
                    sql = "select country_code, sum(spend) as cost, sum(installed) as purchasedUser " +
                            "from app_ads_daily_metrics_history " +
                            "where date between '" + startDate + "' and '" + endDate + "' ";
                    if (appIds.size() > 0) {
                        String ss = "";
                        for (int i = 0; i < appIds.size(); i++) {
                            if (i < appIds.size() - 1) {
                                ss += "'" + appIds.get(i) + "',";
                            } else {
                                ss += "'" + appIds.get(i) + "'";
                            }
                        }
                        sql += " and app_id in (" + ss + ")";
                    }
                    sql += " group by country_code";
                    list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        String countryCode = list.get(i).get("country_code");
                        double cost = Utils.convertDouble(list.get(i).get("cost"), 0);
                        long purchasedUser = Utils.convertLong(list.get(i).get("purchasedUser"), 0);
                        CountryReportMetrics one = metricsMap.get(countryCode);
                        if (one == null) {
                            one = new CountryReportMetrics();
                            metricsMap.put(countryCode, one);
                            resultList.add(one);
                        }
                        one.countryCode = countryCode;
                        one.countryName = countryCode;
                        one.cost = cost;
                        one.purchasedUser = purchasedUser;
                        one.cpa = one.purchasedUser > 0 ? one.cost / one.purchasedUser : 0;
                        one.incoming = one.revenue - one.cost;
                    }

                    sql = "select country_code, sum(user_num_nature) as user_num_nature, " +//自然量 用户数
                            " sum(revenue_purchase) as user_num_purchase, " +//购买量 用户数
                            " sum(revenue_total) as user_num_total, " +//新安装 总用户数
                            " sum(revenue_nature) as revenue_nature, " +//自然量 收益
                            " sum(revenue_purchase) as revenue_purchase, " +//购买量 收益
                            " sum(revenue_total) as revenue_total " +//新安装 总收益
                            " from app_first_install_data " +
                            " where date between '" + startDate + "' and '" + endDate + "' ";
                    if (appIds.size() > 0) {
                        String ss = "";
                        for (int i = 0; i < appIds.size(); i++) {
                            if (i < appIds.size() - 1) {
                                ss += "'" + appIds.get(i) + "',";
                            } else {
                                ss += "'" + appIds.get(i) + "'";
                            }
                        }
                        sql += " and app_id in (" + ss + ")";
                    }
                    sql += " group by country_code";
                    list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        String countryCode = list.get(i).get("country_code");
                        double revenueNature = Utils.convertDouble(list.get(i).get("revenue_nature"), 0);//自然量收益
                        double revenuePurchase = Utils.convertDouble(list.get(i).get("revenue_purchase"), 0); //当日购买用户总、收益
                        double revenueNow = Utils.convertDouble(list.get(i).get("revenue_total"), 0); //当日新安装用户总收益
                        long natureUser = Utils.convertLong(list.get(i).get("user_num_nature"), 0);//自然量用户数
                        long purchaseUser = Utils.convertLong(list.get(i).get("user_num_purchase"), 0);//购买量用户数
                        long totalUser = Utils.convertLong(list.get(i).get("user_num_total"), 0);//当日新安装总用户数
                        CountryReportMetrics one = metricsMap.get(countryCode);
                        if (one == null) {
                            one = new CountryReportMetrics();
                            metricsMap.put(countryCode, one);
                            resultList.add(one);
                        }
                        one.countryCode = countryCode;
                        one.countryName = countryCode;
                        one.natureUser = natureUser;
                        one.purchaseUser = purchaseUser;
                        //one.totalUser = totalUser;//不能修改原来的逻辑值，新安装用户收益 仅使用 自然量、购买量比例运算
                        one.natureRevenue = revenueNature;//自然量 用户收益
                        one.purchaseRevenue = revenuePurchase;//购买安装用户收益
                        one.nowRevenue = revenueNow;//当日 购买用户总收益
                    }
                    metricsMap.clear();

                    HashMap<String, String> countryMap = Utils.getCountryMap();
                    String countryName = null;
                    for (int i = index; i < resultList.size() && i < (index + size); i++) {
                        CountryReportMetrics one = resultList.get(i);
                        countryName = countryMap.get(one.countryCode);
                        one.countryName = (countryName == null ? one.countryCode : countryName);
                        if (one.estimatedRevenue == 0) {
                            CountryARPU item = arpuHashMap.get(one.countryCode);
                            if (item != null && item.activeUser > 0) {
                                one.estimatedRevenue = one.totalInstalled * item.estimatedRevenue / item.activeUser;
                            }
                        }
                        long totalUser = one.purchaseUser + one.natureUser;
                        //System.out.println("总用户数"+totalUser);
                        //one.nowRevenue = revenue_now;//当日 购买用户总收益
                        /*if(one.natureRevenue == 0){
                            one.natureRevenue = totalUser > 0 ? one.nowRevenue * one.natureUser/totalUser : 0;//自然量 用户收益
                        }
                        if(one.purchaseRevenue == 0){
                            one.purchaseRevenue = totalUser > 0 ? one.nowRevenue * one.purchaseUser/totalUser : 0;//购买安装用户收益
                        }*/
                    }


                    int orderIndex = order;
                    resultList.sort(new Comparator<CountryReportMetrics>() {
                        @Override
                        public int compare(CountryReportMetrics o1, CountryReportMetrics o2) {
                            double ret = 0;
                            switch (orderIndex) {
                                case 0:
                                    //ret = o1.countryName.compareTo(o2.countryName);
                                    ret = o1.countryCode.compareTo(o2.countryCode);
                                    break;
                                case 1:
                                    ret = o1.cost - o2.cost;
                                    break;
                                case 2:
                                    ret = o1.purchasedUser - o2.purchasedUser;
                                    break;
                                case 3:
                                    ret = o1.totalInstalled - o2.totalInstalled;
                                    break;
                                case 4:
                                    ret = o1.totalUninstalled - o2.totalUninstalled;
                                    break;
                                case 5:
                                    ret = o1.uninstallRate - o2.uninstallRate;
                                    break;
                                case 6:
                                    ret = o1.totalUser - o2.totalUser;
                                    break;
                                case 7:
                                    ret = o1.activeUser - o2.activeUser;
                                    break;
                                case 8:
                                    ret = o1.cpa - o2.cpa;
                                    break;
                                case 9:
                                    ret = o1.revenue - o2.revenue;
                                    break;
                                case 10:
                                    ret = o1.nowRevenue - o2.nowRevenue;
                                    break;
                                case 11:
                                    ret = o1.purchaseRevenue - o2.purchaseRevenue;
                                    break;
                                case 12:
                                    ret = o1.ecpm - o2.ecpm;
                                    break;
                                case 13:
                                    ret = o1.incoming - o2.incoming;
                                    break;
                                case 14:
                                    ret = o1.estimatedRevenue - o2.estimatedRevenue;
                                    break;
                            }
                            if (ret > 0) {
                                return desc ? -1 : 1;
                            } else if (ret == 0) {
                                return 0;
                            } else {
                                return desc ? 1 : -1;
                            }
                        }
                    });


                    JsonArray array = new JsonArray();
                    for (int i = index; i < resultList.size() && i < (index + size); i++) {
                        JsonObject jsonObject = new JsonObject();
                        CountryReportMetrics one = resultList.get(i);

                        //jsonObject.addProperty("country_code", one.countryCode);
                        jsonObject.addProperty("country_name", one.countryName);
                        jsonObject.addProperty("cost", Utils.trimDouble(one.cost));
                        //jsonObject.addProperty("revenue_now", Utils.trimDouble(one.nowRevenue));//当日新安装用户广告收益数据
                        jsonObject.addProperty("purchased_user", one.purchasedUser);
                        jsonObject.addProperty("total_installed", one.totalInstalled);
                        jsonObject.addProperty("total_uninstalled", one.totalUninstalled);
                        jsonObject.addProperty("uninstalled_rate", one.uninstallRate);
                        jsonObject.addProperty("cpa", Utils.trimDouble(one.cpa));
                        jsonObject.addProperty("active_user", one.activeUser);
                        jsonObject.addProperty("total_user", one.totalUser);
                        jsonObject.addProperty("revenue", Utils.trimDouble(one.revenue));
                        jsonObject.addProperty("ecpm", Utils.trimDouble(one.ecpm * 1000));
                        jsonObject.addProperty("incoming", Utils.trimDouble(one.incoming));
                        jsonObject.addProperty("estimated_revenue", Utils.trimDouble(resultList.get(i).estimatedRevenue));

                        jsonObject.addProperty("nature_user", one.natureUser);//自然量用户数
                        jsonObject.addProperty("revenue_nature", Utils.trimDouble(one.natureRevenue));//自然量 用户收益
                        jsonObject.addProperty("revenue_purchase", Utils.trimDouble(one.purchaseRevenue));//购买安装用户收益
                        jsonObject.addProperty("revenue_now", Utils.trimDouble(one.nowRevenue));//当日 购买用户总收益

                        array.add(jsonObject);
                    }

                    json.addProperty("ret", 1);
                    json.addProperty("message", "成功");
                    json.addProperty("total", resultList.size());
                    json.add("data", array);
                } catch (Exception ex) {
                    json.addProperty("ret", 0);
                    json.addProperty("message", ex.getMessage());
                }
            }
        }

        response.getWriter().write(json.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    private class CountryARPU {
        public long activeUser;
        public double estimatedRevenue;
    }
    private HashMap<String, CountryARPU> fetchNearbyARPU(String date, ArrayList<String> appIds) {
        HashMap<String, CountryARPU> map = new HashMap<>();
        try {
            String sqlPart = "";
            if (appIds.size() > 0) {
                String ss = "";
                for (int i = 0; i < appIds.size(); i++) {
                    if (i < appIds.size() - 1) {
                        ss += "'" + appIds.get(i) + "',";
                    } else {
                        ss += "'" + appIds.get(i) + "'";
                    }
                }
                sqlPart += " and app_id in (" + ss + ")";
            }

            String sql = "select max(install_date) as target_date from app_user_life_time_history where install_date<? " + sqlPart;
            JSObject one = DB.findOneBySql(sql, date);
            if (one.hasObjectData()) {
                Date targetDate = one.get("target_date");

                sql = "select country_code, sum(active_count) as active_count " +
                        "from app_user_life_time_history " +
                        "where install_date=? and active_date=? " + sqlPart + " group by country_code";
                List<JSObject> list = DB.findListBySql(sql, targetDate, targetDate);
                for (int i = 0; i < list.size(); i++) {
                    String countryCode = list.get(i).get("country_code");
                    CountryARPU item = map.get(countryCode);
                    if (item == null) {
                        item = new CountryARPU();
                        map.put(countryCode, item);
                    }
                    item.activeUser = Utils.convertLong(list.get(i).get("active_count"), 0);
                }
                sql = "select country_code, sum(estimated_revenue) as estimated_revenue " +
                        "from app_user_life_time_history " +
                        "where install_date=? " + sqlPart + " group by country_code";
                list = DB.findListBySql(sql, targetDate);
                for (int i = 0; i < list.size(); i++) {
                    String countryCode = list.get(i).get("country_code");
                    CountryARPU item = map.get(countryCode);
                    if (item == null) {
                        item = new CountryARPU();
                        map.put(countryCode, item);
                    }
                    item.estimatedRevenue = Utils.convertDouble(list.get(i).get("estimated_revenue"), 0);
                }
            }
        } catch (Exception ex) {
        }
        return map;
    }
}
