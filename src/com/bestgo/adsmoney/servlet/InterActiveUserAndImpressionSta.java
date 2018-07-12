package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.bean.AppData;
import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.common.database.services.DB;
import com.bestgo.common.database.utils.JSObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.deploy.config.DefaultConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Desc: 活跃用户、展示收益数据同步接口
 * Auth: maliang
 * Date: 2018/7/12 17:40
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
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonArray jsonArray = null;
        if(path == null){
            response.getWriter().write(jsonArray.toString());
            return;
        }

        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        //常规业务逻辑
        if (path.startsWith("/activeUser")) {//活跃用户
            jsonArray = queryActiveUser(startDate,endDate);
        }else if(path.startsWith("/revenue")){//展示次数及收益
            jsonArray = queryRevenue(startDate,endDate);
        }
        response.getWriter().write(jsonArray.toString());
    }


    /**
     * 拉取活跃用户
     * @param startDate 统计日期开始时间
     * @param endDate 统计日期结束时间
     * @return
     */
    private JsonArray queryActiveUser(String startDate,String endDate){
        JsonArray jArray = new JsonArray();
        //TODO 获取活跃用户数据，并转换为JsonArray

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
        //TODO 获取活跃用户数据，并转换为JsonArray

        return jArray;
    }
}
