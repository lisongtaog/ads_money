package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.Utils;
import com.bestgo.adsmoney.bean.AppData;
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

@WebServlet(name = "CtrMonitor", urlPatterns = {"/ctr_monitor/*"})
public class CtrMonitor extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/query")) {
                double target = Utils.parseDouble(request.getParameter("target"), 5);

                String sql = "select app_ad_unit_metrics.app_id,app_data.app_name, app_ad_unit_metrics.ad_unit_id, \n" +
                        "sum(ad_request) as ad_request, sum(ad_filled) as ad_filled, sum(ad_impression) as ad_impression, sum(ad_click) as ad_click, sum(ad_revenue) as ad_revenue\n" +
                        "from app_ad_unit_metrics, app_data where app_ad_unit_metrics.app_id=app_data.app_id\n" +
                        "group by app_ad_unit_metrics.app_id,app_data.app_name, app_ad_unit_metrics.ad_unit_id";

                try {
                    List<JSObject> list = DB.findListBySql(sql);
                    JsonArray array = new JsonArray();
                    for (int i = 0; i < list.size(); i++) {
                        JSObject one = list.get(i);
                        double click = Utils.convertDouble(one.get("ad_click"), 0);
                        double impression = Utils.convertDouble(one.get("ad_impression"), 0);
                        double revenue = Utils.convertDouble(one.get("ad_revenue"), 0);
                        one.put("ctr", impression > 0 ? click / impression * 100: 0);
                        one.put("ecpm", impression > 0 ? revenue / impression * 1000 : 0);
                    }
                    Collections.sort(list, new Comparator<JSObject>() {
                        @Override
                        public int compare(JSObject o1, JSObject o2) {
                            double ctr = o1.get("ctr");
                            double ctr1 = o2.get("ctr");
                            if (ctr > ctr1) {
                                return -1;
                            } else if (ctr < ctr1) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    });

                    for (int  i = 0; i < list.size(); i++) {
                        JSObject one = list.get(i);
                        String appId = one.get("app_id");
                        String appName = one.get("app_name");
                        String appUnitId = one.get("ad_unit_id");
                        long adRequest = Utils.convertLong(one.get("ad_request"), 0);
                        double click = Utils.convertDouble(one.get("ad_click"), 0);
                        double impression = Utils.convertDouble(one.get("ad_impression"), 0);
                        double revenue = Utils.convertDouble(one.get("ad_revenue"), 0);
                        double ctr = one.get("ctr");
                        double ecpm = one.get("ecpm");

                        if (impression < 100) {
                            continue;
                        }
                        if (ctr <= target) {
                            continue;
                        }

                        JsonObject object = new JsonObject();
                        object.addProperty("app_id", appId);
                        object.addProperty("app_name", appName);
                        object.addProperty("ad_unit_id", appUnitId);
                        object.addProperty("ad_request", adRequest);
                        object.addProperty("ad_click", click);
                        object.addProperty("ad_impression", Utils.trimDouble(impression));
                        object.addProperty("ad_revenue", Utils.trimDouble(revenue));
                        object.addProperty("ctr", Utils.trimDouble(ctr));
                        object.addProperty("ecpm", Utils.trimDouble(ecpm));

                        array.add(object);
                    }

                    json.addProperty("ret", 1);
                    json.addProperty("message", "成功");
                    json.addProperty("total", array.size());
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
