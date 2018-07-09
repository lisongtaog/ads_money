package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.adsmoney.bean.AppDailyActiveUser;
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
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(name = "DailyActiveUser", urlPatterns = {"/daily_active_user/*"})
public class DailyActiveUser extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/query") || path.equals("/get")) {
                ArrayList<AppDailyActiveUser> resultList = new ArrayList<>();

                String endDate = request.getParameter("end_date");
                int index = Utils.parseInt(request.getParameter("page_index"), 0);
                int size = Utils.parseInt(request.getParameter("page_size"), 20);
                String filter = request.getParameter("filter");

                if (filter == null || filter.isEmpty()) {
                    filter = "";
                }
                ArrayList<String> appIds = new ArrayList<>();
                String[] filters = filter.split(",");
                for (String appId : filters) {
                    if (appId.isEmpty()) continue;
                    appIds.add(appId);
                }

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date targetDate = sdf.parse(endDate);

                    JsonArray array = new JsonArray();

                    int loop = 0;
                    while (loop++ < 7) {
                        String currDate = sdf.format(targetDate);
                        String sql = "select first_open_date, total_user, active_count, avg_active_count " +
                                "from app_daily_active_user " +
                                "where date = '" + currDate + "' ";
                        targetDate.setTime(targetDate.getTime() - 86400 * 1000);
                        if (appIds.size() > 0) {
                            String ss = "";
                            for (int i = 0; i < appIds.size(); i++) {
                                if (i < appIds.size() - 1) {
                                    ss += "'" + appIds.get(i) + "',";
                                } else {
                                    ss += "'" + appIds.get(i) + "'";
                                }
                            }
                            sql += " and app_id in (" + ss + ")";
                        }
                        sql += " group by first_open_date order by first_open_date desc";

                        List<JSObject> list = DB.findListBySql(sql);
                        resultList.clear();

                        for (int i = 0; i < list.size(); i++) {
                            Date date = list.get(i).get("first_open_date");
                            long totalUser = Utils.convertLong(list.get(i).get("total_user"), 0);
                            long activeCount = Utils.convertLong(list.get(i).get("active_count"), 0);
                            double avgActiveCount = Utils.convertDouble(list.get(i).get("avg_active_count"), 0);
                            AppDailyActiveUser one = new AppDailyActiveUser();
                            one.firstOpenDate = date;
                            one.userCount = totalUser;
                            one.activeCount = activeCount;
                            one.avgActiveCount = avgActiveCount;
                            resultList.add(one);
                        }

                        JsonArray oneItem = new JsonArray();
                        array.add(oneItem);
                        if (path.equals("/get")) {
                            index = 0;
                            size = resultList.size();
                        }
                        for (int i = index * size; i < resultList.size() && i < (index * size + size); i++) {
                            JsonObject jsonObject = new JsonObject();
                            if (path.equals("/get")) {
                                jsonObject.addProperty("date", resultList.get(i).firstOpenDate.getTime());
                            } else {
                                jsonObject.addProperty("date", resultList.get(i).firstOpenDate.toString());
                            }
                            jsonObject.addProperty("total_user", resultList.get(i).userCount);
                            jsonObject.addProperty("active_count", resultList.get(i).activeCount);
                            jsonObject.addProperty("avg_active_count", Utils.trimDouble(resultList.get(i).avgActiveCount));
                            oneItem.add(jsonObject);
                        }
                    }

                    json.addProperty("ret", 1);
                    json.addProperty("message", "成功");
                    json.addProperty("total", resultList.size());
                    json.add("data", array);
                } catch (Exception ex) {
                    json.addProperty("ret", 0);
                    json.addProperty("message", ex.getMessage());
                }
            }
        }

        response.getWriter().write(json.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
