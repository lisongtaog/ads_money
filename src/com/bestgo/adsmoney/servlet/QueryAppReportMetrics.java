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
import java.util.List;

@WebServlet(name = "QueryAppReportMetrics", urlPatterns = "/query_app_report_metric")
public class QueryAppReportMetrics extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String date = request.getParameter("date");
        String appId = request.getParameter("app_id");
        if ("iLoveMoney".equals(token)) {
            try {
                String sql = "select date,app_id,ad_unit_id,country_code,ad_request,ad_filled,ad_impression,ad_click,ad_revenue" +
                        " from app_ad_unit_metrics_history where date=? and app_id=?";

                List<JSObject> list = DB.findListBySql(sql, date, appId);
                JsonArray array = new JsonArray();
                for (int i = 0; i < list.size(); i++) {
                    JsonObject one = new JsonObject();
                    one.addProperty("ad_unit_id", list.get(i).get("ad_unit_id").toString());
                    one.addProperty("country_code", list.get(i).get("country_code").toString());
                    one.addProperty("request", Utils.convertDouble(list.get(i).get("ad_request"), 0));
                    one.addProperty("filled", Utils.convertDouble(list.get(i).get("ad_filled"), 0));
                    one.addProperty("impression", Utils.convertDouble(list.get(i).get("ad_impression"), 0));
                    one.addProperty("click", Utils.convertDouble(list.get(i).get("ad_click"), 0));
                    one.addProperty("revenue", Utils.convertDouble(list.get(i).get("ad_click"), 0));
                    array.add(one);
                }
                response.getWriter().write(array.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
