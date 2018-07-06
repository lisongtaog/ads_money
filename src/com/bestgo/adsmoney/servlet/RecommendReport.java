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

/**
 * Desc: app互推报告统计.
 * Auth: maliang
 * Date: 2018/6/27 15:46
 */
@WebServlet(name = "RecommendReport", urlPatterns = {"/recommendReport/*"})
public class RecommendReport extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doRequest(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doRequest(request,response);
    }

    protected void doRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();
        if(path == null){
            response.getWriter().write(json.toString());
            return;
        }
        //常规业务逻辑
        if (path.startsWith("/query")) {
            //String tableName = "app_daily_metrics_history";
            String tableName = "app_recommend_daily_history";

            List<AppData> appData = AppManagement.fetchAllAppData();

            String startDate = request.getParameter("start_date");
            String endDate = request.getParameter("end_date");
            int index = Utils.parseInt(request.getParameter("page_index"), 0);
            int size = Utils.parseInt(request.getParameter("page_size"), 20);
            String dimension = request.getParameter("dimension");
            String filter = request.getParameter("filter");
            String filterTarget = request.getParameter("filterTarget");
            String filterCountry = request.getParameter("filterCountry");
            int order = Utils.parseInt(request.getParameter("order"), 0);
            boolean desc = order < 1000;
            if (order >= 1000) order = order - 1000;

            if (filter == null || filter.isEmpty()) {
                filter = "";
            }
            if (filterTarget == null || filterTarget.isEmpty()) {
                filterTarget = "";
            }
            if (filterCountry == null || filterCountry.isEmpty()) {
                filterCountry = "";
            }
            ArrayList<String> appIds = new ArrayList<>();
            ArrayList<String> targAppIds = new ArrayList<>();
            ArrayList<String> countryCodes = new ArrayList<>();
            String[] filters = filter.split(",");
            for (String appId : filters) {
                if (appId.isEmpty()) continue;
                appIds.add(appId);
            }
            filters = filterTarget.split(",");
            for (String targAppId : filters) {
                if (targAppId.isEmpty()) continue;
                targAppIds.add(targAppId);
            }
            filters = filterCountry.split(",");
            for (String countryCode : filters) {
                if (countryCode.isEmpty()) continue;
                countryCodes.add(countryCode);
            }

            try {
                StringBuffer countSql = new StringBuffer();
                countSql.append("SELECT DISTINCT r.date,r.app_id,r.country_code,r.target_app_id")
                        .append(" from app_recommend_daily_history r ")
                        .append(" WHERE date between '").append(startDate).append("' and '").append(endDate).append("' ");

                StringBuffer sqlBuff = new StringBuffer();
                sqlBuff.append("SELECT f.* ," )
                        .append(" IF(f.installed > 0,f.ad_installed * f.spend / f.installed,0) as ad_revenue," )
                        .append(" IF(f.ad_impression > 0 and f.installed > 0,f.ad_installed * f.spend / (f.ad_impression*f.installed) * 1000,0)  as ecpm," )
                        .append(" IF(f.ad_impression > 0,f.ad_click * 1.0 / f.ad_impression * 100,0) as ctr " )
                        .append("from (" );
                sqlBuff.append("SELECT d.date,d.app_id,d.country_code,d.target_app_id," )
                        .append("1* SUBSTRING_INDEX(d.more,'|',1)  AS spend,")  //历史花费
                        .append("1* SUBSTRING_INDEX(d.more,'|',-1) AS installed,") // 历史安装数量
                        .append("IFNULL(d.ad_impression,0) as ad_impression,") //互推展示数量
                        .append("IFNULL(d.ad_click,0) as ad_click,") //互推点击数量
                        .append("IFNULL(d.ad_installed,0) as ad_installed ")//互推安装数量
                        .append(" FROM (")
                                .append("SELECT DISTINCT r.date,r.app_id,r.country_code,r.target_app_id,fetchCPA(r.date,r.country_code,r.app_id) AS more,")
                                .append("(SELECT s1.value from app_recommend_daily_history s1 WHERE s1.action = '显示' and s1.date = r.date and s1.app_id = r.app_id and s1.country_code = r.country_code and s1.target_app_id = r.target_app_id) as ad_impression, ")
                                .append("(SELECT s2.value from app_recommend_daily_history s2 WHERE s2.action = '点击' and s2.date = r.date and s2.app_id = r.app_id and s2.country_code = r.country_code and s2.target_app_id = r.target_app_id) as ad_click, ")
                                .append("(SELECT s3.value from app_recommend_daily_history s3 WHERE s3.action = '安装' and s3.date = r.date and s3.app_id = r.app_id and s3.country_code = r.country_code and s3.target_app_id = r.target_app_id) as ad_installed ")
                                .append(" from app_recommend_daily_history r ")
                                .append("ORDER BY r.date,r.app_id,r.country_code,r.target_app_id")
                                .append(") d")
                        .append(" WHERE date between '").append(startDate).append("' and '").append(endDate).append("' ");

                if (appIds.size() > 0) {
                    String ss = "";
                    for (int i = 0; i < appIds.size(); i++) {
                        if (i < appIds.size() - 1) {
                            ss += "'" + appIds.get(i) + "',";
                        } else {
                            ss += "'" + appIds.get(i) + "'";
                        }
                    }
                    countSql.append(" and app_id in (" + ss + ")");
                    sqlBuff.append(" and app_id in (" + ss + ")");
                }

                if (targAppIds.size() > 0) {
                    String ss = "";
                    for (int i = 0; i < targAppIds.size(); i++) {
                        if (i < targAppIds.size() - 1) {
                            ss += "'" + targAppIds.get(i) + "',";
                        } else {
                            ss += "'" + targAppIds.get(i) + "'";
                        }
                    }
                    countSql.append(" and target_app_id in (" + ss + ")");
                    sqlBuff.append(" and target_app_id in (" + ss + ")");
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
                    countSql.append(" and country_code in (" + ss + ")");
                    sqlBuff.append(" and country_code in (" + ss + ")");
                }

                sqlBuff.append(" ) f");//三层嵌套

                List<JSObject> count = DB.findListBySql(countSql.toString());

                String[] orders = {" order by date "," order by app_id", " order by country_code"," order by target_app_id ",
                        " order by ad_impression", " order by ad_click"," order by ad_installed ",
                        " order by ad_revenue ", " order by ecpm "," order by ctr " //这三个值是计算出来的，暂时无法排序
                };
                if (order < orders.length) {//单列排序
                    sqlBuff.append(orders[order] + (desc ? " desc" : ""));
                }else{//默认排序
                    sqlBuff.append("ORDER BY d.date,d.app_id,d.country_code,d.target_app_id");
                }

                String sql = sqlBuff.toString();


                sql += " limit " + index * size + "," + size;
                List<JSObject> list = DB.findListBySql(sql);

                String[] columns = {"date","app_id","country_code","target_app_id","ad_impression","ad_click","ad_installed"};
                List<String> fields = Arrays.asList(columns);
                //从app_ads_daily_metrics_history中获取CPA作为互推应用的收益；计算互推应用的ECPM等
                JsonArray array = new JsonArray();
                for (int i = 0; i < list.size(); i++) {
                    JsonObject one = new JsonObject();
                    for (int ii = 0; ii < fields.size(); ii++) {
                        String f = fields.get(ii);
                        String v = list.get(i).get(f).toString();
                        if (f.equals("country_code")) {
                            HashMap<String, String> countryMap = Utils.getCountryMap();
                            String countryName = countryMap.get(v);
                            one.addProperty("country_name", countryName == null ? v : countryName);
                        } else if (f.equals("app_id")) {
                            for (int jj = 0; jj < appData.size(); jj++) {
                                if (appData.get(jj).appId.equals(v)) {
                                    one.addProperty("app_name", appData.get(jj).appName);
                                    break;
                                }
                            }
                        } else if (f.equals("target_app_id")) {
                            for (int jj = 0; jj < appData.size(); jj++) {
                                if (appData.get(jj).appId.equals(v)) {
                                    one.addProperty("target_app_name", appData.get(jj).appName);
                                    break;
                                }
                            }
                        }
                        one.addProperty(f, v);
                    }
                    one.addProperty("ad_impression", list.get(i).get("ad_impression").toString());
                    one.addProperty("ad_click", list.get(i).get("ad_click").toString());

                    //target目标app展示次数
                    int impression = Utils.parseInt(list.get(i).get("ad_impression").toString(), 0);
                    //target目标app点击数
                    long click = Utils.convertLong(list.get(i).get("ad_click"), 0);

                    double revenue = Utils.trimDouble(Utils.convertDouble(list.get(i).get("ad_revenue"),0));
                    double ecpm = Utils.trimDouble(Utils.convertDouble(list.get(i).get("ecpm"),0));
                    double ctr = Utils.trimDouble(Utils.convertDouble(list.get(i).get("ctr"),0));
                    one.addProperty("ad_revenue", revenue);
                    one.addProperty("ecpm", impression > 0 ? Utils.trimDouble(revenue / impression * 1000) : 0);
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
        }

        response.getWriter().write(json.toString());
    }
}
