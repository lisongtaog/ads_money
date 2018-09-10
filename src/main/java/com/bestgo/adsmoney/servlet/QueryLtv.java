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
import java.util.List;

@WebServlet(name = "QueryLtv", urlPatterns = "/query_Ltv")
public class QueryLtv extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String token = request.getParameter("token");
        String date = request.getParameter("date");
//        String app_id = request.getParameter("app_id");

        if ("iLoveMoney".equals(token)) {
            JsonArray array = new JsonArray();

            String date6 = DateUtil.addDay(date, -6, "yyyy-MM-dd");
            String date9 = DateUtil.addDay(date, -9, "yyyy-MM-dd");

            String date17 = DateUtil.addDay(date, -16, "yyyy-MM-dd");
            String date14 = DateUtil.addDay(date, -13, "yyyy-MM-dd");

            try {
                String sql = "  select app_id,country_code,installed_date,seven_days_ltv,fourteen_days_ltv  \n" +
                        "\tfrom app_country_ltv_statistics  \n" +
                        "\twhere installed_date between '" + date17 + "' and '" + date14 + "' and method = 1 ";

                List<JSObject> list = DB.findListBySql(sql);
                JsonObject one;

                String appId = "";
                String countryCode = "";
                String installed_date = "";
                double seven_days_ltv = 0;
                double fourteen_days_ltv = 0;

                for (int i = 0; i < list.size(); i++) {
                    one = new JsonObject();
                    appId = list.get(i).get("app_id");
                    countryCode = list.get(i).get("country_code");
                    installed_date = list.get(i).get("installed_date").toString();
                    seven_days_ltv = Utils.convertDouble(list.get(i).get("seven_days_ltv"), 0);
                    fourteen_days_ltv = Utils.convertDouble(list.get(i).get("fourteen_days_ltv"), 0);

                    one.addProperty("appId", appId);
                    one.addProperty("countryCode", countryCode);
                    one.addProperty("installed_date", installed_date);
                    one.addProperty("seven_days_ltv", seven_days_ltv);
                    one.addProperty("fourteen_days_ltv", fourteen_days_ltv);
                    array.add(one);
                }

                sql = "  select app_id,country_code,installed_date,seven_days_ltv,fourteen_days_ltv  \n" +
                        "\tfrom app_country_ltv_statistics  \n" +
                        "\twhere installed_date between '" + date9 + "' and '" + date6 + "'  and method = 1 ";

                List<JSObject> list1 = DB.findListBySql(sql);

                for (int i = 0; i < list1.size(); i++) {
                    one = new JsonObject();
                    appId = list1.get(i).get("app_id");
                    countryCode = list1.get(i).get("country_code");
                    installed_date = list1.get(i).get("installed_date").toString();
                    seven_days_ltv = Utils.convertDouble(list1.get(i).get("seven_days_ltv"), 0);
                    fourteen_days_ltv = Utils.convertDouble(list1.get(i).get("fourteen_days_ltv"), 0);

                    one.addProperty("appId", appId);
                    one.addProperty("countryCode", countryCode);
                    one.addProperty("installed_date", installed_date);
                    one.addProperty("seven_days_ltv", seven_days_ltv);
                    one.addProperty("fourteen_days_ltv", fourteen_days_ltv);
                    array.add(one);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            response.getWriter().write(array.toString());
        }
    }
}
