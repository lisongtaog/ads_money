package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.cache.GlobalCache;
import com.bestgo.common.database.services.DB;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "UpdateDailyMetric", urlPatterns = "/update_daily_metric")
public class UpdateDailyMetrics extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String date = request.getParameter("date");
        if ("iLoveMoney".equals(token)) {
            try {
                DB.delete("app_daily_metrics_history").where(DB.filter().whereEqualTo("date", date)).execute();
                String sql = "insert into app_daily_metrics_history select date, app_id, ad_network, country_code, sum(ad_request) as ad_request, sum(ad_filled) as ad_filled, " +
                        "sum(ad_impression) as ad_impression, sum(ad_click) as ad_click, sum(ad_revenue) as ad_revenue from app_ad_unit_metrics_history " +
                        "where date = '" + date + "' group by date, app_id, ad_network, country_code";
                DB.updateBySql(sql);
                response.getWriter().write("ok");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
