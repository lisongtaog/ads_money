package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.OperationResult;
import com.bestgo.adsmoney.Utils;
import com.bestgo.adsmoney.bean.AppData;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@WebServlet(name = "AppReport", urlPatterns = {"/app_report/*"})
public class AppReport extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/query")) {
                String tableName = "app_daily_metrics_history";

                List<AppData> appData = AppManagement.fetchAllAppData();

                String startDate = request.getParameter("start_date");
                String endDate = request.getParameter("end_date");
                int index = Utils.parseInt(request.getParameter("page_index"), 0);
                int size = Utils.parseInt(request.getParameter("page_size"), 20);
                String dimension = request.getParameter("dimension");
                String filter = request.getParameter("filter");

                if (dimension == null || dimension.isEmpty()) {
                    dimension = "";
                }
                ArrayList<String> fields = new ArrayList<>();
                String[] dim = dimension.split(",");
                for (String d : dim) {
                    if (d.isEmpty()) continue;
                    switch (d) {
                        case "1":
                            fields.add("date");
                            break;
                        case "2":
                            fields.add("app_id");
                            break;
                        case "3":
                            tableName = "app_ad_unit_metrics_history";
                            fields.add("ad_unit_id");
                            break;
                        case "4":
                            fields.add("country_code");
                            break;
                        case "5":
                            fields.add("ad_network");
                            break;
                    }
                }
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
                    String sql = "select ";
                    String ff = "";
                    if (fields.size() > 0) {
                        for (int i = 0; i < fields.size(); i++) {
                            if (i < fields.size() - 1) {
                                ff += fields.get(i) + ",";
                            } else {
                                ff += fields.get(i);
                            }
                        }
                        sql += ff + ", ";
                    }
                    sql += " sum(ad_request) as ad_request, sum(ad_filled) as ad_filled, sum(ad_impression) as ad_impression, sum(ad_click) as ad_click, sum(ad_revenue) as ad_revenue " +
                            "from " + tableName + " " +
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
                    if (!ff.isEmpty()) {
                        sql += " group by " + ff;
                    }

                    List<JSObject> count = DB.findListBySql(sql);

                    if (fields.size() > 0) {
                        sql += " order by " + fields.get(0) + " desc";
                    }
                    sql += " limit " + index * size + "," + size;
                    List<JSObject> list = DB.findListBySql(sql);

                    JsonArray array = new JsonArray();
                    for (int i = 0; i < list.size(); i++) {
                        JsonObject one = new JsonObject();
                        for (int ii = 0; ii < fields.size(); ii++) {
                            String f = fields.get(ii);
                            String v = list.get(i).get(f).toString();
                            if (f.equals("country_code")) {
                                HashMap<String, String> countryMap = Utils.getCountryMap();
                                String countryName = countryMap.get(v);
                                one.addProperty("country_name", countryName == null ? v : countryName);
                            } else if (f.equals("app_id")) {
                                for (int jj = 0; jj < appData.size(); jj++) {
                                    if (appData.get(jj).appId.equals(v)) {
                                        one.addProperty("app_name", appData.get(jj).appName);
                                        break;
                                    }
                                }
                            }
                            one.addProperty(f, v);
                        }
                        one.addProperty("ad_request", list.get(i).get("ad_request").toString());
                        one.addProperty("ad_filled", list.get(i).get("ad_filled").toString());
                        one.addProperty("ad_impression", list.get(i).get("ad_impression").toString());
                        one.addProperty("ad_click", list.get(i).get("ad_click").toString());
                        one.addProperty("ad_revenue", Utils.trimDouble(list.get(i).get("ad_revenue")));
                        array.add(one);
                    }

                    json.addProperty("ret", 1);
                    json.addProperty("message", "成功");
                    json.addProperty("total", count.size());
                    json.add("data", array);
                } catch (Exception ex) {
                    json.addProperty("ret", 0);
                    json.addProperty("message", ex.getMessage());
                }
            } else if (path.equals("/get")) {
                String startDate = request.getParameter("start_date");
                String endDate = request.getParameter("end_date");
                String filter = request.getParameter("filter");

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
                    String sql = "select date, sum(ad_request) as ad_request, sum(ad_filled) as ad_filled, sum(ad_impression) as ad_impression, sum(ad_click) as ad_click, sum(ad_revenue) as ad_revenue " +
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
                    sql += " group by date order by date desc";
                    List<JSObject> list = DB.findListBySql(sql);

                    JsonArray array = new JsonArray();
                    for (int i = 0; i < list.size(); i++) {
                        JsonObject one = new JsonObject();
                        one.addProperty("date", ((Date)list.get(i).get("date")).getTime());
                        one.addProperty("ad_request", Utils.convertLong(list.get(i).get("ad_request"), 0));
                        one.addProperty("ad_filled", Utils.convertLong(list.get(i).get("ad_filled"), 0));
                        one.addProperty("ad_impression", Utils.convertLong(list.get(i).get("ad_impression"), 0));
                        one.addProperty("ad_click", Utils.convertLong(list.get(i).get("ad_click"), 0));
                        one.addProperty("ad_revenue", Utils.trimDouble(list.get(i).get("ad_revenue")));
                        array.add(one);
                    }

                    json.addProperty("ret", 1);
                    json.addProperty("message", "成功");
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
