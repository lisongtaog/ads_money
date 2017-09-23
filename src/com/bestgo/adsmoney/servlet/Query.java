package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.bean.AppAdUnitMetrics;
import com.bestgo.adsmoney.bean.MetricsResponse;
import com.bestgo.adsmoney.cache.CacheItem;
import com.bestgo.adsmoney.cache.GlobalCache;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "Query", urlPatterns = "/query")
public class Query extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String appId = request.getParameter("app_id");
        String countryCode = request.getParameter("country_code");

        Gson gson = new Gson();
        if (appId != null && countryCode != null) {
            CacheItem item = GlobalCache.findCacheItem(appId);
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/json");
            if (item != null) {
                List<AppAdUnitMetrics> daily = item.metrics;
                List<AppAdUnitMetrics> country = item.countryCacheMap.get(countryCode);
                response.getWriter().write(gson.toJson(new MetricsResponse(daily, country)));
            } else {
                response.getWriter().write(gson.toJson(new MetricsResponse()));
            }
        } else {
            response.getWriter().write(gson.toJson(new MetricsResponse()));
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
