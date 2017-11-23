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

@WebServlet(name = "QueryDailyMetrics", urlPatterns = "/query_daily_metric")
public class QueryDailyMetrics extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String date = request.getParameter("date");
        if ("iLoveMoney".equals(token)) {
            try {
                String sql = "select date,app_id,country_code,sum(ad_revenue) as revenue " +
                        "from app_daily_metrics_history where date=? " +
                        "group by date, app_id, country_code";

                List<JSObject> list = DB.findListBySql(sql, date);
                JsonArray array = new JsonArray();
                for (int i = 0; i < list.size(); i++) {
                    JsonObject one = new JsonObject();
                    one.addProperty("app_id", list.get(i).get("app_id").toString());
                    one.addProperty("country_code", list.get(i).get("country_code").toString());
                    one.addProperty("revenue", Utils.convertDouble(list.get(i).get("revenue"), 0));
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
