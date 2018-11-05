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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(name = "AppTrend", urlPatterns = {"/app_trend/*"})
public class AppTrend extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        //特殊用户标记
        HttpSession session = request.getSession();
        boolean isvisitor = session.getAttribute("isvisitor") == null ? false : true;

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

                //为特殊用户指定 特定应用
                if ("".equals(filter) && isvisitor){
                    filter = "com.solitaire.free.lj1," +
                            "com.collection.card.free," +
                            "com.ancient_card.free," +
                            "com.pyramid_card.free," +
                            "com.solitaire_star.card.free," +
                            "com.sudoku.four.mobidev," +
                            "com.chess.one.mobidev," +
                            "com.solitaire.yang1.freecards";
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
                    AppMonitorMetrics appMonitorMetrics = null;

                    //app_daily_metrics_history表得到安装日期应用国家维度的总收入，总展示
                    String sql = "select date, sum(ad_revenue) as ad_revenue,sum(sum_ad_revenue) as sum_ad_revenue, \n" +
                            "sum(ad_impression) as ad_impression\n" +
                            "from app_daily_metrics_history\n" +
                            "where date between '" + startDate + "' and '" + endDate + "' \n" +
                            (appIds.size() > 0 ?  appIdPart : "") +
                            (countryCodes.size() > 0 ?  countryCodePart : "") +
                            "GROUP BY date\n" ;

                    List<JSObject> list = DB.findListBySql(sql);
                    JSObject jsObject = null;
                    for (int i = 0; i < list.size(); i++) {
                        jsObject = list.get(i);
                        Date date = jsObject.get("date");
                        double revenue = Utils.trimDouble(Utils.convertDouble(jsObject.get("ad_revenue"), 0));
                        double sumRevenue = Utils.trimDouble(Utils.convertDouble(jsObject.get("sum_ad_revenue"), 0)); //累计收入
                        long impression = Utils.convertLong(jsObject.get("ad_impression"), 0);
                        appMonitorMetrics = metricsMap.get(date);
                        if (appMonitorMetrics == null) {
                            appMonitorMetrics = new AppMonitorMetrics();
                            metricsMap.put(date, appMonitorMetrics);
                            tmpDataList.add(appMonitorMetrics);
                        }
                        appMonitorMetrics.date = date;
                        appMonitorMetrics.revenue = revenue;
//                        appMonitorMetrics.sumRevenue = sumRevenue;
                        appMonitorMetrics.impression = impression;
                        appMonitorMetrics.ecpm = impression > 0 ? revenue / impression : 0;
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
                        jsObject = list.get(i);
                        Date date = jsObject.get("date");
                        long totalIntalled = Utils.convertLong(jsObject.get("total_installed"), 0);
                        long totalUnIntalled = Utils.convertLong(jsObject.get("total_uninstalled"), 0);
                        long todayUninstalled = Utils.convertLong(jsObject.get("today_uninstalled"), 0);
                        long totalUser = Utils.convertLong(jsObject.get("total_user"), 0);
                        long activeUser = Utils.convertLong(jsObject.get("active_user"), 0);
                        appMonitorMetrics = metricsMap.get(date);
                        if (appMonitorMetrics == null) {
                            appMonitorMetrics = new AppMonitorMetrics();
                            metricsMap.put(date, appMonitorMetrics);
                            tmpDataList.add(appMonitorMetrics);
                        }
                        appMonitorMetrics.date = date;
                        appMonitorMetrics.totalInstalled = totalIntalled;
                        appMonitorMetrics.totalUninstalled = totalUnIntalled;
                        appMonitorMetrics.todayUninstalled = todayUninstalled;
                        appMonitorMetrics.totalUser = totalUser;
                        appMonitorMetrics.activeUser = activeUser;
//                        appMonitorMetrics.arpu = appMonitorMetrics.activeUser > 0 ? (float)(appMonitorMetrics.revenue / appMonitorMetrics.activeUser) : 0;
                        appMonitorMetrics.uninstallRate = appMonitorMetrics.totalInstalled > 0 ? (appMonitorMetrics.todayUninstalled * 1.0f / appMonitorMetrics.totalInstalled) : 0;
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
                        jsObject = list.get(i);
                        Date date = jsObject.get("installed_date");
                        double sumImpressions = Utils.trimDouble(Utils.convertDouble(jsObject.get("sum_impression"), 0));
                        appMonitorMetrics = metricsMap.get(date);
                        if (appMonitorMetrics == null) {
                            appMonitorMetrics = new AppMonitorMetrics();
                            metricsMap.put(date, appMonitorMetrics);
                            tmpDataList.add(appMonitorMetrics);
                        }
                        appMonitorMetrics.date = date;
                        appMonitorMetrics.avgSumImpression = appMonitorMetrics.totalInstalled > 0 ? sumImpressions / appMonitorMetrics.totalInstalled : 0;
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
                        jsObject = list.get(i);
                        Date date = jsObject.get("install_date");
                        double estimatedRevenue = Utils.convertDouble(jsObject.get("estimated_revenue"), 0);
                        appMonitorMetrics = metricsMap.get(date);
                        if (appMonitorMetrics == null) {
                            appMonitorMetrics = new AppMonitorMetrics();
                            metricsMap.put(date, appMonitorMetrics);
                            tmpDataList.add(appMonitorMetrics);
                        }
                        appMonitorMetrics.date = date;
                        appMonitorMetrics.estimatedRevenue = estimatedRevenue;
                    }
                    //app_ads_daily_metrics_history表，得到安装日期应用国家维度的总花费、总购买用户数
                    sql = "select date, sum(spend) as cost, sum(sum_spend) as sum_cost, sum(installed) as purchasedUser\n" +
                            "from app_ads_daily_metrics_history\n" +
                            "where date between '" + startDate + "' and '" + endDate + "' \n" +
                            (appIds.size() > 0 ?  appIdPart : "") +
                            (countryCodes.size() > 0 ?  countryCodePart : "") +
                            "GROUP BY date";
                    list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        jsObject = list.get(i);
                        Date date = list.get(i).get("date");
                        double cost = Utils.convertDouble(jsObject.get("cost"), 0);
                        double sumCost = Utils.convertDouble(jsObject.get("sum_cost"), 0); //累计花费
                        long purchasedUser = Utils.convertLong(jsObject.get("purchasedUser"), 0);
                        appMonitorMetrics = metricsMap.get(date);
                        if (appMonitorMetrics == null) {
                            appMonitorMetrics = new AppMonitorMetrics();
                            metricsMap.put(date, appMonitorMetrics);
                            tmpDataList.add(appMonitorMetrics);
                        }
                        appMonitorMetrics.date = date;
                        appMonitorMetrics.cost = cost;
//                        appMonitorMetrics.sumCost = sumCost;
                        appMonitorMetrics.purchasedUser = purchasedUser;
                        appMonitorMetrics.cpa = appMonitorMetrics.purchasedUser > 0 ? appMonitorMetrics.cost / appMonitorMetrics.purchasedUser : 0;
                        appMonitorMetrics.incoming = appMonitorMetrics.revenue - appMonitorMetrics.cost;
                        appMonitorMetrics.cpaDivEcpm = appMonitorMetrics.ecpm > 0 ? appMonitorMetrics.cpa / appMonitorMetrics.ecpm : 0;
                    }

                    sql = "select date, sum(sum_cost) as sum_cost, sum(sum_revenue) as sum_ad_revenue\n" +
                            "from web_app_country_sum_cost_revenue\n" +
                            "where date between '" + startDate + "' and '" + endDate + "' \n" +
                            (appIds.size() > 0 ?  appIdPart : "") +
                            (countryCodes.size() > 0 ?  countryCodePart : "") +
                            "GROUP BY date";
                    list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        jsObject = list.get(i);
                        Date date = list.get(i).get("date");
                        double sumCost = Utils.convertDouble(jsObject.get("sum_cost"), 0); //累计花费
                        double sumRevenue = Utils.trimDouble(Utils.convertDouble(jsObject.get("sum_ad_revenue"), 0)); //累计收入
                        appMonitorMetrics = metricsMap.get(date);
                        if (appMonitorMetrics == null) {
                            appMonitorMetrics = new AppMonitorMetrics();
                            metricsMap.put(date, appMonitorMetrics);
                            tmpDataList.add(appMonitorMetrics);
                        }
                        appMonitorMetrics.date = date;
                        appMonitorMetrics.sumCost = sumCost;
                        appMonitorMetrics.sumRevenue = sumRevenue;
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
                    appMonitorMetrics = new AppMonitorMetrics();
                    double lastARPU = -1;
                    int lastMonth = -1;
                    for (int i = 0; i < tmpDataList.size(); i++) {
                        if (remainder == 30) {
                            if (lastMonth != tmpDataList.get(i).date.getMonth()) {
                                appMonitorMetrics = new AppMonitorMetrics();
                                appMonitorMetrics.date = tmpDataList.get(i).date;
                                appMonitorMetrics.date.setDate(1);
                                resultList.add(appMonitorMetrics);
                                lastMonth = appMonitorMetrics.date.getMonth();
                            }
                        } else {
                            if (i % remainder == 0) {
                                appMonitorMetrics = new AppMonitorMetrics();
                                appMonitorMetrics.date = tmpDataList.get(i).date;
                                resultList.add(appMonitorMetrics);
                            }
                        }
                        appMonitorMetrics.cost += tmpDataList.get(i).cost;
                        appMonitorMetrics.sumCost += tmpDataList.get(i).sumCost;
                        appMonitorMetrics.purchasedUser += tmpDataList.get(i).purchasedUser;
                        appMonitorMetrics.totalInstalled += tmpDataList.get(i).totalInstalled;
                        appMonitorMetrics.recommendImpression += tmpDataList.get(i).recommendImpression;
                        appMonitorMetrics.recommendClick += tmpDataList.get(i).recommendClick;
                        appMonitorMetrics.recommendInstalled += tmpDataList.get(i).recommendInstalled;
                        appMonitorMetrics.totalUninstalled += tmpDataList.get(i).totalUninstalled;
                        appMonitorMetrics.todayUninstalled += tmpDataList.get(i).todayUninstalled;
                        appMonitorMetrics.totalUser += tmpDataList.get(i).totalUser;
                        appMonitorMetrics.activeUser += tmpDataList.get(i).activeUser;
                        appMonitorMetrics.revenue += tmpDataList.get(i).revenue;
                        appMonitorMetrics.sumRevenue += tmpDataList.get(i).sumRevenue;
                        appMonitorMetrics.impression += tmpDataList.get(i).impression;
                        appMonitorMetrics.avgSumImpression += tmpDataList.get(i).avgSumImpression;
                        appMonitorMetrics.cpa = appMonitorMetrics.purchasedUser > 0 ? appMonitorMetrics.cost / appMonitorMetrics.purchasedUser : 0;
//                        one.arpu = one.activeUser > 0 ? (float)(one.revenue / one.activeUser) : 0;
                        appMonitorMetrics.uninstallRate = appMonitorMetrics.totalInstalled > 0 ? (appMonitorMetrics.todayUninstalled * 1.0f / appMonitorMetrics.totalInstalled) : 0;
                        appMonitorMetrics.ecpm = appMonitorMetrics.impression > 0 ? appMonitorMetrics.revenue / appMonitorMetrics.impression : 0;
                        appMonitorMetrics.cpaDivEcpm = appMonitorMetrics.ecpm > 0 ? appMonitorMetrics.cpa / appMonitorMetrics.ecpm : 0;
                        appMonitorMetrics.incoming = appMonitorMetrics.revenue - appMonitorMetrics.cost;
                        appMonitorMetrics.estimatedRevenue += tmpDataList.get(i).estimatedRevenue;
                        if (appMonitorMetrics.estimatedRevenue == 0) {
                            if (lastARPU == -1) {
                                lastARPU = fetchNearbyARPU(appMonitorMetrics.date.toString(), appIds, countryCodes);
                            }
                            appMonitorMetrics.estimatedRevenue = lastARPU * appMonitorMetrics.totalInstalled;
                        }
                    }

                    for (int i = 0; i < resultList.size(); i++) {
                        if (i != resultList.size() - 1) {
                            appMonitorMetrics = resultList.get(i);
                            AppMonitorMetrics two = resultList.get(i + 1);
                            if (two.totalUser > 0) {
                                appMonitorMetrics.totalUserTrend = Utils.trimFloat((appMonitorMetrics.totalUser - two.totalUser) * 1.0f / two.totalUser);
                            }
                            if (two.activeUser > 0) {
                                appMonitorMetrics.activeUserTrend = Utils.trimFloat((appMonitorMetrics.activeUser - two.activeUser) * 1.0f / two.activeUser);
                            }
                            if (two.revenue > 0) {
                                appMonitorMetrics.revenueTrend = Utils.trimFloat((float)((appMonitorMetrics.revenue - two.revenue) / two.revenue));
                            }
//                            if (two.arpu > 0) {
//                                one.arpuTrend = Utils.trimFloat((one.arpu - two.arpu) / two.arpu);
//                            }
                        }
                    }
                    JsonArray array = new JsonArray();
                    if (path.equals("/get")) {
                        index = 0;
                        size = resultList.size();
                    }
                    for (int i = index * size; i < resultList.size() && i < (index * size + size); i++) {
                        appMonitorMetrics = resultList.get(i);
                        JsonObject jsonObject = new JsonObject();
                        if (path.equals("/get")) {
                            jsonObject.addProperty("date", appMonitorMetrics.date.getTime());
                        } else {
                            jsonObject.addProperty("date", appMonitorMetrics.date.toString());
                        }
                        jsonObject.addProperty("cost", NumberUtil.trimDouble(appMonitorMetrics.cost,3));
                        jsonObject.addProperty("sumCost", NumberUtil.trimDouble(appMonitorMetrics.sumCost,3));
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
                        jsonObject.addProperty("sumRevenue", NumberUtil.trimDouble(appMonitorMetrics.sumRevenue,3));
                        jsonObject.addProperty("revenue_trend", appMonitorMetrics.revenueTrend);
//                        jsonObject.addProperty("arpu", NumberUtil.trimDouble(appMonitorMetrics.arpu * 10000,3));
//                        jsonObject.addProperty("arpu_trend", appMonitorMetrics.arpuTrend);

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
