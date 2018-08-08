package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.utils.NumberUtil;
import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.common.database.services.DB;
import com.bestgo.common.database.utils.JSObject;
import com.google.api.client.json.Json;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author mengjun
 * @date 2018/7/5 14:25
 * @description 应用活跃用户统计
 */
@WebServlet(name = "QueryAppActiveUserStatistics", urlPatterns = {"/query_app_active_user_statistics"})
public class QueryAppActiveUserStatistics extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String date = request.getParameter("date");
        String appId = request.getParameter("appId");
        String countryCode = request.getParameter("countryCode");

//        date = "2018-06-20";
//        appId = "com.androapplite.vpn10";
//        countryCode = "TM";

        JsonObject rtnJson = new JsonObject();//待返回前端的数据
        try {

            //购买安装的 花费、安装量
            String purchaseInstallSql = "SELECT SUM(spend) as purchase_cost,SUM(installed) as purchase_installed FROM app_ads_daily_metrics_history ";//购买安装量
            String allInstallSql = "SELECT SUM(installed) AS all_installed FROM app_firebase_daily_metrics_history ";//总安装量
            String revenueSql = "SELECT sum(ad_revenue) as ad_revenue, sum(ad_impression) as ad_impression from app_ad_unit_metrics_history "
                    + " WHERE ad_unit_id IN (SELECT ad_unit_id from app_ad_unit_config WHERE flag = '1' AND app_id='"+appId+"') \n" ;

            purchaseInstallSql += " where date ='"+date+"' ";
            allInstallSql += " where date ='"+date+"' ";
            revenueSql += " AND date ='"+date+"' ";
            if(!"all".equals(appId) && !appId.isEmpty()){
                purchaseInstallSql += " and app_id ='"+appId+"' ";
                allInstallSql += " and app_id ='"+appId+"' ";
                revenueSql += " and app_id ='"+appId+"' ";
            }
            if(!"all".equals(countryCode) && !countryCode.isEmpty()){
                purchaseInstallSql += " and country_code ='"+countryCode+"' ";
                allInstallSql += " and country_code ='"+countryCode+"' ";
                revenueSql += " and country_code ='"+countryCode+"' ";
            }

            JSObject obj = DB.findOneBySql(purchaseInstallSql);//购买安装量
            JSObject obj2 = DB.findOneBySql(allInstallSql);//总安装量
            JSObject revenueObj = DB.findOneBySql(revenueSql);//首日收益数据

            JsonObject summary = new JsonObject();//表头 通用汇总信息
            double allInstalled = 0,firstRevenue = 0,firstImpression = 0;
            try {
                double purchaseCost=0,purchaseInstalled=0;
                if(obj.get("purchase_installed") != null && !"".equals(obj.get("purchase_installed"))){
                    //purchaseInstalled = Utils.parseInt(obj.get("purchase_installed"),0);
                    purchaseCost = Utils.trimDouble(new BigDecimal(obj.get("purchase_cost").toString()).doubleValue());
                    purchaseInstalled = new BigDecimal(obj.get("purchase_installed").toString()).doubleValue();
                }
                if(obj2.get("all_installed") != null && !"".equals(obj2.get("all_installed"))){
                    //allInstalled = Utils.parseInt(obj.get("all_installed"),0);
                    allInstalled = new BigDecimal(obj2.get("all_installed").toString()).doubleValue();
                }

                if(null != revenueObj.get("ad_revenue") && !"".equals(revenueObj.get("ad_revenue"))){
                    firstRevenue = Utils.trimDouble(new BigDecimal(revenueObj.get("ad_revenue").toString()).doubleValue());
                }
                if(null != revenueObj.get("ad_impression") && !"".equals(revenueObj.get("ad_impression"))){
                    firstImpression = new BigDecimal(revenueObj.get("ad_impression").toString()).doubleValue();
                }

                summary.addProperty("installDate",date);//安装日期
                summary.addProperty("totalInstall",allInstalled);//总安装量
                summary.addProperty("purchaseInstall",purchaseInstalled);//购买安装量
                summary.addProperty("purchaseCost",purchaseCost);//购买花费cost
                //summary.addProperty("purchasePer",Utils.trimDouble(allInstalled >0 ? 100*(purchaseInstalled / allInstalled): 0));//购买安装占比
                summary.addProperty("purchaseCpa",Utils.trimDouble(purchaseInstalled >0 ? (purchaseCost / purchaseInstalled): 0));//CPA

                //summary.addProperty("appId","com.androapplite.antivirus.antivirusapplication");//应用appid
                summary.addProperty("appVersion","待完善…");//应用版本号
                summary.addProperty("firstRevenue",firstRevenue);//首日收益
                summary.addProperty("firstImpression",firstImpression);//首日展示次数

            }catch (Exception e){
                e.printStackTrace();
            }
            rtnJson.add("summary",summary);//汇总表头


            String subGroupBy = ""; String country_query = "";
            StringBuffer subCondition = new StringBuffer();//拼接 appId，country_code查询条件
            if( null != appId && !appId.isEmpty() && !"all".equals(appId) ){//app_id是必录项
                subCondition.append(" AND app_id = '").append(appId).append( "' ");
                //subGroupBy += ",app_id" ;
            }
            if( null != countryCode && !countryCode.isEmpty() && !"all".equals(countryCode) ){
                subCondition.append(" AND country_code = '").append(countryCode).append( "' ");
                subGroupBy += ",country_code" ;
                country_query = ",country_code ";
            }

            String activeSql = "SELECT event_date,sum(active_num) AS total_acitve_num FROM app_active_user_statistics " +
                    "WHERE installed_date = '" + date + "' " + subCondition.toString() +
                    "AND event_date >= '" + date + "' " +
                    "GROUP BY event_date ORDER BY event_date";

            //计算在某个安装日期内（的某个应用在某个国家中）的每个展示日期 展示次数
            String impressionsSql = "SELECT event_date,sum(impressions) AS impressions FROM app_ads_impressions_statistics " +
                    "WHERE installed_date = '" + date + "' " + subCondition.toString() +
                    "AND event_date >= '" + date + "' " +
                    "GROUP BY event_date ORDER BY event_date";

            String adUnitCondition = " AND ad_unit_id IN (SELECT ad_unit_id from app_ad_unit_config WHERE flag = '0' AND app_id='"+appId+"') \n";
            //首日安装用户 在后续日期某一天的 展示次数（非新用户广告单元）
            String imressionEvent = "SELECT event_date"+country_query+",ad_unit_id,SUM(impressions) AS impressions " +
                    "FROM app_ads_impressions_statistics WHERE 1=1 "+ subCondition.toString() +
                    "AND installed_date = '"+ date +"' AND event_date > '" + date + "' \n"
                    + adUnitCondition +
                    "GROUP BY event_date" + subGroupBy + ",ad_unit_id \n" +
                    "ORDER BY event_date ASC";
            //所有安装用户(安装日期后30天内) 在后续日期某一天的 展示次数（非新用户广告单元）
            String imressionAllEvent = "SELECT event_date"+country_query+",ad_unit_id,SUM(impressions) AS impressions " +
                    "FROM app_ads_impressions_statistics WHERE 1=1 " + subCondition.toString() +
                    "AND event_date > installed_date AND event_date > '"+date+"' \n"
                    + adUnitCondition +
                    "GROUP BY event_date" + subGroupBy + ",ad_unit_id \n" +
                    "ORDER BY event_date ASC";

            //变现数据
            String eventRevenueSql = "SELECT date"+country_query+",ad_unit_id,SUM(ad_revenue) AS ad_revenue " +
                    "from app_ad_unit_metrics_history WHERE 1=1 "+ subCondition.toString() +
                    " AND date > '"+date+"' \n"
                    + adUnitCondition +
                    "GROUP BY date" + subGroupBy + ",ad_unit_id \n" +
                    "ORDER BY date ASC";

            Map<String,Double> impressionEventMap = new HashMap<String,Double>();//新用户在后续每日的 广告展示次数
            Map<String,Double> impressionAllEventMap = new HashMap<String,Double>();//所有用户的 广告展示次数
            Map<String,Double> eventRevenueMap = new LinkedHashMap<String,Double>();//收益(用于存取当日收入)

            List<JSObject> impressionEventList = DB.findListBySql(imressionEvent);
            List<JSObject> impressionAllEventList = DB.findListBySql(imressionAllEvent);
            List<JSObject> eventRevenueList = DB.findListBySql(eventRevenueSql);

            String eventDate = null,adUnitId=null,key = null;
            JSObject tmp = null;double impr = 0D;
            for (int i = 0,len = impressionEventList.size();i < len;i++) {
                tmp = impressionEventList.get(i);
                if (tmp.hasObjectData()) {
                    eventDate = tmp.get("event_date").toString();
                    adUnitId = tmp.get("ad_unit_id").toString();
                    impr = new BigDecimal(tmp.get("impressions").toString()).doubleValue();//展示次数
                    key = eventDate + "_" + countryCode + "_" + adUnitId;
                    impressionEventMap.put(key,impr);
                }
            }
            impressionEventList.clear();impressionEventList = null;

            for (int i = 0,len = impressionAllEventList.size();i < len;i++) {
                tmp = impressionAllEventList.get(i);
                if (tmp.hasObjectData()) {
                    eventDate = tmp.get("event_date").toString();
                    adUnitId = tmp.get("ad_unit_id").toString();
                    impr = new BigDecimal(tmp.get("impressions").toString()).doubleValue();//展示次数
                    key = eventDate + "_" + countryCode + "_" + adUnitId;
                    impressionAllEventMap.put(key,impr);
                }
            }
            impressionAllEventList.clear();impressionAllEventList = null;

            Double currentRevenue,num1,num2;
            String tmpKey = null;Double sumRevenue = 0D;

            eventRevenueMap.put(date,firstRevenue);//设置当日安装的收益，直接取 变现数据值
            for (int i = 0,len = eventRevenueList.size();i < len;i++) {
                tmp = eventRevenueList.get(i);
                if (tmp.hasObjectData()) {
                    eventDate = tmp.get("date").toString();
                    adUnitId = tmp.get("ad_unit_id").toString();
                    impr = new BigDecimal(tmp.get("ad_revenue").toString()).doubleValue();//总收益
                    key = eventDate + "_" + countryCode + "_" + adUnitId;
                    num1 = impressionEventMap.get(key);//新安装 后面某日的 广告展示次数
                    num2 = impressionAllEventMap.get(key);//老用户 总广告展示次数
                    impressionEventMap.remove(key);//移除元素
                    impressionAllEventMap.remove(key);//移除元素
                    num1 = (null == num1 ? 0 : num1);
                    num2 = (null == num2 ? 0 : num2);
                    currentRevenue = 0D;//为每个广告单元的收益
                    if(num2 > 0){
                        currentRevenue = impr * num1 / num2 ;
                    }
                    tmpKey = eventDate;//活跃用户 只按照date分组，此处需将满足该date的数据 求和计算
                    sumRevenue = eventRevenueMap.get(tmpKey);//为 各个广告单元 在event_date的收益 总和
                    sumRevenue = (null == sumRevenue) ? currentRevenue :(sumRevenue + currentRevenue);
                    eventRevenueMap.put(tmpKey,sumRevenue);//key为日期
                }
            }
            eventRevenueList.clear(); eventRevenueList = null;
            impressionEventMap.clear();impressionEventMap = null;impressionAllEventMap.clear();impressionAllEventMap=null;

            List<JSObject> activeList = DB.findListBySql(activeSql);//活跃用户
            List<JSObject> impressionsList = DB.findListBySql(impressionsSql);//展示次数

            String activeUserFirstEventDate = date;
            if(activeList.size() > 0 && activeList.get(0).hasObjectData()){
                activeUserFirstEventDate = activeList.get(0).get("event_date").toString();//取活跃用户第一天的展示日期
            }
            double firstImpressions = 0;//首日展示次数
            double impressions = 0D;double sumImpressions = 0D;//累计展示次数
            Map<String,List> impressionMap = new HashMap<String,List>();
            List<Double> impressionList = null;
            for (int i = 0,len = impressionsList.size();i < len;i++) {
                JSObject impressionsJS = impressionsList.get(i);
                if (impressionsJS.hasObjectData()) {
                    impressionList = new ArrayList();
                    eventDate = impressionsJS.get("event_date").toString();//event_date
                    impressions = new BigDecimal(impressionsJS.get("impressions").toString()).doubleValue();//展示次数
                    if(activeUserFirstEventDate.equals(eventDate)){
                        firstImpressions = impressions;
                    }
                    sumImpressions += impressions;//累计展示次数

                    impressionList.add(impressions);//展示次数
                    impressionList.add(sumImpressions);//累计展示次数
                    impressionList.add(Utils.trimDouble(firstImpressions >0 ? 100*(impressions / firstImpressions): 0));//首日展示占比
                    impressionList.add(Utils.trimDouble(allInstalled >0 ? (sumImpressions / allInstalled): 0));//人均广告展示次数=累计的总的广告展示次数/firebase安装量

                    impressionMap.put(eventDate,impressionList);
                }
            }

            Map<String,List> activeMap = new HashMap<String,List>();//活跃信息
            List<Double> activeDataList = null;
            double installActive = 0;Double nowRevenue;
            JSObject activeJS = null;
            double sumActiveNum = 0;//累计活跃用户数，即自安装日至当日的活跃用户数总和
            for (int i = 0,len = activeList.size();i < len;i++) {
                activeJS = activeList.get(i);
                if (activeJS.hasObjectData()) {
                    activeDataList = new ArrayList();
                    eventDate = activeJS.get("event_date").toString();
                    double totalActiveNum = Utils.convertDouble(activeJS.get("total_acitve_num"),0);
                    double activeNum = NumberUtil.trimDouble(totalActiveNum,0);
                    sumActiveNum += activeNum;//累计活跃用户数
                    if(i == 0){
                        installActive = activeNum;
                    }
                    activeDataList.add(activeNum);//活跃用户数
                    activeDataList.add(Utils.trimDouble(allInstalled >0 ? 100*(activeNum / allInstalled): 0));//活跃占比
                    activeDataList.add(Utils.trimDouble(installActive >0 ? 100*(activeNum / installActive): 0));//首日占比
                    activeDataList.add(Utils.trimDouble(allInstalled >0 ? 100*(sumActiveNum / allInstalled): 0));//累计活跃占比(累计活跃占比=自安装日至当日的活跃用户数总和/firebase安装量)

                    activeMap.put(eventDate,activeDataList);
                }
            }


            JsonArray dataArray = new JsonArray();
            JsonArray item = null;
            sumRevenue = 0D;//累计活跃用户 收入，即自安装日至当日的活跃用户数 收益总和
            for (Iterator<String> ite = eventRevenueMap.keySet().iterator();ite.hasNext();) {
                eventDate = ite.next();
                item = new JsonArray();

                activeDataList = activeMap.get(eventDate);//活跃信息
                item.add(eventDate);
                if(null != activeDataList){
                    item.add(activeDataList.get(0));//活跃用户数
                    item.add(activeDataList.get(1));//活跃占比
                    item.add(activeDataList.get(2));//首日占比
                    item.add(activeDataList.get(3));//累计活跃占比(累计活跃占比=自安装日至当日的活跃用户数总和/firebase安装量)
                }else {
                    item.add("-");
                    item.add("-");//活跃占比
                    item.add("-");//首日占比
                    item.add("-");//累计活跃占比(累计活跃占比=自安装日至当日的活跃用户数总和/firebase安装量)
                }

                impressionList = impressionMap.get(eventDate);//展示次数
                if (null != impressionList){
                    //item.add(impressionList.get(0));//广告展示次数
                    //item.add(impressionList.get(1));//累计展示次数
                    //item.add(impressionList.get(2));//首日展示占比%
                    item.add(impressionList.get(3));//人均广告展示次数=累计的总的广告展示次数/firebase安装量
                }else {
                    //item.add("-");//广告展示次数
                    //item.add("-");//累计展示次数
                    //item.add("-");//首日展示占比%
                    item.add("-");//人均广告展示次数
                }
                nowRevenue = eventRevenueMap.get(eventDate);//当日收入
                nowRevenue = (null == nowRevenue) ? 0D : nowRevenue;
                sumRevenue += nowRevenue;

                item.add(Utils.trimDouble(nowRevenue));//当日收入
                item.add(Utils.trimDouble(sumRevenue));//累计收入
                double ltv = Utils.trimDouble(allInstalled >0 ? (sumRevenue / allInstalled): 0);//ltv
                item.add(ltv);//ltv
                double cpa = summary.get("purchaseCpa").getAsDouble();//cpa
                item.add(Utils.trimDouble(cpa > 0 ? 100*(ltv / cpa) : 100));//回本率(ltv / cpa)

                dataArray.add(item);
            }
            rtnJson.add("dataArray",dataArray);
            rtnJson.addProperty("ret", 1);

        } catch (Exception ex) {
            ex.printStackTrace();
            rtnJson.addProperty("ret", 0);
            rtnJson.addProperty("message", ex.getMessage());
        }
        response.getWriter().write(rtnJson.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }
}
