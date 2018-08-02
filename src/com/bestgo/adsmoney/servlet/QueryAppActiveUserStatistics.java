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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            //计算在某个安装日期内（的某个应用在某个国家中）的每个展示日期 展示次数
            String impressionsSql = "SELECT event_date,sum(impressions) AS impressions FROM app_ads_impressions_statistics " +
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

            List<JSObject> impressionsList = DB.findListBySql(impressionsSql);//展示次数

            JSObject obj = DB.findOneBySql(purchaseInstallSql);
            JSObject obj2 = DB.findOneBySql(allInstallSql);
            double purchaseInstalled=0,allInstalled = 0;
            try {

                if(obj.get("purchase_installed") != null && !"".equals(obj.get("purchase_installed"))){
                    //purchaseInstalled = Utils.parseInt(obj.get("purchase_installed"),0);
                    purchaseInstalled = new BigDecimal(obj.get("purchase_installed").toString()).doubleValue();
                }
                if(obj2.get("all_installed") != null && !"".equals(obj2.get("all_installed"))){
                    //allInstalled = Utils.parseInt(obj.get("all_installed"),0);
                    allInstalled = new BigDecimal(obj2.get("all_installed").toString()).doubleValue();
                }
            }catch (Exception e){
                e.printStackTrace();
            }


            String activeUserFirstEventDate = date;
            if(revenueList.size() > 0 && revenueList.get(0).hasObjectData()){
                activeUserFirstEventDate = revenueList.get(0).get("event_date").toString();//去活跃用户第一天的展示日期
            }
            double firstImpressions = 0;//首日展示次数
            double preImpression = 0;
            String eventDate = null;double impressions = 0D;
            Map<String,List> impressionMap = new HashMap<String,List>();
            List impressionList = null;
            for (int i = 0,len = impressionsList.size();i < len;i++) {
                JSObject impressionsJS = impressionsList.get(i);
                if (impressionsJS.hasObjectData()) {
                    impressionList = new ArrayList();
                    eventDate = impressionsJS.get("event_date").toString();//event_date
                    impressions = new BigDecimal(impressionsJS.get("impressions").toString()).doubleValue();//展示次数
                    if(activeUserFirstEventDate.equals(eventDate)){
                        firstImpressions = impressions;
                        preImpression = impressions;
                    }
                    impressionList.add(impressions);//展示次数
                    impressionList.add(Utils.trimDouble(firstImpressions >0 ? 100*(impressions / firstImpressions): 0));//首日展示占比
                    impressionList.add(impressions - preImpression);//递进值

                    impressionMap.put(eventDate,impressionList);
                    preImpression = impressions;
                }
            }



            double installActive = 0;
            double preActive = 0;

            JsonArray array1 = new JsonArray();
            JsonArray array2 = new JsonArray();
            JsonArray dataArray = new JsonArray();
            JsonArray item = null;
            for (int i = 0,len = revenueList.size();i < len;i++) {
                JSObject revenueJS = revenueList.get(i);
                item = new JsonArray();
                if (revenueJS.hasObjectData()) {
                    eventDate = revenueJS.get("event_date").toString();
                    array1.add(eventDate);
                    double totalActiveNum = Utils.convertDouble(revenueJS.get("total_acitve_num"),0);
                    double activeNum = NumberUtil.trimDouble(totalActiveNum,0);
                    if(i == 0){
                        installActive = activeNum;
                        preActive = activeNum;
                    }
                    array2.add(activeNum);
                    item.add(date);
                    item.add(allInstalled);//总安装量
                    item.add(purchaseInstalled);//购买安装量
                    item.add(Utils.trimDouble(allInstalled >0 ? 100*(purchaseInstalled / allInstalled): 0));//购买安装占比
                    item.add(eventDate);
                    item.add(activeNum);
                    item.add(Utils.trimDouble(allInstalled >0 ? 100*(activeNum / allInstalled): 0));//活跃占比
                    item.add(Utils.trimDouble(installActive >0 ? 100*(activeNum / installActive): 0));//首日占比
                    item.add(activeNum - preActive);//递进值
                    impressionList = impressionMap.get(eventDate);//展示次数
                    if (null != impressionList){
                        item.add(impressionList.get(0).toString());//广告展示次数
                        item.add(impressionList.get(1).toString());//首日展示占比%
                        item.add(impressionList.get(2).toString());//展示递进值
                    }else {
                        item.add("-");//广告展示次数
                        item.add("-");//首日展示占比%
                        item.add("-");//展示递进值
                    }

                    dataArray.add(item);
                    preActive = activeNum;
                }
            }
            json.add("date_array",array1);
            json.add("data_array",array2);
            json.add("data_table",dataArray);
            json.addProperty("ret", 1);

        } catch (Exception ex) {
            ex.printStackTrace();
            json.addProperty("ret", 0);
            json.addProperty("message", ex.getMessage());
        }
        response.getWriter().write(json.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }
}
