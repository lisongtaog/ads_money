package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.Utils;
import com.bestgo.adsmoney.bean.AppAdImpressionMetrics;
import com.bestgo.adsmoney.bean.AppData;
import com.bestgo.adsmoney.bean.CountryReportMetrics;
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

@WebServlet(name = "AdImpressionMonitor", urlPatterns = {"/ad_impression_monitor/*"})
public class AdImpressionMonitor extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        List<AppData> appData = AppManagement.fetchAllAppData();

        if (path != null) {
            if (path.startsWith("/query")) {
                ArrayList<AppAdImpressionMetrics> resultList = new ArrayList<>();

                String startDate = request.getParameter("start_date");
                String endDate = request.getParameter("end_date");
                int index = Utils.parseInt(request.getParameter("page_index"), 0);
                int size = Utils.parseInt(request.getParameter("page_size"), 20);
                int order = Utils.parseInt(request.getParameter("order"), 0);
                boolean desc = order < 1000;
                if (order > 1000) order = order - 1000;
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
                    String sql = "select date, app_id, sum(total_user) as total_user, sum(active_user) as active_user, " +
                            "sum(ad_user) as ad_user, sum(full_ad_user) as full_ad_user, " +
                            "sum(one_full_ad_user) as one_full_ad_user, sum(two_full_ad_user) as two_full_ad_user, " +
                            "sum(three_full_ad_user) as three_full_ad_user, sum(four_full_ad_user) as four_full_ad_user, " +
                            "sum(five_full_ad_user) as five_full_ad_user, sum(native_ad_user) as native_ad_user, " +
                            "sum(one_native_ad_user) as one_native_ad_user, sum(two_native_ad_user) as two_native_ad_user, " +
                            "sum(three_native_ad_user) as three_native_ad_user, sum(four_native_ad_user) as four_native_ad_user, " +
                            "sum(five_native_ad_user) as five_native_ad_user, sum(full_ad_count) as full_ad_count, sum(native_ad_count) as native_ad_count " +
                            "from app_ads_impressions_history " +
                            "where date between '" + startDate + "' and '" + endDate + "' ";
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
                    sql += " group by date, app_id";

                    List<JSObject> list = DB.findListBySql(sql);

                    for (int i = 0; i < list.size(); i++) {
                        JSObject one = list.get(i);
                        AppAdImpressionMetrics item = new AppAdImpressionMetrics();

                        String appId = one.get("app_id");
                        item.date = one.get("date");
                        item.totalUser = Utils.convertLong(one.get("total_user"), 0);
                        item.activeUser = Utils.convertLong(one.get("active_user"), 0);
                        item.adUser = Utils.convertLong(one.get("ad_user"), 0);
                        item.fullAdUser = Utils.convertLong(one.get("full_ad_user"), 0);
                        item.oneFullAdUser = Utils.convertLong(one.get("one_full_ad_user"), 0);
                        item.twoFullAdUser = Utils.convertLong(one.get("two_full_ad_user"), 0);
                        item.threeFullAdUser = Utils.convertLong(one.get("three_full_ad_user"), 0);
                        item.fourFullAdUser = Utils.convertLong(one.get("four_full_ad_user"), 0);
                        item.fiveFullAdUser = Utils.convertLong(one.get("five_full_ad_user"), 0);
                        item.nativeAdUser = Utils.convertLong(one.get("native_ad_user"), 0);
                        item.oneNativeAdUser = Utils.convertLong(one.get("one_native_ad_user"), 0);
                        item.twoNativeAdUser = Utils.convertLong(one.get("two_native_ad_user"), 0);
                        item.threeNativeAdUser = Utils.convertLong(one.get("three_native_ad_user"), 0);
                        item.fourNativeAdUser = Utils.convertLong(one.get("four_native_ad_user"), 0);
                        item.fiveNativeAdUser = Utils.convertLong(one.get("five_native_ad_user"), 0);
                        item.fullAdCount = Utils.convertLong(one.get("full_ad_count"), 0);
                        item.nativeAdCount = Utils.convertLong(one.get("native_ad_count"), 0);
                        for (AppData app : appData) {
                            if (app.appId.equals(appId)) {
                                item.appName = app.appName;
                                break;
                            }
                        }
                        if (item.appName != null) {
                            resultList.add(item);
                        }
                    }

                    int orderIndex = order;
                    resultList.sort(new Comparator<AppAdImpressionMetrics>() {
                        @Override
                        public int compare(AppAdImpressionMetrics o1, AppAdImpressionMetrics o2) {
                            double ret = 0;
                            switch (orderIndex) {
                                case 0:
                                    ret = o1.date.compareTo(o2.date);
                                    break;
                                case 1:
                                    ret = o1.appName.compareTo(o2.appName);
                                    break;
                                case 2:
                                    ret = o1.totalUser - o2.totalUser;
                                    break;
                                case 3:
                                    ret = o1.activeUser - o2.activeUser;
                                    break;
                                case 4:
                                    ret = (o1.totalUser > 0 ? o1.activeUser * 1.0 / o1.totalUser : 0) - (o2.totalUser > 0 ? o2.activeUser * 1.0 / o2.totalUser : 0);
                                    break;
                                case 5:
                                    ret = (o1.activeUser > 0 ? (o1.activeUser - o1.adUser) * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? (o2.activeUser - o2.adUser) * 1.0 / o2.activeUser : 0);
                                    break;
                                case 6:
                                    ret = (o1.activeUser > 0 ? o1.fullAdUser * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? o2.fullAdUser * 1.0 / o2.activeUser : 0);
                                    break;
                                case 7:
                                    ret = o1.fullAdCount - o2.fullAdCount;
                                    break;
                                case 8:
                                    ret = (o1.activeUser > 0 ? o1.oneFullAdUser * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? o2.oneFullAdUser * 1.0 / o2.activeUser : 0);
                                    break;
                                case 9:
                                    ret = (o1.activeUser > 0 ? o1.twoFullAdUser * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? o2.twoFullAdUser * 1.0 / o2.activeUser : 0);
                                    break;
                                case 10:
                                    ret = (o1.activeUser > 0 ? o1.threeFullAdUser * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? o2.threeFullAdUser * 1.0 / o2.activeUser : 0);
                                    break;
                                case 11:
                                    ret = (o1.activeUser > 0 ? o1.fourFullAdUser * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? o2.fourFullAdUser * 1.0 / o2.activeUser : 0);
                                    break;
                                case 12:
                                    ret = (o1.activeUser > 0 ? o1.fiveFullAdUser * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? o2.fiveFullAdUser * 1.0 / o2.activeUser : 0);
                                    break;
                                case 13:
                                    ret = (o1.activeUser > 0 ? o1.nativeAdUser * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? o2.nativeAdUser * 1.0 / o2.activeUser : 0);
                                    break;
                                case 14:
                                    ret = o1.nativeAdUser - o2.nativeAdUser;
                                    break;
                                case 15:
                                    ret = (o1.activeUser > 0 ? o1.oneNativeAdUser * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? o2.oneNativeAdUser * 1.0 / o2.activeUser : 0);
                                    break;
                                case 16:
                                    ret = (o1.activeUser > 0 ? o1.twoNativeAdUser * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? o2.twoNativeAdUser * 1.0 / o2.activeUser : 0);
                                    break;
                                case 17:
                                    ret = (o1.activeUser > 0 ? o1.threeNativeAdUser * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? o2.threeNativeAdUser * 1.0 / o2.activeUser : 0);
                                    break;
                                case 18:
                                    ret = (o1.activeUser > 0 ? o1.fourNativeAdUser * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? o2.fourNativeAdUser * 1.0 / o2.activeUser : 0);
                                    break;
                                case 19:
                                    ret = (o1.activeUser > 0 ? o1.fiveNativeAdUser * 1.0 / o1.activeUser : 0) - (o2.activeUser > 0 ? o2.fiveNativeAdUser * 1.0 / o2.activeUser : 0);
                                    break;
                            }
                            if (ret > 0) {
                                return desc ? -1 : 1;
                            } else if (ret == 0) {
                                return 0;
                            } else {
                                return desc ? 1 : -1;
                            }
                        }
                    });

                    JsonArray array = new JsonArray();
                    for (int i = index; i < resultList.size() && i < (index + size); i++) {
                        JsonObject jsonObject = new JsonObject();
                        AppAdImpressionMetrics one = resultList.get(i);
                        jsonObject.addProperty("date", Utils.formatDate(one.date));
                        jsonObject.addProperty("app_name", one.appName);
                        jsonObject.addProperty("total_user", one.totalUser);
                        jsonObject.addProperty("active_user", one.activeUser);
                        jsonObject.addProperty("active_user_rate", formatRate(one.activeUser, one.totalUser));
                        jsonObject.addProperty("non_ad_user_rate", formatRate(one.activeUser - one.adUser > 0 ? one.activeUser - one.adUser : 0, one.activeUser));
                        jsonObject.addProperty("full_ad_user_rate", formatRate(one.fullAdUser, one.activeUser));
                        jsonObject.addProperty("full_ad_count", one.fullAdCount);
                        jsonObject.addProperty("one_full_ad_user_rate", formatRate(one.oneFullAdUser, one.activeUser));
                        jsonObject.addProperty("two_full_ad_user_rate", formatRate(one.twoFullAdUser, one.activeUser));
                        jsonObject.addProperty("three_full_ad_user_rate", formatRate(one.threeFullAdUser, one.activeUser));
                        jsonObject.addProperty("four_full_ad_user_rate", formatRate(one.fourFullAdUser, one.activeUser));
                        jsonObject.addProperty("five_full_ad_user_rate", formatRate(one.fiveFullAdUser, one.activeUser));
                        jsonObject.addProperty("native_ad_user_rate", formatRate(one.nativeAdUser, one.activeUser));
                        jsonObject.addProperty("native_ad_count", one.nativeAdCount);
                        jsonObject.addProperty("one_native_ad_user_rate", formatRate(one.oneNativeAdUser, one.activeUser));
                        jsonObject.addProperty("two_native_ad_user_rate", formatRate(one.twoNativeAdUser, one.activeUser));
                        jsonObject.addProperty("three_native_ad_user_rate", formatRate(one.threeNativeAdUser, one.activeUser));
                        jsonObject.addProperty("four_native_ad_user_rate", formatRate(one.fourNativeAdUser, one.activeUser));
                        jsonObject.addProperty("five_native_ad_user_rate", formatRate(one.fiveNativeAdUser, one.activeUser));
                        array.add(jsonObject);
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

    private String formatRate(long a, long b) {
        float v = b > 0 ? a * 1.0f / b : 0;

        return String.format("%.2f%%", v * 100);
    }
}
