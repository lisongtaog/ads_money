package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.Utils;
import com.bestgo.adsmoney.bean.AppData;
import com.bestgo.adsmoney.bean.AppMonitorMetrics;
import com.bestgo.common.database.services.DB;
import com.bestgo.common.database.utils.JSObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.rmi.CORBA.Util;
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
                    cal.add(Calendar.DAY_OF_MONTH, -90);
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

                try {
                    String sql = "select date, sum(ad_revenue) as ad_revenue " +
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
                    sql += " group by date order by date desc";

                    List<JSObject> list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        Date date = list.get(i).get("date");
                        double revenue = Utils.trimDouble(Utils.convertDouble(list.get(i).get("ad_revenue"), 0));
                        AppMonitorMetrics one = metricsMap.get(date);
                        if (one == null) {
                            one = new AppMonitorMetrics();
                            metricsMap.put(date, one);
                            tmpDataList.add(one);
                        }
                        one.date = date;
                        one.revenue = revenue;
                    }

                    sql = "select date, sum(total_user) as total_user, sum(active_user) as active_user " +
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
                    sql += " group by date order by date desc";
                    list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        Date date = list.get(i).get("date");
                        long totalUser = Utils.convertLong(list.get(i).get("total_user"), 0);
                        long activeUser = Utils.convertLong(list.get(i).get("active_user"), 0);
                        AppMonitorMetrics one = metricsMap.get(date);
                        if (one == null) {
                            one = new AppMonitorMetrics();
                            metricsMap.put(date, one);
                            tmpDataList.add(one);
                        }
                        one.date = date;
                        one.totalUser = totalUser;
                        one.activeUser = activeUser;
                        one.arpu = one.totalUser > 0 ? (float)(one.revenue / one.totalUser) : 0;
                    }
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
                    AppMonitorMetrics one = new AppMonitorMetrics();
                    for (int i = 0; i < tmpDataList.size(); i++) {
                        if (i % remainder == 0) {
                            one = new AppMonitorMetrics();
                            one.date = tmpDataList.get(i).date;
                            resultList.add(one);
                        }
                        one.totalUser += tmpDataList.get(i).totalUser;
                        one.activeUser += tmpDataList.get(i).activeUser;
                        one.revenue += tmpDataList.get(i).revenue;
                        one.arpu = one.totalUser > 0 ? (float)(one.revenue / one.totalUser) : 0;
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
                    for (int i = index; i < resultList.size() && i < (index + size); i++) {
                        JsonObject jsonObject = new JsonObject();
                        if (path.equals("/get")) {
                            jsonObject.addProperty("date", resultList.get(i).date.getTime());
                        } else {
                            jsonObject.addProperty("date", resultList.get(i).date.toString());
                        }
                        jsonObject.addProperty("total_user", resultList.get(i).totalUser);
                        jsonObject.addProperty("total_user_trend", resultList.get(i).totalUserTrend);
                        jsonObject.addProperty("active_user", resultList.get(i).activeUser);
                        jsonObject.addProperty("active_user_trend", resultList.get(i).activeUserTrend);
                        jsonObject.addProperty("revenue", Utils.trimDouble(resultList.get(i).revenue));
                        jsonObject.addProperty("revenue_trend", resultList.get(i).revenueTrend);
                        jsonObject.addProperty("arpu", resultList.get(i).arpu);
                        jsonObject.addProperty("arpu_trend", resultList.get(i).arpuTrend);
                        array.add(jsonObject);
                    }

                    json.addProperty("ret", 1);
                    json.addProperty("message", "成功");
                    json.addProperty("total", list.size());
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
}
