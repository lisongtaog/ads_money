package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.Utils;
import com.bestgo.adsmoney.bean.AppMonitorMetrics;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
                if (order > 1000) order = order - 1000;

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

                    for (int i = 0; i < list.size(); i++) {
                        String countryCode = list.get(i).get("country_code");
                        double revenue = Utils.trimDouble(Utils.convertDouble(list.get(i).get("ad_revenue"), 0));
                        long impression = Utils.convertLong(list.get(i).get("ad_impression"), 0);
                        CountryReportMetrics one = metricsMap.get(countryCode);
                        if (one == null) {
                            one = new CountryReportMetrics();
                            metricsMap.put(countryCode, one);
                            resultList.add(one);
                        }
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

                    for (int i = 0; i < list.size(); i++) {
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
                        one.countryName = countryCode;
                        one.totalInstalled = totalIntalled;
                        one.totalUninstalled = totalUninstalled;
                        one.todayUninstalled = todayUninstalled;
                        one.totalUser = totalUser;
                        one.activeUser = activeUser;
                        one.uninstallRate = one.totalInstalled > 0 ? (one.todayUninstalled * 1.0f / one.totalInstalled) : 0;
                    }

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
                        one.countryName = countryCode;
                        one.cost = cost;
                        one.purchasedUser = purchasedUser;
                        one.cpa = one.purchasedUser > 0 ? one.cost / one.purchasedUser : 0;
                        one.incoming = one.revenue - one.cost;
                    }

                    metricsMap.clear();
                    Collections.sort(resultList, new Comparator<CountryReportMetrics>() {
                        @Override
                        public int compare(CountryReportMetrics o1, CountryReportMetrics o2) {
                            double ret = o1.cost - o2.cost;
                            if (ret > 0) {
                                return -1;
                            } else if (ret == 0) {
                                return 0;
                            } else {
                                return 1;
                            }
                        }
                    });
                    for (int i = 0; i < resultList.size(); i++) {
                        CountryReportMetrics one = resultList.get(i);
                        HashMap<String, String> countryMap = Utils.getCountryMap();
                        String countryName = countryMap.get(one.countryName);
                        one.countryName = (countryName == null ? one.countryName : countryName);
                    }

                    int orderIndex = order;
                    resultList.sort(new Comparator<CountryReportMetrics>() {
                        @Override
                        public int compare(CountryReportMetrics o1, CountryReportMetrics o2) {
                            double ret = 0;
                            switch (orderIndex) {
                                case 0:
                                    ret = o1.countryName.compareTo(o2.countryName);
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
                                    ret = o1.ecpm - o2.ecpm;
                                    break;
                                case 11:
                                    ret = o1.incoming - o2.incoming;
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
                        jsonObject.addProperty("country_name", one.countryName);
                        jsonObject.addProperty("cost", Utils.trimDouble(one.cost));
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
                        double arpu = one.activeUser > 0 ? (float)(one.revenue / one.activeUser) : 0;
                        double arpu1 = one.totalUser > 0 ? (float)(one.revenue / one.totalUser) : 0;
                        jsonObject.addProperty("estimated_revenue", Utils.trimDouble(estimateRevenue(one.purchasedUser,
                                one.uninstallRate, arpu, arpu1)));
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
