package com.bestgo.adsmoney.servlet;

import com.bestgo.common.database.MySqlHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 对外提供的接口，用于收集、上报数据
 */
@WebServlet(name = "InterDataCollection", urlPatterns = {"/dataCollection/*"})
public class InterDataCollection extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response);
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response);
    }

    protected void doRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/upload_adunit_tagecpm")) {//target ecpm
                json = doHandleTargetEcpm(request,response);
            } else if (path.equals("/get")) {

            }
        }

        response.getWriter().write(json.toString());
    }

    /**
     * 接收上报target ecpm
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected JsonObject doHandleTargetEcpm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject json = new JsonObject();

        String dataStr = request.getParameter("data");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<AdUnitData>>(){}.getType();

        try {
            List<AdUnitData> adUnitDataList = gson.fromJson(dataStr, type);
            List<List<Object>> dataList = new ArrayList<List<Object>>();
            List<Object> data = null;
            String sql = "insert into app_ad_unit_target (app_id,ad_unit_id,country_code,tag_ecpm,uploadtime,updatetime) values(?,?,?,?,?,?)";
            Date current = new Date();
            for(AdUnitData item : adUnitDataList){
                data = new ArrayList<Object>();
                data.add(item.app_id);
                data.add(item.ad_unit_id);
                data.add(item.country_code);
                data.add(item.tag_ecpm);
                data.add(item.upload_time);
                data.add(current);

                dataList.add(data);
            }

            if(dataList.size() > 0){
                MySqlHelper helper = new MySqlHelper();
                helper.excuteBatch2DB(sql,dataList);
            }
            json.addProperty("ret","1");
            json.addProperty("msg","handle successful");

        }catch (Exception e){
            e.printStackTrace();
            json.addProperty("ret","0");
            json.addProperty("msg","hadle failed\n" + e.getMessage());
        }
        return json;
    }


    /**
     * 广告单元 target ecpm上报数据格式
     */
    class AdUnitData{
        String app_id;
        String country_code;
        String ad_unit_id;
        Double tag_ecpm;
        String upload_time;
    }

}
