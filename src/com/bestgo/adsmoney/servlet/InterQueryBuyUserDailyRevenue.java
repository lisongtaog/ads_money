package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.utils.DateUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询购买用户每天的累计收入 接口
 */
@WebServlet(name = "InterQueryBuyUserDailyRevenue", urlPatterns = "/query_buy_user_daily_revenue")
public class InterQueryBuyUserDailyRevenue extends HttpServlet {
    class RevenueItem {
        public double first_day_revenue;
        public double second_day_revenue;
        public double third_day_revenue;
        public double fourth_day_revenue;
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String installDate = request.getParameter("date");

        JsonArray jsonArray = new JsonArray();
        if ("iLoveMoney".equals(token)) {
            try {
                //key=installDate:appId:countryCode,value=RevenueItem
                HashMap<String, RevenueItem> map = new HashMap<>();
                RevenueItem revenueItem = null;
                String key = null;
                for (int i =0;i<5;i++){
                    String currInstallDate = DateUtil.addDay(installDate,-i,"yyyy-MM-dd");
                    String secondDay = DateUtil.addDay(currInstallDate,1,"yyyy-MM-dd");
                    String thirdDay = DateUtil.addDay(currInstallDate,2,"yyyy-MM-dd");
                    String fourthDay = DateUtil.addDay(currInstallDate,3,"yyyy-MM-dd");
                    System.out.println(currInstallDate + "," + secondDay + "," + thirdDay + "," + fourthDay);
                    //计算在某个安装日期内（的某个应用在某个国家中）的每展示日期总收入
                    String sql = "SELECT event_date,app_id,country_code,sum(revenue) AS total_revenue FROM app_ads_buy_impressions_statistics " +
                            "WHERE installed_date = '" + currInstallDate + "' " +
                            "and event_date >= '" + currInstallDate + "' and event_date <= '" + installDate + "' " +
                            "GROUP BY event_date,app_id,country_code order by event_date";
                    List<JSObject> revenueList = DB.findListBySql(sql);

                    for (int j =0;j<revenueList.size();j++){
                        JSObject js = revenueList.get(j);
                        String eventDate = js.get("event_date").toString();
                        String appId = js.get("app_id");
                        String countryCode = js.get("country_code");
                        double totalRevenue = js.get("total_revenue");
                        key = currInstallDate + ":" + appId + ":" + countryCode;
                        revenueItem = map.get(key);
                        if (revenueItem == null) {
                            revenueItem = new RevenueItem();
                        }
                        if (currInstallDate.equals(eventDate)) {
                            revenueItem.first_day_revenue = totalRevenue;
                        }else if (secondDay.equals(eventDate)) {
                            revenueItem.second_day_revenue = totalRevenue;
                        }else if (thirdDay.equals(eventDate)) {
                            revenueItem.third_day_revenue = totalRevenue;
                        }else if (fourthDay.equals(eventDate)) {
                            revenueItem.fourth_day_revenue = totalRevenue;
                        }
                        map.put(key,revenueItem);
                    }
                }
                if (map.size() > 0) {
                    for (Map.Entry<String,RevenueItem> entry : map.entrySet()) {
                        JsonObject js = new JsonObject();
                        String currKey = entry.getKey();
                        String[] split = currKey.split(":");
                        RevenueItem item = entry.getValue();
                        js.addProperty("install_date",split[0]);
                        js.addProperty("app_id",split[1]);
                        js.addProperty("country_code",split[2]);
                        js.addProperty("first_day_revenue",item.first_day_revenue);
                        js.addProperty("second_day_revenue",item.second_day_revenue);
                        js.addProperty("third_day_revenue",item.third_day_revenue);
                        js.addProperty("fourth_day_revenue",item.fourth_day_revenue);
                        jsonArray.add(js);
                    }
                }
                response.getWriter().write(jsonArray.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

}
