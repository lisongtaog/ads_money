package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.Utils;
import com.bestgo.adsmoney.bean.AppData;
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

@WebServlet(name = "ActiveUserAdChance", urlPatterns = {"/active_user_ad_chance/*"})
public class ActiveUserAdChance extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/query")) {
                List<AppData> appDataList = AppManagement.fetchAllAppData();
                ArrayList<CountryReportMetrics> resultList = new ArrayList<>();

                String startDate = request.getParameter("start_date");
                String endDate = request.getParameter("end_date");
                int index = Utils.parseInt(request.getParameter("page_index"), 0);
                int size = Utils.parseInt(request.getParameter("page_size"), 20);
                String filter = request.getParameter("filter");
                String filterCountry = request.getParameter("filterCountry");
                int order = Utils.parseInt(request.getParameter("order"), 0);
                boolean desc = order < 1000;
                if (order > 1000) order = order - 1000;

                if (filter == null || filter.isEmpty()) {
                    filter = "";
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
                    String sql = "select date, country_code, app_id, user_day1, ad_chance_day1, ad_chance_day1/user_day1 as ad_chance_rate_day1, " +
                            "user_day2, ad_chance_day2, ad_chance_day2/user_day2 as ad_chance_rate_day2, " +
                            "user_day3, ad_chance_day3, ad_chance_day3/user_day3 as ad_chance_rate_day3, " +
                            "user_day4, ad_chance_day4, ad_chance_day4/user_day4 as ad_chance_rate_day4, " +
                            "user_day2/user_day1 as active_user_rate_day2, user_day3/user_day1 as active_user_rate_day3, user_day4/user_day1 as active_user_rate_day4 " +
                            "from app_active_user_ad_chance " +
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
                    String[] orders = {" order by date"," order by country_code", " order by app_id",
                            "order by user_day1", "order by ad_chance_day1", "order by ad_chance_rate_day1",
                            "order by user_day2", "order by ad_chance_day2", "order by ad_chance_rate_day2",
                            "order by user_day3", "order by ad_chance_day3", "order by ad_chance_rate_day3",
                            "order by user_day4", "order by ad_chance_day4", "order by ad_chance_rate_day4",
                            "order by active_user_rate_day2", "order by active_user_rate_day3", "order by active_user_rate_day4",
                    };
                    if (order < orders.length) {
                        sql += orders[order] + (desc ? " desc" : "");
                    }
                    sql += " limit " + index * size + "," + size;

                    List<JSObject> list = DB.findListBySql(sql);

                    JsonArray array = new JsonArray();
                    for (int i = 0; i < list.size(); i++) {
                        JsonObject jsonObject = new JsonObject();
                        JSObject one = list.get(i);
                        HashMap<String, String> countryMap = Utils.getCountryMap();
                        String countryName = countryMap.get(one.get("country_code"));
                        jsonObject.addProperty("country_name", countryName == null ? one.get("country_code") : countryName);
                        String appName = "";
                        for (int j = 0; j < appDataList.size(); j++) {
                            if (appDataList.get(j).appId.equals(one.get("app_id"))) {
                                appName = appDataList.get(j).appName;
                                break;
                            }
                        }
                        jsonObject.addProperty("date", one.get("date").toString());
                        jsonObject.addProperty("app_name", appName);
                        jsonObject.addProperty("active_user_day1", Utils.convertLong(one.get("user_day1"), 0));
                        jsonObject.addProperty("ad_chance_day1", Utils.convertLong(one.get("ad_chance_day1"), 0));
                        jsonObject.addProperty("ad_chance_rate_day1", Utils.trimDouble(Utils.convertDouble(one.get("ad_chance_rate_day1"), 0)));

                        jsonObject.addProperty("active_user_day2", Utils.convertLong(one.get("user_day2"), 0));
                        jsonObject.addProperty("ad_chance_day2", Utils.convertLong(one.get("ad_chance_day2"), 0));
                        jsonObject.addProperty("ad_chance_rate_day2", Utils.trimDouble(Utils.convertDouble(one.get("ad_chance_rate_day2"), 0)));

                        jsonObject.addProperty("active_user_day3", Utils.convertLong(one.get("user_day3"), 0));
                        jsonObject.addProperty("ad_chance_day3", Utils.convertLong(one.get("ad_chance_day3"), 0));
                        jsonObject.addProperty("ad_chance_rate_day3", Utils.trimDouble(Utils.convertDouble(one.get("ad_chance_rate_day3"), 0)));

                        jsonObject.addProperty("active_user_day4", Utils.convertLong(one.get("user_day4"), 0));
                        jsonObject.addProperty("ad_chance_day4", Utils.convertLong(one.get("ad_chance_day4"), 0));
                        jsonObject.addProperty("ad_chance_rate_day4", Utils.trimDouble(Utils.convertDouble(one.get("ad_chance_rate_day4"), 0)));

                        jsonObject.addProperty("active_user_rate_day2", Utils.trimDouble(Utils.convertDouble(one.get("active_user_rate_day2"), 0)));
                        jsonObject.addProperty("active_user_rate_day3", Utils.trimDouble(Utils.convertDouble(one.get("active_user_rate_day3"), 0)));
                        jsonObject.addProperty("active_user_rate_day4", Utils.trimDouble(Utils.convertDouble(one.get("active_user_rate_day4"), 0)));

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
}
