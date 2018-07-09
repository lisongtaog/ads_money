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
 * @description 应用广告收入统计
 */
@WebServlet(name = "QueryAppAdsRevenueStatistics", urlPatterns = {"/query_app_ads_revenue_statistics"})
public class QueryAppAdsRevenueStatistics extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;
        String date = request.getParameter("date");
        String appId = request.getParameter("appId");
        String countryCode = request.getParameter("countryCode");

//        date = "2018-06-20";
//        appId = "com.androapplite.vpn10";
//        countryCode = "TM";
        JsonObject json = new JsonObject();
        try {
            if ("2018-06-27".compareTo(date) > 0) {
                json.addProperty("ret", 0);
                json.addProperty("message", "只能查询6月27号到前天的数据,今晚23点以后可查询昨天的数据");
            } else {
                String sql = "SELECT event_date,sum(revenue) AS total_revenue FROM app_ads_impressions_statistics " +
                        "WHERE installed_date = '" + date + "' " +
                        ("all".equals(appId) || appId.isEmpty() ? " " : "AND app_id = '" + appId + "' ") +
                        ("all".equals(countryCode) || countryCode.isEmpty() ? " " : "AND country_code = '" + countryCode + "' ") +
                        "AND event_date >= '" + date + "' " +
                        "GROUP BY event_date ORDER BY event_date";
                List<JSObject> revenueList = DB.findListBySql(sql);
                JsonArray array1 = new JsonArray();
                JsonArray array2 = new JsonArray();
                double sumRevenue = 0;
                for (int i = 0,len = revenueList.size();i < len;i++) {
                    JSObject revenueJS = revenueList.get(i);
                    if (revenueJS.hasObjectData()) {
                        String eventDate = revenueJS.get("event_date").toString();
                        array1.add(eventDate);
                        double totalRevenue = revenueJS.get("total_revenue");
                        sumRevenue += totalRevenue;
                        array2.add(sumRevenue);
                    }
                }
                json.add("date_array",array1);
                json.add("data_array",array2);
                json.addProperty("ret", 1);
            }

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
