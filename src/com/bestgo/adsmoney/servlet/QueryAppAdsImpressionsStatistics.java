package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.Utils;
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
import java.util.List;

/**
 * @author mengjun
 * @date 2018/7/5 14:25
 * @description 应用广告展示统计
 */
@WebServlet(name = "QueryAppAdsImpressionsStatistics", urlPatterns = {"/query_app_ads_impressions_statistics"})
public class QueryAppAdsImpressionsStatistics extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;
        String date = request.getParameter("date");
        String appId = request.getParameter("app_id");
        String countryCode = request.getParameter("country_code");

        JsonObject json = new JsonObject();
        try {
            String sql = "SELECT event_date,sum(revenue) AS total_revenue FROM app_ads_impressions_statistics " +
                    "WHERE installed_date = '" + date + "' " +
                    (null == appId || appId.isEmpty() ? " " : "AND app_id = '" + appId + "' ") +
                    (null == countryCode || countryCode.isEmpty() ? " " : "AND country_code = '" + countryCode + "' ") +
                    "GROUP BY event_date ORDER BY event_date";
            List<JSObject> revenueList = DB.findListBySql(sql);
            JsonArray array1 = new JsonArray();
            JsonArray array2 = new JsonArray();
            for (int i = 0,len = revenueList.size();i < len;i++) {
                JSObject revenueJS = revenueList.get(i);
                if (revenueJS.hasObjectData()) {
                    String eventDate = revenueJS.get("event_date").toString();
                    array1.add(eventDate);
                    double totalRevenue = revenueJS.get("total_revenue");
                    array2.add(totalRevenue);
                }
            }
            json.add("date_array",array1);
            json.add("data_array",array2);
            json.addProperty("ret", 1);
            System.out.println(json);
        } catch (Exception ex) {
            json.addProperty("ret", 0);
            json.addProperty("message", ex.getMessage());
        }

        response.getWriter().write(json.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }
}