package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.cache.GlobalCache;
import com.bestgo.adsmoney.utils.DateUtil;
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
                String sql = "insert into app_daily_metrics_history(date,app_id,ad_network,country_code,ad_request,ad_filled,ad_impression,ad_click,ad_revenue,ad_new_revenue) select date, app_id, ad_network, country_code, sum(ad_request) as ad_request, sum(ad_filled) as ad_filled, " +
                        "sum(ad_impression) as ad_impression, sum(ad_click) as ad_click, sum(ad_revenue) as ad_revenue,"+
                        "(SELECT SUM(IFNULL(s.ad_revenue,0)) FROM app_ad_unit_metrics_history s WHERE s.date = d.date "+
                        "   AND s.app_id = d.app_id AND s.ad_network = d.ad_network AND s.country_code = d.country_code "+
                        "   AND s.ad_unit_id IN (SELECT ad_unit_id from app_ad_unit_config WHERE flag = '1' ) "+
                        ")AS ad_new_revenue "+
                        " from app_ad_unit_metrics_history d " +
                        " where date = '" + date + "' group by date, app_id, ad_network, country_code";
                DB.updateBySql(sql);
                sql = "UPDATE app_daily_metrics_history a,\n" +
                        "(SELECT h.app_id,h.country_code,h.ad_network,h.sum_ad_revenue FROM app_daily_metrics_history h,\n" +
                        "(SELECT app_id,country_code,ad_network,MAX(date) AS before_date FROM app_daily_metrics_history WHERE date < '"+date+"') m \n" +
                        "WHERE h.date = m.before_date GROUP BY h.app_id,h.country_code,h.ad_network) b\n" +
                        "SET a.sum_ad_revenue = a.ad_revenue + b.sum_ad_revenue WHERE a.date = '"+date+"' AND a.app_id = b.app_id AND a.country_code = b.country_code AND a.ad_network = b.ad_network";
                DB.updateBySql(sql);
                DB.updateBySql("UPDATE app_daily_metrics_history SET sum_ad_revenue = ad_revenue WHERE date = '"+date+"' AND sum_ad_revenue = 0");
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
