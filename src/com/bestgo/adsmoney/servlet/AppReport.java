package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.utils.NumberUtil;
import com.bestgo.adsmoney.utils.Utils;
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

@WebServlet(name = "AppReport", urlPatterns = {"/app_report/*"})
public class AppReport extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/query")) {
                String tableName = "app_daily_metrics_history";

                List<AppData> appData = AppManagement.fetchAllAppData();
                Map<String,String> adUnitMap = AdUnitManagement.fetchAllUnitName();

                String startDate = request.getParameter("start_date");
                String endDate = request.getParameter("end_date");
                int index = Utils.parseInt(request.getParameter("page_index"), 0);
                int size = Utils.parseInt(request.getParameter("page_size"), 20);
                String dimension = request.getParameter("dimension");
                String filter = request.getParameter("filter");
                String filterCountry = request.getParameter("filterCountry");

                int order = Utils.parseInt(request.getParameter("order"), 0);

                if (dimension == null || dimension.isEmpty()) {
                    dimension = "";
                }
                ArrayList<String> fields = new ArrayList<>();
                String[] dim = dimension.split(",");
                for (String d : dim) {
                    if (d.isEmpty()) continue;
                    switch (d) {
                        case "1":
                            fields.add("date");
                            break;
                        case "2":
                            fields.add("app_id");
                            break;
                        case "3":
                            tableName = "app_ad_unit_metrics_history";
                            fields.add("ad_unit_id");
                            fields.add("ad_unit_name");
                            break;
                        case "4":
                            fields.add("country_code");
                            break;
                        case "5":
                            fields.add("ad_network");
                            break;
                    }
                }
                if (filter == null || filter.isEmpty()) {
                    filter = "";
                }
                if (filterCountry == null || filterCountry.isEmpty()) {
                    filterCountry = "";
                }
                ArrayList<String> appIds = new ArrayList<>();
                ArrayList<String> countryCodes = new ArrayList<>();
                String[] filters = filter.split(",");
                for (String appId : filters) {
                    if (appId.isEmpty()) continue;
                    appIds.add(appId);
                }
                filters = filterCountry.split(",");
                for (String countryCode : filters) {
                    if (countryCode.isEmpty()) continue;
                    countryCodes.add(countryCode);
                }

                String tagEcpmSql = "";
                boolean isShowTagEcpm = false;//是否展示 target ecpm
                //date app_id ad_unit_id 选择Dimension && 选择了app应用filter && ( 选择了Dimension OR 选择了国家filter)
                if(fields.contains("date") && fields.contains("app_id") && fields.contains("ad_unit_id") && appIds.size() > 0
                        && (fields.contains("country_code") || countryCodes.size() > 0)){
                    isShowTagEcpm = true; //否展示 target ecpm 列
                    tableName = "app_ad_unit_metrics_history u \n";
                    //要有逗号，拼接
                    tagEcpmSql = ",\n(SELECT GROUP_CONCAT(tag_ecpm ORDER BY uploadtime DESC) from app_ad_unit_target \n" +
                            "where app_id = u.app_id and ad_unit_id = u.ad_unit_id and country_code = u.country_code and DATE(uploadtime) = u.date\n" +
                            ") AS tag_ecpm_current,\n" +
                            "(SELECT tag_ecpm from app_ad_unit_target WHERE id =\n" +
                            "(SELECT MAX(id) FROM app_ad_unit_target where app_id = u.app_id and ad_unit_id = u.ad_unit_id and country_code = u.country_code AND DATE(uploadtime) < u.date )\n" +
                            ") AS tag_ecpm_pre \n";
                }

                try {
                    String sql = "select ";
                    String ff = "";
                    if (fields.size() > 0) {
                        for (int i = 0; i < fields.size(); i++) {
                            if("ad_unit_name".equals(fields.get(i))) continue;
                            ff += fields.get(i) + ",";
                        }
                        ff = ff.substring(0,ff.length()-1);
                        sql += ff + ",";
                    }
                    sql += " sum(ad_request) as ad_request, sum(ad_filled) as ad_filled, sum(ad_impression) as ad_impression, sum(ad_click) as ad_click, sum(ad_revenue) as ad_revenue "
                            + tagEcpmSql +
                            "from " + tableName + " " +
                            "where date between '" + startDate + "' and '" + endDate + "' ";
                    String appIdsStr = "";
                    if (appIds.size() > 0) {
                        for (int i = 0; i < appIds.size(); i++) {
                            if (i < appIds.size() - 1) {
                                appIdsStr += "'" + appIds.get(i) + "',";
                            } else {
                                appIdsStr += "'" + appIds.get(i) + "'";
                            }
                        }
                        sql += " and app_id in (" + appIdsStr + ")";
                    }
                    String countryCodesStr = "";
                    if (countryCodes.size() > 0) {
                        for (int i = 0; i < countryCodes.size(); i++) {
                            if (i < countryCodes.size() - 1) {
                                countryCodesStr += "'" + countryCodes.get(i) + "',";
                            } else {
                                countryCodesStr += "'" + countryCodes.get(i) + "'";
                            }
                        }
                        sql += " and country_code in (" + countryCodesStr + ")";
                    }
                    if (!ff.isEmpty()) {
                        sql += " group by " + ff;
                    }

                    List<JSObject> count = DB.findListBySql(sql);

                    sql = "select " +  (""!=ff ? ff + "," : "") + "ad_request,ad_filled,ad_impression,ad_click, ad_revenue " +
                            (isShowTagEcpm ? " ,IFNULL(tag_ecpm_current,tag_ecpm_pre) AS tag_ecpm " : "") +//没有当天的配置，则取在此之前配置的最新值
                            "\nfrom (\n" + sql + " \n) d  ";

                    ArrayList<String> allFields = new ArrayList<>();//用于排序使用
                    allFields.addAll(fields); fields.remove("ad_unit_name");
                    allFields.addAll(Arrays.asList("ad_request","ad_filled","ad_impression","ad_click","ad_revenue","ecpm","ctr"));
                    if(isShowTagEcpm){
                        allFields.add(allFields.size()-1,"tag_ecpm");
                    }
                    if (allFields.size() > 0) {
                        boolean desc = order < 1000;
                        if (order >= 1000) order = order - 1000;
                        String field = allFields.get(order);
                        if("ad_unit_name".equals(field)){
                            field = "ad_unit_id";
                        }
                        sql += " order by " + field +  (desc ? " desc" : " asc");
                    }
                    sql += " limit " + index * size + "," + size;
                    List<JSObject> list = DB.findListBySql(sql);
                    Map<String,ShowNum> showNumMap = null;
                    if (!fields.contains("ad_unit_id") && fields.contains("date")) { //只有在没有AdUnit并且有date字段的时候才展示
                        showNumMap = fetchShowNumMap(startDate,endDate,appIdsStr,countryCodesStr,fields);
                    }
                    Object tagEcpm = null;
                    JsonArray array = new JsonArray();
                    String countryCode = null;
                    String appId = null;
                    String eventDate = null;
                    ShowNum showNum = null;
                    HashMap<String, String> countryMap = Utils.getCountryMap();
                    for (int i = 0; i < list.size(); i++) {
                        JSObject js = list.get(i);
                        JsonObject one = new JsonObject();
                        for (int ii = 0; ii < fields.size(); ii++) {
                            String f = fields.get(ii);
                            String v = js.get(f).toString();
                            if (f.equals("country_code")) {
                                String countryName = countryMap.get(v);
                                one.addProperty("country_name", countryName == null ? v : countryName);
                            } else if (f.equals("app_id")) {
                                for (int jj = 0; jj < appData.size(); jj++) {
                                    if (appData.get(jj).appId.equals(v)) {
                                        one.addProperty("app_name", appData.get(jj).appName);
                                        break;
                                    }
                                }
                            }else if(f.equals("ad_unit_id")){
                                //ad_unit_id ad_unit_name
                                one.addProperty("ad_unit_id", v);
                                one.addProperty("ad_unit_name", adUnitMap.get(v));
                            }
                            one.addProperty(f, v);
                        }
                        if (showNumMap != null) {
                            countryCode = js.get("country_code");
                            if (countryCode == null) countryCode = "";
                            appId = js.get("app_id");
                            if (appId == null) appId = "";
                            eventDate = js.get("date").toString();
                            showNum = showNumMap.get(eventDate + appId + countryCode);
                            if (showNum == null) {
                                one.addProperty("total_num", 0);
                                one.addProperty("total_num_ready",0);
                                one.addProperty("total_num_ready_div_total_num",0);
                                one.addProperty("new_total_num", 0);
                                one.addProperty("new_total_num_ready",0);
                                one.addProperty("new_total_num_ready_div_new_total_num",0);
                            }else {
                                one.addProperty("total_num", showNum.totalNum);
                                one.addProperty("total_num_ready",showNum.totalNumReady);
                                one.addProperty("total_num_ready_div_total_num",showNum.totalNumReadyDivTotalNum);
                                one.addProperty("new_total_num", showNum.newTotalNum);
                                one.addProperty("new_total_num_ready",showNum.newTotalNumReady);
                                one.addProperty("new_total_num_ready_div_new_total_num",showNum.newTotalNumReadyDivNewTotalNum);
                            }
                        } else {
                            one.addProperty("total_num", 0);
                            one.addProperty("total_num_ready",0);
                            one.addProperty("total_num_ready_div_total_num",0);
                            one.addProperty("new_total_num", 0);
                            one.addProperty("new_total_num_ready",0);
                            one.addProperty("new_total_num_ready_div_new_total_num",0);
                        }


                        one.addProperty("ad_request", js.get("ad_request").toString());
                        one.addProperty("ad_filled", js.get("ad_filled").toString());
                        one.addProperty("ad_impression", js.get("ad_impression").toString());
                        one.addProperty("ad_click", js.get("ad_click").toString());
                        one.addProperty("ad_revenue", Utils.trimDouble(Utils.convertDouble(js.get("ad_revenue"), 0)));
                        int impression = Utils.parseInt(js.get("ad_impression").toString(), 0);
                        double revenue = Utils.trimDouble(Utils.convertDouble(js.get("ad_revenue"), 0));
                        long click = Utils.convertLong(js.get("ad_click"), 0);

                        one.addProperty("ecpm", impression > 0 ? Utils.trimDouble(revenue / impression * 1000) : 0);
                        if (isShowTagEcpm) {
                            tagEcpm = js.get("tag_ecpm");//变现后台 配置的目标ECPM（展示满足ecpm条件的广告）
                            one.addProperty("tag_ecpm", null != tagEcpm ? tagEcpm.toString() : "");
                        }
                        one.addProperty("ctr", impression > 0 ? Utils.trimDouble(click * 1.0 / impression * 100) : 0);
                        array.add(one);
                    }

                    json.addProperty("ret", 1);
                    json.addProperty("message", "成功");
                    json.addProperty("total", count.size());
                    json.add("data", array);
                } catch (Exception ex) {
                    json.addProperty("ret", 0);
                    json.addProperty("message", ex.getMessage());
                }
            } else if (path.equals("/get")) {
                String startDate = request.getParameter("start_date");
                String endDate = request.getParameter("end_date");
                String filter = request.getParameter("filter");
                String filterCountry = request.getParameter("filterCountry");

                if (filter == null || filter.isEmpty()) {
                    filter = "";
                }
                if (filterCountry == null || filterCountry.isEmpty()) {
                    filterCountry = "";
                }
                ArrayList<String> appIds = new ArrayList<>();
                ArrayList<String> countryCodes = new ArrayList<>();
                String[] filters = filter.split(",");
                for (String appId : filters) {
                    if (appId.isEmpty()) continue;
                    appIds.add(appId);
                }
                filters = filterCountry.split(",");
                for (String countryCode : filters) {
                    if (countryCode.isEmpty()) continue;
                    countryCodes.add(countryCode);
                }

                try {
                    String sql = "select date, sum(ad_request) as ad_request, sum(ad_filled) as ad_filled, sum(ad_impression) as ad_impression, sum(ad_click) as ad_click, sum(ad_revenue) as ad_revenue " +
                            "from app_daily_metrics_history " +
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
                    if (countryCodes.size() > 0) {
                        String ss = "";
                        for (int i = 0; i < countryCodes.size(); i++) {
                            if (i < countryCodes.size() - 1) {
                                ss += "'" + countryCodes.get(i) + "',";
                            } else {
                                ss += "'" + countryCodes.get(i) + "'";
                            }
                        }
                        sql += " and country_code in (" + ss + ")";
                    }
                    sql += " group by date order by date desc";
                    List<JSObject> list = DB.findListBySql(sql);

                    JsonArray array = new JsonArray();
                    for (int i = 0; i < list.size(); i++) {
                        JsonObject one = new JsonObject();
                        one.addProperty("date", ((Date)list.get(i).get("date")).getTime());
                        one.addProperty("ad_request", Utils.convertLong(list.get(i).get("ad_request"), 0));
                        one.addProperty("ad_filled", Utils.convertLong(list.get(i).get("ad_filled"), 0));
                        one.addProperty("ad_impression", Utils.convertLong(list.get(i).get("ad_impression"), 0));
                        one.addProperty("ad_click", Utils.convertLong(list.get(i).get("ad_click"), 0));
                        one.addProperty("ad_revenue", Utils.trimDouble(Utils.convertDouble(list.get(i).get("ad_revenue"), 0)));
                        int impression = Utils.parseInt(list.get(i).get("ad_impression").toString(), 0);
                        double revenue = Utils.trimDouble(Utils.convertDouble(list.get(i).get("ad_revenue"), 0));
                        one.addProperty("ecpm", impression > 0 ? Utils.trimDouble(revenue / impression * 1000) : 0);
                        array.add(one);
                    }

                    json.addProperty("ret", 1);
                    json.addProperty("message", "成功");
                    json.add("data", array);
                } catch (Exception ex) {
                    json.addProperty("ret", 0);
                    json.addProperty("message", ex.getMessage());
                }
            } else if (path.equals("/getFirebase")) {
                String startDate = request.getParameter("start_date");
                String endDate = request.getParameter("end_date");
                String filter = request.getParameter("filter");
                String filterCountry = request.getParameter("filterCountry");

                if (filter == null || filter.isEmpty()) {
                    filter = "";
                }
                if (filterCountry == null || filterCountry.isEmpty()) {
                    filterCountry = "";
                }
                ArrayList<String> appIds = new ArrayList<>();
                ArrayList<String> countryCodes = new ArrayList<>();
                String[] filters = filter.split(",");
                for (String appId : filters) {
                    if (appId.isEmpty()) continue;
                    appIds.add(appId);
                }
                filters = filterCountry.split(",");
                for (String countryCode : filters) {
                    if (countryCode.isEmpty()) continue;
                    countryCodes.add(countryCode);
                }

                try {
                    String sql = "select date, sum(total_user) as total_user, sum(active_user) as active_user, sum(installed) as installed, sum(uninstalled) as uninstalled, sum(today_uninstalled) as today_uninstalled " +
                            "from app_firebase_daily_metrics_history " +
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
                    if (countryCodes.size() > 0) {
                        String ss = "";
                        for (int i = 0; i < countryCodes.size(); i++) {
                            if (i < countryCodes.size() - 1) {
                                ss += "'" + countryCodes.get(i) + "',";
                            } else {
                                ss += "'" + countryCodes.get(i) + "'";
                            }
                        }
                        sql += " and country_code in (" + ss + ")";
                    }
                    sql += " group by date order by date desc";
                    List<JSObject> list = DB.findListBySql(sql);

                    JsonArray array = new JsonArray();
                    for (int i = 0; i < list.size(); i++) {
                        JsonObject one = new JsonObject();
                        one.addProperty("date", ((Date)list.get(i).get("date")).getTime());
                        one.addProperty("total_user", Utils.convertLong(list.get(i).get("total_user"), 0));
                        one.addProperty("active_user", Utils.convertLong(list.get(i).get("active_user"), 0));
                        one.addProperty("installed", Utils.convertLong(list.get(i).get("installed"), 0));
                        one.addProperty("uninstalled", Utils.convertLong(list.get(i).get("uninstalled"), 0));
                        one.addProperty("today_uninstalled", Utils.convertLong(list.get(i).get("today_uninstalled"), 0));
                        array.add(one);
                    }

                    json.addProperty("ret", 1);
                    json.addProperty("message", "成功");
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

    /**
     * 获取广告展示机会数的Map
     * key = date + (appId) + (countryCode)
     * appId和countryCode根据情况可能为空
     * @param startDate
     * @param endDate
     * @param appIds
     * @param countryCodes
     * @param fields
     * @return
     */
    private Map<String,ShowNum> fetchShowNumMap(String startDate,String endDate,String appIds,String countryCodes,ArrayList<String> fields){
        Map<String,ShowNum> map = new HashMap<>();
        String eventDate = "";
        String appId = "";
        String countryCode = "";
        String selectFiled = " aas.event_date";
        boolean existAppIds = false;
        boolean existCountryCodes = false;
        if (appIds != null && !appIds.isEmpty()) existAppIds = true;
        if (countryCodes != null && !countryCodes.isEmpty()) existCountryCodes = true;
        if (fields != null) {
            if (!fields.contains("date")) return map;
            if (fields.contains("app_id")) {
                selectFiled += ",aas.app_id";
            }
            if (fields.contains("country_code")) {
                selectFiled += ",aas.country_code";
            }
        }
        String sql = "SELECT " + selectFiled + ",\n" +
                "SUM(aas.num) AS total_num,SUM(aas.num_ready) AS total_num_ready,\n" +
                "SUM(CASE WHEN aas.installed_date = aas.event_date THEN num ELSE 0 END) AS new_num,\n"+
                "SUM(CASE WHEN aas.installed_date = aas.event_date THEN num_ready ELSE 0 END) AS new_ready\n" +
                "FROM app_ads_adchance_statistics aas\n" +
                "WHERE aas.event_date between '" + startDate + "' AND '"+endDate + "'\n" +
                (existAppIds ? "AND aas.app_id IN (" + appIds + ")\n" : "") +
                (existCountryCodes ? "AND aas.country_code IN (" + countryCodes + ")\n" : "")
                + " GROUP BY " + selectFiled;
        List<JSObject> list = null;
        ShowNum showNum = null;
        try {
            list = DB.findListBySql(sql);
            for (int i = 0,len = list.size();i < len;i++) {
                JSObject one = list.get(i);
                if (one.hasObjectData()) {
                    eventDate = one.get("event_date").toString();
                    appId = one.get("app_id");
                    if (appId == null) appId = "";
                    countryCode = one.get("country_code");
                    if (countryCode == null) countryCode = "";
                    showNum = new ShowNum();
                    showNum.totalNum = NumberUtil.convertDouble(one.get("total_num"),0);
                    showNum.totalNumReady = NumberUtil.convertDouble(one.get("total_num_ready"),0);
                    showNum.totalNumReadyDivTotalNum = showNum.totalNum > 0 ? NumberUtil.trimDouble(showNum.totalNumReady / showNum.totalNum,4) : 0;
                    showNum.newTotalNum = NumberUtil.convertDouble(one.get("new_num"),0);
                    showNum.newTotalNumReady = NumberUtil.convertDouble(one.get("new_ready"),0);
                    showNum.newTotalNumReadyDivNewTotalNum = showNum.newTotalNum > 0 ? NumberUtil.trimDouble(showNum.newTotalNumReady / showNum.newTotalNum,4) : 0;
                    //不管应用和国家哪个是空串，都只看selectField
                    map.put(eventDate + appId + countryCode,showNum);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
    private class ShowNum{
        public double totalNum;
        public double totalNumReady;
        public double totalNumReadyDivTotalNum;
        public double newTotalNum;
        public double newTotalNumReady;
        public double newTotalNumReadyDivNewTotalNum;
    }
}
