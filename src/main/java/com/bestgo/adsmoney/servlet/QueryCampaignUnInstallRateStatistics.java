package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.utils.DateUtil;
import com.bestgo.adsmoney.utils.NumberUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mengjun
 * @date 2018/9/18 16:04
 * @desc 查询系列卸载率的接口
 */
@WebServlet(name = "QueryCampaignUnInstallRateStatistics", urlPatterns = "/query_campaign_uninstall_rate_statistics")
public class QueryCampaignUnInstallRateStatistics extends HttpServlet {
    private class CampaignUninstall {
        public double installNum;
        public double uninstallNum;
        public double uninstallRate;
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String installedDate = request.getParameter("installedDate");
        String threeDaysAgoInstalledDate = DateUtil.addDay(installedDate,-3,"yyyy-MM-dd");

        HashMap<String, CampaignUninstall> campaignUninstallMap = new HashMap<>();
        List<JSObject> list = null;
        CampaignUninstall campaignUninstall = null;
        String key = null;
        JsonArray array = new JsonArray();
        if ("iLoveMoney".equals(token)) {
            try {
                String sql = "SELECT installed_date,app_id,country_code,campaign_name,SUM(user_num) AS two_days_uninstall_sum \n" +
                        "FROM app_campaign_uninstall_statistics\n" +
                        "WHERE installed_date BETWEEN '" + threeDaysAgoInstalledDate + "' AND '" + installedDate + "'\n" +
                        "AND event_date BETWEEN installed_date AND DATE_ADD(installed_date,INTERVAL 1 DAY)\n" +
                        "AND event_name = 'app_remove'\n" +
                        "GROUP BY installed_date,app_id,country_code,campaign_name";
                list = DB.findListBySql(sql);
                JSObject one = null;
                for (int i = 0,len = list.size();i < len; i++) {
                    one = list.get(i);
                    if (one.hasObjectData()) {
                        campaignUninstall = new CampaignUninstall();
                        key = one.get("installed_date").toString() + ":" + one.get("app_id") + ":" + one.get("country_code") + ":" + one.get("campaign_name");
                        campaignUninstall.uninstallNum = NumberUtil.convertDouble(one.get("two_days_uninstall_sum"),0);
                        campaignUninstallMap.put(key,campaignUninstall);
                    }
                }
                sql = "SELECT installed_date,app_id,country_code,campaign_name,SUM(user_num) AS install_sum \n" +
                        "FROM app_campaign_uninstall_statistics\n" +
                        "WHERE installed_date BETWEEN '" + threeDaysAgoInstalledDate + "' AND '" + installedDate + "'\n" +
                        "AND event_name = 'first_open'\n" +
                        "GROUP BY installed_date,app_id,country_code,campaign_name";
                list = DB.findListBySql(sql);
                for (int i = 0,len = list.size();i < len; i++) {
                    one = list.get(i);
                    if (one.hasObjectData()) {
                        key = one.get("installed_date").toString() + ":" + one.get("app_id") + ":" + one.get("country_code") + ":" + one.get("campaign_name");
                        campaignUninstall = campaignUninstallMap.get(key);
                        if (campaignUninstall == null) {
                            campaignUninstall = new CampaignUninstall();
                        }
                        campaignUninstall.installNum = NumberUtil.convertDouble(one.get("install_sum"),0);
                        campaignUninstall.uninstallRate = campaignUninstall.installNum > 0 ? campaignUninstall.uninstallNum / campaignUninstall.installNum : 0;
                        campaignUninstallMap.put(key,campaignUninstall);
                    }
                }
                if (campaignUninstallMap.size() > 0) {
                    JsonObject jsonObject = null;
                    for (Map.Entry<String,CampaignUninstall> entry : campaignUninstallMap.entrySet()) {
                        jsonObject = new JsonObject();
                        key = entry.getKey();
                        campaignUninstall = entry.getValue();
                        String[] split = key.split(":");
                        jsonObject.addProperty("installedDate",split[0]);
                        jsonObject.addProperty("appId",split[1]);
                        jsonObject.addProperty("countryCode",split[2]);
                        jsonObject.addProperty("campaignName",split[3]);
                        jsonObject.addProperty("uninstallRate",campaignUninstall.uninstallRate);
                        jsonObject.addProperty("installNum",campaignUninstall.installNum);
                        array.add(jsonObject);
                    }
                }
                response.getWriter().write(array.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                campaignUninstallMap = null;
                array = null;
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
