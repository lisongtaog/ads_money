package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.Utils;
import com.bestgo.adsmoney.bean.AppAdsMetrics;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@WebServlet(name = "Dashboard", urlPatterns = "/dashboard")
public class Dashboard extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject json = new JsonObject();
        JsonArray arr = new JsonArray();

        Calendar now = Calendar.getInstance();
        String end = String.format("%d-%02d-%02d", now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
        now.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH) - 15);
        String start = String.format("%d-%02d-%02d", now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));

        String sql = "select date, ad_network, sum(ad_request) as ad_request, sum(ad_filled) as ad_filled, " +
                "sum(ad_impression) as ad_impression, sum(ad_click) as ad_click, sum(ad_revenue) as ad_revenue from app_daily_metrics_history " +
                "where date between '" + start + "' and '" + end + "' group by date, ad_network order by date desc";
        try {
            List<JSObject> list = DB.findListBySql(sql);
            for (int i = 0; i < list.size(); i++) {
                AppAdsMetrics metric = new AppAdsMetrics();
                metric.date = list.get(i).get("date");
                metric.adNetwork = list.get(i).get("ad_network");
                metric.adRequest = Utils.convertLong(list.get(i).get("ad_request"), 0);
                metric.adFilled = Utils.convertLong(list.get(i).get("ad_filled"), 0);
                metric.adImpression = Utils.convertLong(list.get(i).get("ad_impression"), 0);
                metric.adClick = Utils.convertLong(list.get(i).get("ad_click"), 0);
                metric.adRevenue = Utils.trimDouble(list.get(i).get("ad_revenue"));

                JsonObject one = new JsonObject();
                one.addProperty("ad_network", metric.adNetwork);
                one.addProperty("date", metric.date.getTime());
                one.addProperty("ad_request", metric.adRequest);
                one.addProperty("ad_filled", metric.adFilled);
                one.addProperty("ad_impression", metric.adImpression);
                one.addProperty("ad_click", metric.adClick);
                one.addProperty("ad_revenue", metric.adRevenue);
                arr.add(one);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        json.add("data", arr);
        if (arr.size() > 0) {
            json.addProperty("ret", 1);
        } else {
            json.addProperty("ret", 0);
        }
        response.getWriter().write(json.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    public static List<AppAdsMetrics> fetchAdsMetrics() {
        List<AppAdsMetrics> metrics = new ArrayList<>();
        String sql = "select ad_network, sum(ad_request) as ad_request, sum(ad_filled) as ad_filled, " +
                "sum(ad_impression) as ad_impression, sum(ad_click) as ad_click, sum(ad_revenue) as ad_revenue from app_ad_unit_metrics " +
                "group by ad_network";
        try {
            List<JSObject> list = DB.findListBySql(sql);
            for (int i = 0; i < list.size(); i++) {
                AppAdsMetrics metric = new AppAdsMetrics();
                metric.adNetwork = list.get(i).get("ad_network");
                metric.adRequest = Utils.convertLong(list.get(i).get("ad_request"), 0);
                metric.adFilled = Utils.convertLong(list.get(i).get("ad_filled"), 0);
                metric.adImpression = Utils.convertLong(list.get(i).get("ad_impression"), 0);
                metric.adClick = Utils.convertLong(list.get(i).get("ad_click"), 0);
                metric.adRevenue = Utils.trimDouble(list.get(i).get("ad_revenue"));
                metrics.add(metric);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return metrics;
    }
}
