package com.bestgo.adsmoney.servlet;


import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.adsmoney.bean.AppAdsDailyMetrics;
import com.bestgo.common.database.services.DB;
import com.bestgo.common.database.utils.JSObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jikai on 11/13/17.
 */
@WebServlet(name = "AppAdImport", urlPatterns = {"/app_ad_data_import"})
public class AppAdImport extends HttpServlet {//投放系统admanager_tools 请求此接口，上报
    private static ExecutorService executors = Executors.newFixedThreadPool(1);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String token = req.getParameter("token");
        String date = req.getParameter("date");
        if ("iLoveMoney".equals(token)) {
            resp.getWriter().write("ok");

            String body  = Utils.getRequestBody(req);
            executors.submit(new Runnable() {
                @Override
                public void run() {
                    updateToDB(body,date);
                }
            });
        }
    }

    private void updateToDB(String body,String date) {
        Type listType = new TypeToken<List<AppAdsDailyMetrics>>() {}.getType();
        List<AppAdsDailyMetrics> list = new Gson().fromJson(body, listType);
        for (int i = 0; i < list.size(); i++) {
            try {
                AppAdsDailyMetrics one = list.get(i);
                JSObject record = DB.simpleScan("app_ads_daily_metrics_history").select("date")
                        .where(DB.filter().whereEqualTo("date", one.date))
                        .and(DB.filter().whereEqualTo("app_id", one.appId))
                        .and(DB.filter().whereEqualTo("country_code", one.countryCode))
                        .and(DB.filter().whereEqualTo("ad_network", one.adNetwork))
                        .execute();
                if (record.hasObjectData()) {
                    DB.update("app_ads_daily_metrics_history")
                            .put("impressions", one.impressions)
                            .put("spend", one.spend)
                            .put("installed", one.installed)
                            .put("click", one.click)
                            .where(DB.filter().whereEqualTo("date", one.date))
                            .and(DB.filter().whereEqualTo("app_id", one.appId))
                            .and(DB.filter().whereEqualTo("country_code", one.countryCode))
                            .and(DB.filter().whereEqualTo("ad_network", one.adNetwork))
                            .execute();
                } else {
                    DB.insert("app_ads_daily_metrics_history")
                            .put("impressions", one.impressions)
                            .put("spend", one.spend)
                            .put("installed", one.installed)
                            .put("click", one.click)
                            .put("date", one.date)
                            .put("app_id", one.appId)
                            .put("country_code", one.countryCode)
                            .put("ad_network", one.adNetwork)
                            .execute();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
            DB.updateBySql("UPDATE app_ads_daily_metrics_history a,\n" +
                    "(SELECT h.app_id,h.country_code,h.ad_network,h.sum_spend FROM app_ads_daily_metrics_history h,\n" +
                    "(SELECT app_id,country_code,ad_network,MAX(date) AS before_date FROM app_ads_daily_metrics_history WHERE date < '"+date+"') m \n" +
                    "WHERE h.date = m.before_date GROUP BY h.app_id,h.country_code,h.ad_network) b\n" +
                    "SET a.sum_spend = a.spend + b.sum_spend WHERE a.date = '"+date+"' AND a.app_id = b.app_id AND a.country_code = b.country_code AND a.ad_network = b.ad_network");
            DB.updateBySql("UPDATE app_ads_daily_metrics_history SET sum_spend = spend WHERE date = '"+date+"' and sum_spend = 0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("updateToDB finished");
    }
}
