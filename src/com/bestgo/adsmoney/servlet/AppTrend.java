package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.utils.NumberUtil;
import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.adsmoney.bean.AppMonitorMetrics;
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

@WebServlet(name = "AppTrend", urlPatterns = {"/app_trend/*"})
public class AppTrend extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            //get对应的是折线图，query对应的是table
            if (path.startsWith("/query") || path.equals("/get")) {
                HashMap<Date, AppMonitorMetrics> metricsMap = new HashMap<>();
                ArrayList<AppMonitorMetrics> tmpDataList = new ArrayList<>();
                ArrayList<AppMonitorMetrics> resultList = new ArrayList<>();

                int period = Utils.parseInt(request.getParameter("period"), 1);
                Date start = null;
                Date end = null;
                String endDate = request.getParameter("end_date");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    end = sdf.parse(endDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(end);
                    if (period == 3) {
                        cal.add(Calendar.MONTH, -2);
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                    } else {
                        cal.add(Calendar.DAY_OF_MONTH, -90);
                    }
                    start = cal.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String startDate = sdf.format(start);
                int index = Utils.parseInt(request.getParameter("page_index"), 0);
                int size = Utils.parseInt(request.getParameter("page_size"), 20);
                String filter = request.getParameter("filter");
                String filterCountry = request.getParameter("filterCountry");

                if (filter == null || filter.isEmpty()) {
                    filter = "";
                }
                if (filterCountry == null || filterCountry.isEmpty()) {
                    filterCountry = "";
                }
                ArrayList<String> appIds = new ArrayList<>();
                ArrayList<String> countryCodes = new ArrayList<>();
                String[] filters = filter.split(",");
                for (String appId : filters) {
                    if (appId.isEmpty()) continue;
                    appIds.add(appId);
                }
                filters = filterCountry.split(",");
                for (String countryCode : filters) {
                    if (countryCode.isEmpty()) continue;
                    countryCodes.add(countryCode);
                }
                String appIdPart = null;
                if (appIds.size() > 0) {
                    appIdPart = " AND (";
                    for (int i = 0,len = appIds.size(); i < len; i++) {
                        if (i < len - 1) {
                            appIdPart += " app_id = '" + appIds.get(i) + "' OR ";
                        } else {
                            appIdPart += " app_id = '" + appIds.get(i) + "' ";
                        }
                    }
                    appIdPart += ") ";
                }
                String countryCodePart = null;
                if (countryCodes.size() > 0) {
                    countryCodePart = " AND (";
                    for (int i = 0,len = countryCodes.size(); i < len; i++) {
                        if (i < len - 1) {
                            countryCodePart += " country_code = '" + countryCodes.get(i) + "' OR ";
                        } else {
                            countryCodePart += " country_code = '" + countryCodes.get(i) + "' ";
                        }
                    }
                    countryCodePart += ") ";
                }

                try {
                    JSObject js = null;
                    AppMonitorMetrics one = null;

                    //app_daily_metrics_history表得到安装日期应用国家维度的总收入，总展示
                    String sql = "select date, sum(ad_revenue) as ad_revenue, \n" +
                            "sum(ad_impression) as ad_impression\n" +
                            "from app_daily_metrics_history\n" +
                            "where date between '" + startDate + "' and '" + endDate + "' \n" +
                            (appIds.size() > 0 ?  appIdPart : "") +
                            (countryCodes.size() > 0 ?  countryCodePart : "") +
                            "GROUP BY date\n" ;

                    List<JSObject> list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        Date date = list.get(i).get("date");
                        double revenue = Utils.trimDouble(Utils.convertDouble(list.get(i).get("ad_revenue"), 0));
                        long impression = Utils.convertLong(list.get(i).get("ad_impression"), 0);
                        one = metricsMap.get(date);
                        if (one == null) {
                            one = new AppMonitorMetrics();
                            metricsMap.put(date, one);
                            tmpDataList.add(one);
                        }
                        one.date = date;
                        one.revenue = revenue;
                        one.impression = impression;
                        one.ecpm = impression > 0 ? revenue / impression : 0;
                    }

                    //app_firebase_daily_metrics_history得到安装日期应用国家维度的总安装、总卸载、总用户数、总活跃用户数
                    sql = "SELECT date, sum(installed) as total_installed, \n" +
                            "sum(uninstalled) as total_uninstalled, \n" +
                            "sum(today_uninstalled) as today_uninstalled, \n" +
                            "sum(total_user) as total_user, \n" +
                            "sum(active_user) as active_user\n" +
                            "FROM app_firebase_daily_metrics_history\n" +
                            "WHERE date between '" + startDate + "' and '" + endDate + "'\n" +
                            (appIds.size() > 0 ?  appIdPart : "") +
                            (countryCodes.size() > 0 ?  countryCodePart : "") +
                            "GROUP BY date";
                    list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        Date date = list.get(i).get("date");
                        long totalIntalled = Utils.convertLong(list.get(i).get("total_installed"), 0);
                        long totalUnIntalled = Utils.convertLong(list.get(i).get("total_uninstalled"), 0);
                        long todayUninstalled = Utils.convertLong(list.get(i).get("today_uninstalled"), 0);
                        long totalUser = Utils.convertLong(list.get(i).get("total_user"), 0);
                        long activeUser = Utils.convertLong(list.get(i).get("active_user"), 0);
                        one = metricsMap.get(date);
                        if (one == null) {
                            one = new AppMonitorMetrics();
                            metricsMap.put(date, one);
                            tmpDataList.add(one);
                        }
                        one.date = date;
                        one.totalInstalled = totalIntalled;
                        one.totalUninstalled = totalUnIntalled;
                        one.todayUninstalled = todayUninstalled;
                        one.totalUser = totalUser;
                        one.activeUser = activeUser;
                        one.arpu = one.activeUser > 0 ? (float)(one.revenue / one.activeUser) : 0;
                        one.uninstallRate = one.totalInstalled > 0 ? (one.todayUninstalled * 1.0f / one.totalInstalled) : 0;
                    }

                    //app_ads_impressions_statistics表，得到安装日期下每展示日期下应用国家维度的累计总展示
//                    sql = "SELECT installed_date,SUM(impressions) AS sum_impressions \n" +
//                            "FROM app_ads_impressions_statistics\n" +
//                            "WHERE installed_date BETWEEN '" + startDate + "' AND '" + endDate + "' " +
//                            (appIds.size() > 0 ?  appIdPart : "") +
//                            (countryCodes.size() > 0 ?  countryCodePart : "") +
//                            "AND event_date BETWEEN installed_date AND '" + endDate + "'\n" +
//                            "GROUP BY installed_date";
                    //app_ads_impressions_statistics表，得到安装日期下每展示日期下应用国家维度的累计总展示
                    sql = "SELECT web_app_ads_sum_impression.installed_date,\n" +
                            "SUM(sum_impressions) AS sum_impression\n" +
                            "FROM web_app_ads_sum_impression\n" +
                            "WHERE installed_date BETWEEN '" + startDate + "' AND '" + endDate + "' " +
                            (appIds.size() > 0 ?  appIdPart : "") +
                            (countryCodes.size() > 0 ?  countryCodePart : "") +
                            "GROUP BY installed_date";
                    list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        js = list.get(i);
                        Date date = js.get("installed_date");
                        double sumImpressions = Utils.trimDouble(Utils.convertDouble(js.get("sum_impression"), 0));
                        one = metricsMap.get(date);
                        if (one == null) {
                            one = new AppMonitorMetrics();
                            metricsMap.put(date, one);
                            tmpDataList.add(one);
                        }
                        one.date = date;
                        one.avgSumImpression = one.totalInstalled > 0 ? sumImpressions / one.totalInstalled : 0;
                    }

                    //app_user_life_time_history表，得到安装日期应用国家维度的预估收入
                    sql = "select install_date, sum(estimated_revenue) as estimated_revenue\n" +
                            "from app_user_life_time_history\n" +
                            "where install_date between '" + startDate + "' and '" + endDate + "' \n" +
                            (appIds.size() > 0 ?  appIdPart : "") +
                            (countryCodes.size() > 0 ?  countryCodePart : "") +
                            "GROUP BY install_date";
                    list = DB.findListBySql(sql);
                    for (int i = 0; i < list.size(); i++) {
                        Date date = list.get(i).get("install_date");
                        double estimatedRevenue = Utils.convertDouble(list.get(i).get("estimated_revenue"), 0);
                        one = metricsMap.get(date);
                        if (one == null) {
                            one = new AppMonitorMetrics();
                            metricsMap.put(date, one);
                            tmpDataList.add(one);
                        }
                        one.date = date;
                        one.estimatedRevenue = estimatedRevenue;
                    }
                    //app_ads_daily_metrics_history表，得到安装日期应用国家维度的总花费、总购买用户数
                    sql = "select date, sum(spend) as cost, sum(installed) as purchasedUser\n" +
                            "from app_ads_daily_metrics_history\n" +
                            "where date between '" + startDate + "' and '" + endDate + "' \n" +
                            (appIds.size() > 0 ?  appIdPart : "") +
                            (countryCodes.size() > 0 ?  countryCodePart : "") +
                            "GROUP BY date";
                    list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        Date date = list.get(i).get("date");
                        double cost = Utils.convertDouble(list.get(i).get("cost"), 0);
                        long purchasedUser = Utils.convertLong(list.get(i).get("purchasedUser"), 0);
                        one = metricsMap.get(date);
                        if (one == null) {
                            one = new AppMonitorMetrics();
                            metricsMap.put(date, one);
                            tmpDataList.add(one);
                        }
                        one.date = date;
                        one.cost = cost;
                        one.purchasedUser = purchasedUser;
                        one.cpa = one.purchasedUser > 0 ? one.cost / one.purchasedUser : 0;
                        one.incoming = one.revenue - one.cost;
                        one.cpaDivEcpm = one.ecpm > 0 ? one.cpa / one.ecpm : 0;
                    }

                    /*sql = "select date, action, sum(value) as value " +
                            "from app_recommend_daily_history " +
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
                    if (countryCodes.size() > 0) {
                        String ss = "";
                        for (int i = 0; i < countryCodes.size(); i++) {
                            if (i < countryCodes.size() - 1) {
                                ss += "'" + countryCodes.get(i) + "',";
                            } else {
                                ss += "'" + countryCodes.get(i) + "'";
                            }
                        }
                        sql += " and country_code in (" + ss + ")";
                    }
                    sql += " group by date, action order by date desc";
                    list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        Date date = list.get(i).get("date");
                        String action = list.get(i).get("action");
                        long value = Utils.convertLong(list.get(i).get("value"), 0);
                        AppMonitorMetrics one = metricsMap.get(date);
                        if (one == null) {
                            one = new AppMonitorMetrics();
                            metricsMap.put(date, one);
                            tmpDataList.add(one);
                        }
                        if (action.equals("显示")) {
                            one.recommendImpression = value;
                        }
                        if (action.equals("点击")) {
                            one.recommendClick = value;
                        }
                        if (action.equals("安装")) {
                            one.recommendInstalled = value;
                        }
                    }*/

                    metricsMap.clear();
                    Collections.sort(tmpDataList, new Comparator<AppMonitorMetrics>() {
                        @Override
                        public int compare(AppMonitorMetrics o1, AppMonitorMetrics o2) {
                            int ret = o1.date.compareTo(o2.date);
                            if (ret > 0) {
                                return -1;
                            } else if (ret == 0) {
                                return 0;
                            } else {
                                return 1;
                            }
                        }
                    });
                    int remainder = 1;
                    switch (period) {
                        case 2:
                            remainder = 7;
                            break;
                        case 3:
                            remainder = 30;
                            break;
                    }
                    one = new AppMonitorMetrics();
                    double lastARPU = -1;
                    int lastMonth = -1;
                    for (int i = 0; i < tmpDataList.size(); i++) {
                        if (remainder == 30) {
                            if (lastMonth != tmpDataList.get(i).date.getMonth()) {
                                one = new AppMonitorMetrics();
                                one.date = tmpDataList.get(i).date;
                                one.date.setDate(1);
                                resultList.add(one);
                                lastMonth = one.date.getMonth();
                            }
                        } else {
                            if (i % remainder == 0) {
                                one = new AppMonitorMetrics();
                                one.date = tmpDataList.get(i).date;
                                resultList.add(one);
                            }
                        }
                        one.cost += tmpDataList.get(i).cost;
                        one.purchasedUser += tmpDataList.get(i).purchasedUser;
                        one.totalInstalled += tmpDataList.get(i).totalInstalled;
                        one.recommendImpression += tmpDataList.get(i).recommendImpression;
                        one.recommendClick += tmpDataList.get(i).recommendClick;
                        one.recommendInstalled += tmpDataList.get(i).recommendInstalled;
                        one.totalUninstalled += tmpDataList.get(i).totalUninstalled;
                        one.todayUninstalled += tmpDataList.get(i).todayUninstalled;
                        one.totalUser += tmpDataList.get(i).totalUser;
                        one.activeUser += tmpDataList.get(i).activeUser;
                        one.revenue += tmpDataList.get(i).revenue;
                        one.impression += tmpDataList.get(i).impression;
                        one.avgSumImpression += tmpDataList.get(i).avgSumImpression;
                        one.cpa = one.purchasedUser > 0 ? one.cost / one.purchasedUser : 0;
                        one.arpu = one.activeUser > 0 ? (float)(one.revenue / one.activeUser) : 0;
                        one.uninstallRate = one.totalInstalled > 0 ? (one.todayUninstalled * 1.0f / one.totalInstalled) : 0;
                        one.ecpm = one.impression > 0 ? one.revenue / one.impression : 0;
                        one.cpaDivEcpm = one.ecpm > 0 ? one.cpa / one.ecpm : 0;
                        one.incoming = one.revenue - one.cost;
                        one.estimatedRevenue += tmpDataList.get(i).estimatedRevenue;
                        if (one.estimatedRevenue == 0) {
                            if (lastARPU == -1) {
                                lastARPU = fetchNearbyARPU(one.date.toString(), appIds, countryCodes);
                            }
                            one.estimatedRevenue = lastARPU * one.totalInstalled;
                        }
                    }

                    for (int i = 0; i < resultList.size(); i++) {
                        if (i != resultList.size() - 1) {
                            one = resultList.get(i);
                            AppMonitorMetrics two = resultList.get(i + 1);
                            if (two.totalUser > 0) {
                                one.totalUserTrend = Utils.trimFloat((one.totalUser - two.totalUser) * 1.0f / two.totalUser);
                            }
                            if (two.activeUser > 0) {
                                one.activeUserTrend = Utils.trimFloat((one.activeUser - two.activeUser) * 1.0f / two.activeUser);
                            }
                            if (two.revenue > 0) {
                                one.revenueTrend = Utils.trimFloat((float)((one.revenue - two.revenue) / two.revenue));
                            }
                            if (two.arpu > 0) {
                                one.arpuTrend = Utils.trimFloat((one.arpu - two.arpu) / two.arpu);
                            }
                        }
                    }
                    JsonArray array = new JsonArray();
                    if (path.equals("/get")) {
                        index = 0;
                        size = resultList.size();
                    }
                    AppMonitorMetrics appMonitorMetrics = null;
                    for (int i = index * size; i < resultList.size() && i < (index * size + size); i++) {
                        appMonitorMetrics = resultList.get(i);
                        JsonObject jsonObject = new JsonObject();
                        if (path.equals("/get")) {
                            jsonObject.addProperty("date", appMonitorMetrics.date.getTime());
                        } else {
                            jsonObject.addProperty("date", appMonitorMetrics.date.toString());
                        }
                        jsonObject.addProperty("cost", NumberUtil.trimDouble(appMonitorMetrics.cost,3));
                        jsonObject.addProperty("purchased_user", appMonitorMetrics.purchasedUser);
                        jsonObject.addProperty("recommend_impression", appMonitorMetrics.recommendImpression);
                        jsonObject.addProperty("recommend_click", appMonitorMetrics.recommendClick);
                        jsonObject.addProperty("recommend_installed", appMonitorMetrics.recommendInstalled);
                        jsonObject.addProperty("total_installed",appMonitorMetrics.totalInstalled);
                        jsonObject.addProperty("total_user", appMonitorMetrics.totalUser);
                        jsonObject.addProperty("total_user_trend", appMonitorMetrics.totalUserTrend);
                        jsonObject.addProperty("active_user", appMonitorMetrics.activeUser);
                        jsonObject.addProperty("active_user_trend", appMonitorMetrics.activeUserTrend);
                        jsonObject.addProperty("revenue", NumberUtil.trimDouble(appMonitorMetrics.revenue,3));
                        jsonObject.addProperty("revenue_trend", appMonitorMetrics.revenueTrend);
                        jsonObject.addProperty("arpu", NumberUtil.trimDouble(appMonitorMetrics.arpu * 10000,3));
                        jsonObject.addProperty("arpu_trend", appMonitorMetrics.arpuTrend);

                        jsonObject.addProperty("total_uninstalled", appMonitorMetrics.totalUninstalled);
                        jsonObject.addProperty("uninstalled_rate", NumberUtil.trimDouble(appMonitorMetrics.uninstallRate,3));
                        jsonObject.addProperty("cpa", NumberUtil.trimDouble(appMonitorMetrics.cpa,3));
                        //ecpm = 总收入/总展示*1000，所以这里要乘以1000
                        jsonObject.addProperty("ecpm", NumberUtil.trimDouble(appMonitorMetrics.ecpm * 1000,3));
                        //这里要除以1000
                        jsonObject.addProperty("cpa_div_ecpm", NumberUtil.trimDouble(appMonitorMetrics.cpaDivEcpm / 1000,3));
                        jsonObject.addProperty("avg_sum_impression", NumberUtil.trimDouble(appMonitorMetrics.avgSumImpression,3));
                        jsonObject.addProperty("incoming", NumberUtil.trimDouble(appMonitorMetrics.incoming,3));
                        jsonObject.addProperty("estimated_revenue", NumberUtil.trimDouble(appMonitorMetrics.estimatedRevenue,3));

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

    /**
     * 获取最近的ARPU
     * @param date
     * @param appIds
     * @param countryCodes
     * @return
     */
    private double fetchNearbyARPU(String date, ArrayList<String> appIds, ArrayList<String> countryCodes) {
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
            if (countryCodes.size() > 0) {
                String ss = "";
                for (int i = 0; i < countryCodes.size(); i++) {
                    if (i < countryCodes.size() - 1) {
                        ss += "'" + countryCodes.get(i) + "',";
                    } else {
                        ss += "'" + countryCodes.get(i) + "'";
                    }
                }
                sqlPart += " and country_code in (" + ss + ")";
            }

            //在app_user_life_time_history表中找到它的最近日期
            String sql = "select max(install_date) as target_date from app_user_life_time_history where install_date<? " + sqlPart;
            JSObject one = DB.findOneBySql(sql, date);
            if (one.hasObjectData()) {
                Date targetDate = one.get("target_date");

                long activeCount = 0;
                //在app_user_life_time_history表中根据安装日期+最近日期，找到应用国家维度的总活跃数
                sql = "select sum(active_count) as active_count " +
                        "from app_user_life_time_history " +
                        "where install_date=? and active_date=? " + sqlPart;
                one = DB.findOneBySql(sql, targetDate, targetDate);
                if (one.hasObjectData()) {
                    activeCount = Utils.convertLong(one.get("active_count"), 0);
                }
                //在app_user_life_time_history表中根据安装日期，找到应用国家维度的总预估收入
                sql = "select sum(estimated_revenue) as estimated_revenue " +
                        "from app_user_life_time_history " +
                        "where install_date=? " + sqlPart;
                one = DB.findOneBySql(sql, targetDate);
                if (one.hasObjectData()) {
                    double estimatedRevenue = Utils.convertDouble(one.get("estimated_revenue"), 0);
                    //arpu=预估收入/活跃数
                    return estimatedRevenue / activeCount;
                }
            }
        } catch (Exception ex) {
        }
        return 0;
    }
}
