package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.utils.DateUtil;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 查询购买用户每天的累计收入 接口
 */
@WebServlet(name = "InterQueryBuyUserDailyRevenue", urlPatterns = "/query_buy_user_daily_revenue")
public class InterQueryBuyUserDailyRevenue extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String installDate = request.getParameter("date");

        JsonArray jsonArray = new JsonArray();
        if ("iLoveMoney".equals(token)) {
            try {
                    for (int i =0;i<5;i++){
                        String currInstallDate = DateUtil.addDay(installDate,-i,"yyyy-MM-dd");
                        String secondDay = DateUtil.addDay(currInstallDate,1,"yyyy-MM-dd");
                        String thirdDay = DateUtil.addDay(currInstallDate,2,"yyyy-MM-dd");
                        String fourthDay = DateUtil.addDay(currInstallDate,3,"yyyy-MM-dd");
                        //计算在某个安装日期内（的某个应用在某个国家中）的每展示日期总收入
                        String sql = "SELECT event_date,app_id,country_code,sum(revenue) AS total_revenue FROM app_ads_buy_impressions_statistics " +
                                "WHERE installed_date = '" + currInstallDate + "' " +
                                "and event_date between '" + currInstallDate + "' and '" + installDate +"' " +
                                " GROUP BY event_date,app_id,country_code order by event_date";
                        List<JSObject> revenueList = DB.findListBySql(sql);

                        for (int j =0;j<revenueList.size();j++){
                            JSObject js = revenueList.get(j);
                            String eventDate = js.get("event_date").toString();
                            String appId = js.get("app_id");
                            String countryCode = js.get("country_code");
                            double totalRevenue = js.get("total_revenue");
                            JsonObject one = new JsonObject();
                            one.addProperty("intall_date",currInstallDate);
                            one.addProperty("app_id",appId);
                            one.addProperty("country_code",countryCode);
                            if (currInstallDate.equals(eventDate)) {
                                one.addProperty("first_day_revenue",totalRevenue);
                            }else if (secondDay.equals(eventDate)) {
                                one.addProperty("second_day_revenue",totalRevenue);
                            }else if (thirdDay.equals(eventDate)) {
                                one.addProperty("third_day_revenue",totalRevenue);
                            }else if (fourthDay.equals(eventDate)) {
                                one.addProperty("fourth_day_revenue",totalRevenue);
                            }
                            jsonArray.add(one);
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
