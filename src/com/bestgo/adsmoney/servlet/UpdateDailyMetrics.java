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
public class UpdateDailyMetrics extends HttpServlet {//供money_tools调用使用；更新日表：应用、国家维度的请求、填充、展示、点击、收益数据，舞广告单元维度
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String date = request.getParameter("date");
        if ("iLoveMoney".equals(token)) {
            try {
                DB.delete("app_daily_metrics_history").where(DB.filter().whereEqualTo("date", date)).execute();
                String sql = "insert into app_daily_metrics_history select date, app_id, ad_network, country_code, sum(ad_request) as ad_request, sum(ad_filled) as ad_filled, " +
                        "sum(ad_impression) as ad_impression, sum(ad_click) as ad_click, sum(ad_revenue) as ad_revenue,"+
                        "(SELECT SUM(IFNULL(s.ad_revenue,0)) FROM app_ad_unit_metrics_history s WHERE s.date = d.date "+
                        "   AND s.app_id = d.app_id AND s.ad_network = d.ad_network AND s.country_code = d.country_code "+
                        "   AND s.ad_unit_id IN (SELECT ad_unit_id from app_ad_unit_config WHERE flag = '1' ) "+
                        ")AS ad_new_revenue "+
                        " from app_ad_unit_metrics_history d " +
                        " where date = '" + date + "' group by date, app_id, ad_network, country_code";
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
