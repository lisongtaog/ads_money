package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.utils.NumberUtil;
import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.common.database.services.DB;
import com.bestgo.common.database.utils.JSObject;
import com.google.api.client.json.Json;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author mengjun
 * @date 2018/7/5 14:25
 * @description 应用活跃用户统计
 */
@WebServlet(name = "QueryAppActiveUserStatistics", urlPatterns = {"/query_app_active_user_statistics"})
public class QueryAppActiveUserStatistics extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;
        JsonObject json = new JsonObject();
        String date = request.getParameter("date");
        String appId = request.getParameter("appId");
        String countryCode = request.getParameter("countryCode");

//        date = "2018-06-20";
//        appId = "com.androapplite.vpn10";
//        countryCode = "TM";

        try {
            String sql = "SELECT event_date,sum(active_num) AS total_acitve_num FROM app_active_user_statistics " +
                    "WHERE installed_date = '" + date + "' " +
                    ("all".equals(appId) || appId.isEmpty() ? " " : "AND app_id = '" + appId + "' ") +
                    ("all".equals(countryCode) || countryCode.isEmpty() ? " " : "AND country_code = '" + countryCode + "' ") +
                    "AND event_date >= '" + date + "' " +
                    "GROUP BY event_date ORDER BY event_date";
            String purchaseInstallSql = "SELECT SUM(installed) as purchase_installed FROM app_ads_daily_metrics_history ";//购买安装量
            String allInstallSql = "SELECT SUM(installed) AS all_installed FROM app_firebase_daily_metrics_history ";//总安装量
            purchaseInstallSql += " where date ='"+date+"' ";
            allInstallSql += " where date ='"+date+"' ";
            if(!"all".equals(appId) && !appId.isEmpty()){
                purchaseInstallSql += " and app_id ='"+appId+"' ";
                allInstallSql += " and app_id ='"+appId+"' ";
            }
            if(!"all".equals(countryCode) && !countryCode.isEmpty()){
                purchaseInstallSql += " and country_code ='"+countryCode+"' ";
                allInstallSql += " and country_code ='"+countryCode+"' ";
            }
            List<JSObject> revenueList = DB.findListBySql(sql);
            JSObject obj = DB.findOneBySql(purchaseInstallSql);
            JSObject obj2 = DB.findOneBySql(allInstallSql);
            int purchaseInstalled=0,allInstalled = 0;
            if(obj.get("purchase_installed") != null){
                purchaseInstalled = Utils.parseInt(obj.get("purchase_installed"),0);
            }
            if(obj.get("all_installed") != null){
                allInstalled = Utils.parseInt(obj.get("all_installed"),0);
            }

            JsonArray array1 = new JsonArray();
            JsonArray array2 = new JsonArray();
            JsonArray dataArray = new JsonArray();
            JsonArray item = null;
            for (int i = 0,len = revenueList.size();i < len;i++) {
                JSObject revenueJS = revenueList.get(i);
                item = new JsonArray();
                if (revenueJS.hasObjectData()) {
                    String eventDate = revenueJS.get("event_date").toString();
                    array1.add(eventDate);
                    double totalActiveNum = Utils.convertDouble(revenueJS.get("total_acitve_num"),0);
                    double activeNum = NumberUtil.trimDouble(totalActiveNum,0);
                    array2.add(activeNum);
                    item.add(date);
                    item.add(allInstalled);
                    item.add(purchaseInstalled);
                    item.add(eventDate);
                    item.add(activeNum);
                    item.add(allInstalled >0 ? activeNum / allInstalled : 0);
                    dataArray.add(item);
                }
            }
            json.add("date_array",array1);
            json.add("data_array",array2);
            json.add("data_table",dataArray);
            json.addProperty("ret", 1);

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
