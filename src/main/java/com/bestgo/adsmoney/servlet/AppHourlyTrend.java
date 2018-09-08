package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.adsmoney.bean.AppHourlyMetrics;
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

@WebServlet(name = "AppHourlyTrend", urlPatterns = {"/app_hourly_trend/*"})
public class AppHourlyTrend extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/query") || path.equals("/get")) {
                String startDate = request.getParameter("start_date");
                String endDate = request.getParameter("end_date");
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
                    String sql = "select date, (case when hour=0 then 24 else hour end) as hour, cost, revenue, revenue-cost as incoming from (" +
                            "select date, hour, sum(cost) as cost, sum(revenue) as revenue " +
                            "from app_hourly_metrics_history " +
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
                    sql += "group by date, hour)t order by date, hour";

                    List<JSObject> list = DB.findListBySql(sql);

                    ArrayList<AppHourlyMetrics> metrics = new ArrayList<>();

                    for (int i = 0; i < list.size(); i++) {
                        AppHourlyMetrics one = new AppHourlyMetrics();
                        one.date = list.get(i).get("date");
                        one.hour = list.get(i).get("hour");
                        one.cost = list.get(i).get("cost");
                        one.revenue = list.get(i).get("revenue");
                        one.incoming = list.get(i).get("incoming");
                        metrics.add(one);
                    }

                    JsonArray array = new JsonArray();
                    if (path.equals("/get")) {
                        index = 0;
                        size = metrics.size();
                    }
                    for (int i = index * size; i < metrics.size() && i < (index * size + size); i++) {
                        JsonObject jsonObject = new JsonObject();
                        if (path.equals("/get")) {
                            jsonObject.addProperty("date", metrics.get(i).date.getTime());
                        } else {
                            jsonObject.addProperty("date", metrics.get(i).date.toString());
                        }
                        jsonObject.addProperty("hour", metrics.get(i).hour);
                        jsonObject.addProperty("cost", Utils.trimDouble(metrics.get(i).cost));
                        jsonObject.addProperty("revenue", Utils.trimDouble(metrics.get(i).revenue));
                        jsonObject.addProperty("incoming", Utils.trimDouble(metrics.get(i).incoming));
                        jsonObject.addProperty("cost", Utils.trimDouble(metrics.get(i).cost));
                        array.add(jsonObject);
                    }

                    json.addProperty("ret", 1);
                    json.addProperty("message", "成功");
                    json.addProperty("total", metrics.size());
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
