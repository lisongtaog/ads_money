package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.utils.Utils;
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
import java.util.*;

/**
 * @author mengjun
 * @date 2018/8/6 11:05
 * @desc 活跃用户、展示收益数据同步接口
 */
@WebServlet(name = "InterActiveUserAndImpressionSta", urlPatterns = {"/interStaSync/*"})
public class InterActiveUserAndImpressionSta extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            doRequest(request,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    protected void doRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String path = request.getPathInfo();
        JsonArray jsonArray = null;
        if (path == null) {
            response.getWriter().write(jsonArray.toString());
            return;
        }
        String token = request.getParameter("token");
        //常规业务逻辑
        if ("iLoveMoney".equals(token)) {
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String appId = request.getParameter("appId");
            if (path.startsWith("/activeUser")) {//活跃用户
                jsonArray = queryActiveUser(startDate,endDate,appId);
            }else if(path.startsWith("/revenue")){//展示次数及收益
                jsonArray = queryRevenue(startDate,endDate);
            }
        }
        response.getWriter().write(jsonArray.toString());
    }


    /**
     * 查询已经拉取的系列活跃用户
     * @param startDate 统计日期开始时间
     * @param endDate 统计日期结束时间
     * @param appId
     * @return
     */
    private JsonArray queryActiveUser(String startDate,String endDate,String appId) {
        JsonArray jArray = new JsonArray();
        try {
            List<JSObject> jsList = fetchCampaignActiveUserByEventDate(startDate, endDate,appId);
            for (int i = 0,len = jsList.size();i < len;i++) {
                JSObject js = jsList.get(i);
                if (js.hasObjectData()) {
                    JsonObject json = new JsonObject();
                    json.addProperty("installed_date",js.get("installed_date").toString());
                    json.addProperty("event_date",js.get("event_date").toString());
                    json.addProperty("app_id",appId);
                    json.addProperty("campaign_name",js.get("campaign_name").toString());
                    json.addProperty("country_code",js.get("country_code").toString());
                    json.addProperty("active_num", Utils.convertDouble(js.get("active_num"), 0));
                    jArray.add(json);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jArray;
    }

    /**
     * 拉取同步展示 收益
     * @param startDate 统计日期开始时间
     * @param endDate 统计日期结束时间
     * @return
     * @return
     */
    private JsonArray queryRevenue(String startDate,String endDate){
        JsonArray jArray = new JsonArray();
        try {
            List<JSObject> jsList = fetchCampaignRevenueByEventDate(startDate,endDate);
            for (int i = 0,len = jsList.size();i < len;i++) {
                JSObject js = jsList.get(i);
                if (js.hasObjectData()) {
                    JsonObject json = new JsonObject();
                    json.addProperty("installed_date",js.get("installed_date").toString());
                    json.addProperty("event_date",js.get("event_date").toString());
                    json.addProperty("app_id",js.get("app_id").toString());
                    json.addProperty("campaign_name",js.get("campaign_name").toString());
                    json.addProperty("country_code",js.get("country_code").toString());
                    json.addProperty("ad_unit_id",js.get("ad_unit_id").toString());
                    json.addProperty("impressions", Utils.convertDouble(js.get("impressions"), 0));
                    json.addProperty("ecpm", Utils.convertDouble(js.get("ecpm"), 0));
                    json.addProperty("revenue", Utils.convertDouble(js.get("revenue"), 0));
                    jArray.add(json);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jArray;
    }


    /**
     * 根据事件日期日期区间获取系列活跃用户数
     * @param startDate
     * @param endDate
     * @param appId
     * @return
     * @throws Exception
     */
    private List<JSObject> fetchCampaignActiveUserByEventDate(String startDate,String endDate,String appId) throws Exception {
        String sql = "SELECT installed_date,event_date,app_id,campaign_name,country_code,active_num \n" +
                "FROM app_campaign_active_user_statistics\n" +
                "WHERE event_date BETWEEN '" + startDate + "' AND '" + endDate + "' AND app_id = '"+appId+"'";
        List<JSObject> list = DB.findListBySql(sql);
        return list;
    }

    /**
     * 根据事件日期区间获取系列收入
     * @param startDate
     * @param endDate
     * @return
     * @throws Exception
     */
    private List<JSObject> fetchCampaignRevenueByEventDate(String startDate,String endDate) throws Exception {
        String sql = "SELECT installed_date,event_date,app_id,country_code,ad_unit_id,campaign_name,impressions,ecpm,revenue \n" +
                "FROM app_campaign_impressions_statistics \n" +
                "WHERE event_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
        List<JSObject> list = DB.findListBySql(sql);
        return list;
    }

}
