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

        JsonObject rtnJson = new JsonObject();//待返回前端的数据
        try {
            //app版本号查询
            String appVersionSql =
                    "SELECT DATE(create_time) AS publish_date,GROUP_CONCAT(version_number ORDER BY version_number SEPARATOR ' & ') AS app_version \n" +
                    " FROM app_version_number WHERE app_id = '"+appId+"' \n" +
                    " AND DATE(create_time) = (SELECT DATE(MAX(create_time)) from app_version_number WHERE DATE(create_time) <= '"+date+"') ";                    ;

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

            JSObject versionObj = DB.findOneBySql(appVersionSql);//app版本信息
            JSObject obj = DB.findOneBySql(purchaseInstallSql);//购买安装量
            JSObject obj2 = DB.findOneBySql(allInstallSql);//总安装量
            JSObject revenueObj = DB.findOneBySql(revenueSql);//首日收益数据

            JsonObject summary = new JsonObject();//表头 通用汇总信息
            double allInstalled = 0,firstRevenue = 0,firstImpression = 0,firstEcpm = 0;
            String appVersion = "";
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
                    firstEcpm = firstImpression > 0 ? firstRevenue / firstImpression * 1000 : 0;
                    firstEcpm = Utils.trimDouble(firstEcpm);
                }

                if(null != versionObj.get("app_version") && !"".equals(versionObj.get("app_version"))){
                    appVersion = versionObj.get("app_version");
                }

                summary.addProperty("installDate",date);//安装日期
                summary.addProperty("totalInstall",allInstalled);//总安装量
                summary.addProperty("purchaseInstall",purchaseInstalled);//购买安装量
                summary.addProperty("purchaseCost",purchaseCost);//购买花费cost
                //summary.addProperty("purchasePer",Utils.trimDouble(allInstalled >0 ? 100*(purchaseInstalled / allInstalled): 0));//购买安装占比
                summary.addProperty("purchaseCpa",Utils.trimDouble(purchaseInstalled >0 ? (purchaseCost / purchaseInstalled): 0));//CPA

                //summary.addProperty("appId","com.androapplite.antivirus.antivirusapplication");//应用appid
                summary.addProperty("appVersion",appVersion);//应用版本号
                summary.addProperty("firstRevenue",firstRevenue);//首日收益
                summary.addProperty("firstImpression",firstImpression);//首日展示次数
                summary.addProperty("firstEcpm",firstEcpm);//首日ecpm = 首日收益/首日展示次数 * 1000 ；分子、分母均为money变现数据

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

            //firebase 计算在某个安装日期内（的某个应用在某个国家中）的每个展示日期 展示次数（不区分新老用户）
            String impressionsSql = "SELECT event_date,sum(impressions) AS impressions FROM app_ads_impressions_statistics " +
                    "WHERE installed_date = '" + date + "' " + subCondition.toString() +
                    "AND event_date >= '" + date + "' " +
                    "GROUP BY event_date ORDER BY event_date";

            //firebase 1、计算（某个应用在某个国家中,所有安装日期）的安装日期 的展示次数 【新用户】；ecpm计算使用
            //firebase 2、计算（某个应用在某个国家中,所有安装日期）的每个展示日期 展示次数 【老用户】；ecpm计算使用
            /*String impressionsUnionSql = //区分新老用户的展示次数
                    "SELECT event_date,sum(impressions) AS impressions FROM app_ads_impressions_statistics \n" +
                    " WHERE ad_unit_id IN (SELECT ad_unit_id from app_ad_unit_config WHERE flag = '1' AND app_id='"+appId+"') \n"
                    + subCondition.toString() +
                    "\n AND event_date = '" + date + "' "
                    +"\n UNION \n" +
                    "SELECT event_date,sum(impressions) AS impressions FROM app_ads_impressions_statistics \n" +
                    " WHERE ad_unit_id IN (SELECT ad_unit_id from app_ad_unit_config WHERE flag = '0' AND app_id='"+appId+"') \n"
                    + subCondition.toString() +
                    "\n AND event_date > '" + date + "' \n" + //老用户 不包含安装日期当天
                    " GROUP BY event_date ORDER BY event_date ASC ";*/

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

            //变现数据，大于安装日期的 所有老用户的收益（广告单元flag为0）
            String eventRevenueSql = "SELECT date"+country_query+",ad_unit_id,SUM(ad_revenue) AS ad_revenue,SUM(ad_impression) AS ad_impression "
                    + "from app_ad_unit_metrics_history WHERE 1=1 "+ subCondition.toString() +
                    " AND date < DATE(NOW()) AND date > '"+date+"' \n"
                    + adUnitCondition +
                    "GROUP BY date" + subGroupBy + ",ad_unit_id \n" +
                    "ORDER BY date ASC";

            Map<String,Double> impressionEventMap = new HashMap<String,Double>();//新用户在后续每日的 广告展示次数
            Map<String,Double> impressionAllEventMap = new HashMap<String,Double>();//所有用户的 广告展示次数
            Map<String,RevenueData> eventRevenueMap = new LinkedHashMap<String,RevenueData>();//收益-列表(用于存取当日收入)

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

            RevenueData revenueData = new RevenueData();//首日的
            revenueData.nowRevenue = firstRevenue;
            revenueData.revenue = firstRevenue;
            revenueData.impression = firstImpression;
            eventRevenueMap.put(date,revenueData);//设置当日安装的收益，直接取 变现数据值

            Double currentRevenue,num1,num2;//currentRevenue：按照广告展示 比例计算的收益（currentRevenue = revenue * num1 /num2 ）
            Double revenue;//变现的 收益数据
            for (int i = 0,len = eventRevenueList.size();i < len;i++) {
                tmp = eventRevenueList.get(i);
                if (tmp.hasObjectData()) {
                    eventDate = tmp.get("date").toString();
                    adUnitId = tmp.get("ad_unit_id").toString();
                    revenue = new BigDecimal(tmp.get("ad_revenue").toString()).doubleValue();//广告单元总收益
                    impr = new BigDecimal(tmp.get("ad_impression").toString()).doubleValue();//广告单元 总展示次数
                    key = eventDate + "_" + countryCode + "_" + adUnitId;
                    num1 = impressionEventMap.get(key);//新安装 后面某日的 广告展示次数
                    num2 = impressionAllEventMap.get(key);//老用户 总广告展示次数
                    impressionEventMap.remove(key);//移除元素
                    impressionAllEventMap.remove(key);//移除元素
                    num1 = (null == num1 ? 0 : num1);
                    num2 = (null == num2 ? 0 : num2);
                    currentRevenue = 0D;//为每个广告单元的收益
                    if(num2 > 0){
                        currentRevenue = revenue * num1 / num2 ; //广告单元维度： 收益 * 新安装存留用户展示 / 所有老用户展示
                    }
                    revenueData = eventRevenueMap.get(eventDate);//活跃用户 只按照date分组，此处需将满足该date的数据 求和计算
                    if(null == revenueData){
                        revenueData = new RevenueData();
                    }
                    revenueData.nowRevenue += currentRevenue;//为 各个广告单元 在event_date的收益（收益为通过 广告比例换算的） 总和
                    revenueData.revenue += revenue;//为 各个广告单元 在event_date的展示收益 总和
                    revenueData.impression += impr;//为 各个广告单元 在event_date的展示次数 总和

                    eventRevenueMap.put(eventDate,revenueData);//key为日期，
                }
            }
            eventRevenueList.clear(); eventRevenueList = null;
            impressionEventMap.clear();impressionEventMap = null;impressionAllEventMap.clear();impressionAllEventMap=null;

            List<JSObject> activeList = DB.findListBySql(activeSql);//活跃用户
            List<JSObject> impressionsList = DB.findListBySql(impressionsSql);//展示次数（firebase不区分新老用户）
            //List<JSObject> impressionsUnionList = DB.findListBySql(impressionsUnionSql);//firebase展示次数（区分新老用户）


            String activeUserFirstEventDate = date;
            if(activeList.size() > 0 && activeList.get(0).hasObjectData()){
                activeUserFirstEventDate = activeList.get(0).get("event_date").toString();//取活跃用户第一天的展示日期
            }
            double firstImpressions = 0;//首日展示次数
            double impressions = 0D;double sumImpressions = 0D;//累计展示次数
            Map<String,List> impressionMap = new HashMap<String,List>();
            List<Double> impressionList = null;JSObject impressionsJS = null;
            for (int i = 0,len = impressionsList.size();i < len;i++) {
                impressionsJS = impressionsList.get(i);
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
            impressionsList.clear();impressionsList=null;
            /*
            //广告单元展示次数：区分新老用户的 广告展示次数，首日为新安装用户广告单元展示次数，其他日期为所有老用户广告单元展示次数
            Map<String,Double> impressionUnionMap = new HashMap<String,Double>();
            for (int i = 0,len = impressionsUnionList.size();i < len;i++) {//区分新老用户的 广告展示次数
                impressionsJS = impressionsUnionList.get(i);
                if (impressionsJS.hasObjectData()) {
                    eventDate = impressionsJS.get("event_date").toString();//event_date
                    impressions = new BigDecimal(impressionsJS.get("impressions").toString()).doubleValue();//展示次数
                    impressionUnionMap.put(eventDate,impressions);
                }
            }
            impressionsUnionList.clear();impressionsUnionList=null;
            */


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
            activeList.clear();activeList = null;

            JsonArray dataArray = new JsonArray();
            JsonArray item = null;
            Double sumRevenue = 0D;//累计活跃用户 收入，即自安装日至当日的活跃用户数 收益总和
            Double nowImpression;//当日 首日安装的用户，在后面 某日老用户 广告展示次数
            Double nowEcpm,ecpm;//firebase当日 ecpm，变现ecpm
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
                    item.add(impressionList.get(0));//广告展示次数 firebase
                    //item.add(impressionList.get(1));//累计展示次数 firebase
                    //item.add(impressionList.get(2));//首日展示占比% firebase
                    item.add(impressionList.get(3));//人均广告展示次数=累计的总的广告展示次数/firebase安装量
                }else {
                    item.add("-");//广告展示次数 firebase
                    //item.add("-");//累计展示次数 firebase
                    //item.add("-");//首日展示占比% firebase
                    item.add("-");//人均广告展示次数 firebase
                }

                //nowImpression = impressionUnionMap.get(eventDate);//新用户/老用户 firebase广告单元展示次数
                //nowImpression = null != nowImpression ? nowImpression : 0D;//firebase

                revenueData = eventRevenueMap.get(eventDate);//当日收入
                if(null == revenueData){
                    revenueData = new RevenueData();
                }
                nowRevenue = revenueData.nowRevenue;
                nowRevenue = (null == nowRevenue) ? 0D : nowRevenue;
                sumRevenue += nowRevenue;//累计收入

                //nowEcpm = nowImpression > 0 ? (revenueData.revenue / nowImpression * 1000) : 0;//当日ecpm = 当日总收益/firebase当日广告展示次数
                ecpm = revenueData.impression > 0 ? (revenueData.revenue / revenueData.impression * 1000) : 0;//变现ecpm


                item.add(Utils.trimDouble(ecpm));//adplatform当日ecpm = 当日变现收益 / 变现展示数
                //item.add(Utils.trimDouble(nowEcpm));//firebase当日ecpm = 当日收益 / firebase展示数
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

            eventRevenueMap.clear();eventRevenueMap=null;
            //impressionUnionMap.clear();impressionUnionMap=null;
            activeMap.clear();activeMap=null;

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

    /**
     * 广告收益数据存储
     */
    class RevenueData{
        Double nowRevenue = 0D;//当日收益（按照 广告展示次数换算后）
        Double revenue = 0D;//统计的 收益（变现）
        Double impression = 0D;//统计的 广告展示次数（变现）
    }
}
