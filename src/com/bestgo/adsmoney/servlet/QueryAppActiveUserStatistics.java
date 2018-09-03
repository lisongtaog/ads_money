package com.bestgo.adsmoney.servlet;

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
import java.math.BigDecimal;
import java.util.*;

/**
 * @author mengjun
 * @date 2018/7/5 14:25
 * @desc 应用活跃用户统计
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
            //app最近那天的版本号查询，结果用&拼接
            String appVersionSql =
                    "SELECT DATE(create_time) AS publish_date,GROUP_CONCAT(version_number ORDER BY version_number SEPARATOR ' & ') AS app_version \n" +
                    " FROM app_version_number WHERE app_id = '"+appId+"' \n" +
                    " AND DATE(create_time) = (SELECT DATE(MAX(create_time)) from app_version_number WHERE DATE(create_time) <= '"+date+"') ";                    ;

            //购买安装的 花费、安装量
            String purchaseInstallSql = "SELECT SUM(spend) as purchase_cost,SUM(installed) as purchase_installed FROM app_ads_daily_metrics_history ";//购买安装量
            String allInstallSql = "SELECT SUM(installed) AS all_installed FROM app_firebase_daily_metrics_history ";//总安装量

            //首日 所有用户(含首日抽样用户、首日安装非抽样用户、老用户在当日的展示)收益、展示
//            String revenueSql = "SELECT sum(ad_revenue) as ad_revenue, sum(ad_impression) as ad_impression from app_ad_unit_metrics_history \n";
            //首日 抽样用户收益、展示
//            String sampleRevenueSql = "SELECT sum(ad_revenue) as sample_revenue, sum(ad_impression) as sample_impression from app_ad_unit_metrics_history \n"
//                    + " WHERE ad_unit_id IN (SELECT ad_unit_id from app_ad_unit_config WHERE flag = '1' AND app_id='"+appId+"') \n" ;
//            //抽样用户数量 样本sql
//            String sampleUserSql = "SELECT SUM(user_num_total) AS new_install_num,SUM(user_num_sample) AS sample_num FROM app_first_install_data \n";

            purchaseInstallSql += " where date ='"+date+"' ";
            allInstallSql += " where date ='"+date+"' ";
//            revenueSql += " where date ='"+date+"' ";
//            sampleRevenueSql += " AND date ='"+date+"' ";
//            sampleUserSql += " where date ='"+date+"' ";
            if(!"all".equals(appId) && !appId.isEmpty()){
                purchaseInstallSql += " and app_id ='"+appId+"' ";
                allInstallSql += " and app_id ='"+appId+"' ";
//                revenueSql += " and app_id ='"+appId+"' ";
//                sampleRevenueSql += " and app_id ='"+appId+"' ";
//                sampleUserSql += " and app_id ='"+appId+"' ";
            }
            if(!"all".equals(countryCode) && !countryCode.isEmpty()){
                purchaseInstallSql += " and country_code ='"+countryCode+"' ";
                allInstallSql += " and country_code ='"+countryCode+"' ";
//                revenueSql += " and country_code ='"+countryCode+"' ";
//                sampleRevenueSql += " and country_code ='"+countryCode+"' ";
//                sampleUserSql += " and country_code ='"+countryCode+"' ";
            }

            JSObject versionObj = DB.findOneBySql(appVersionSql);//app版本信息
            JSObject obj = DB.findOneBySql(purchaseInstallSql);//购买安装量
            JSObject obj2 = DB.findOneBySql(allInstallSql);//总安装量
//            JSObject revenueObj = DB.findOneBySql(revenueSql);//首日所有用户收益数据
//            JSObject sampleRevenueObj = DB.findOneBySql(sampleRevenueSql);//首日抽样用户收益数据
//            JSObject sampleUserObj = DB.findOneBySql(sampleUserSql);//首日新用户 样本数据

            JsonObject summary = new JsonObject();//表头 通用汇总信息
            double allInstalled = 0;
            String appVersion = "";
            try {
                double purchaseCost=0,purchaseInstalled=0;
//                double totalNewInstallNum=0,sampleInstallNum=0;//首日新安装总用户数；首日新安装抽样用户数
//                double sampleRenenue=0,sampleImpression = 0,sampleAvgImpression;//抽样用户：收益、 展示数、抽样用户平均展示
//                double firstTotalRevenue=0,firstTotalImpression=0;//所有用户在首日的 收益、展示
//                double avgECPM = 0;//平均ECPM = 所有用户收益/所有用户展示 * 1000
                if(obj.get("purchase_installed") != null && !"".equals(obj.get("purchase_installed"))){
                    //purchaseInstalled = Utils.parseInt(obj.get("purchase_installed"),0);
                    purchaseCost = Utils.trimDouble(new BigDecimal(obj.get("purchase_cost").toString()).doubleValue());
                    purchaseInstalled = new BigDecimal(obj.get("purchase_installed").toString()).doubleValue();
                }
                if(obj2.get("all_installed") != null && !"".equals(obj2.get("all_installed"))){
                    //allInstalled = Utils.parseInt(obj.get("all_installed"),0);
                    allInstalled = new BigDecimal(obj2.get("all_installed").toString()).doubleValue();
                }
                //首日新安装总用户数
//                if(null != sampleUserObj.get("new_install_num") && !"".equals(sampleUserObj.get("new_install_num"))){
//                    totalNewInstallNum = Utils.trimDouble(new BigDecimal(sampleUserObj.get("new_install_num").toString()).doubleValue());
//                }
//                //首日新安装抽样用户数
//                if(null != sampleUserObj.get("sample_num") && !"".equals(sampleUserObj.get("sample_num"))){
//                    sampleInstallNum = Utils.trimDouble(new BigDecimal(sampleUserObj.get("sample_num").toString()).doubleValue());
//                }
//                //抽样用户收益
//                if(null != sampleRevenueObj.get("sample_revenue") && !"".equals(sampleRevenueObj.get("sample_revenue"))){
//                    sampleRenenue = Utils.trimDouble(new BigDecimal(sampleRevenueObj.get("sample_revenue").toString()).doubleValue());
//                }
//                //抽样用户展示
//                if(null != sampleRevenueObj.get("sample_impression") && !"".equals(sampleRevenueObj.get("sample_impression"))){
//                    sampleImpression = new BigDecimal(sampleRevenueObj.get("sample_impression").toString()).doubleValue();
//                }
                //抽样用户 平均展示
//                sampleAvgImpression = sampleInstallNum > 0 ? sampleImpression / sampleInstallNum : 0 ;
//
//                //根据抽样用户 估算：首日所有新用户 广告展示数（抽样展示 * 总新安装用户 / 抽样用户数）
//                firstImpression = sampleInstallNum > 0 ? sampleImpression * totalNewInstallNum / sampleInstallNum : 0 ;
//                firstImpression = Utils.trimDouble(firstImpression);
                //当日ecpm 取：抽样用户ecpm
                //firstEcpm = sampleImpression > 0 ? sampleRenenue / sampleImpression * 1000 : 0;
                //firstEcpm = Utils.trimDouble(firstEcpm);

                //首日所有用户的收益
//                if(null != revenueObj.get("ad_revenue") && !"".equals(revenueObj.get("ad_revenue"))){
//                    firstTotalRevenue = Utils.trimDouble(new BigDecimal(revenueObj.get("ad_revenue").toString()).doubleValue());
//                }
//                //首日所有用户的展示
//                if(null != revenueObj.get("ad_impression") && !"".equals(revenueObj.get("ad_impression"))){
//                    firstTotalImpression = new BigDecimal(revenueObj.get("ad_impression").toString()).doubleValue();
//                }
//                avgECPM = firstTotalImpression > 0 ? firstTotalRevenue / firstTotalImpression * 1000 : 0;
                //当日ecpm 取：平均ECPM
//                firstEcpm = Utils.trimDouble(avgECPM);
                //首日收入方案1：firstRevenue = totalNewInstallNum * sampleAvgImpression * avgECPM / 1000 ; //或
                //firstRevenue = sampleInstallNum > 0 ? totalNewInstallNum * avgECPM * sampleImpression / (1000 * sampleInstallNum) : 0 ;
                //首日收入方案2：抽样用户收入 +  首日非抽样新用户数 * 抽样用户平均展示 * 平均ECPM /1000
//                firstRevenue = sampleRenenue + (totalNewInstallNum - sampleInstallNum) * sampleAvgImpression * avgECPM / 1000 ;
//                firstRevenue = Utils.trimDouble(firstRevenue);

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
//                summary.addProperty("firstImpression",firstImpression);//首日展示次数
//                summary.addProperty("firstRevenue",firstRevenue);//首日收益
//                summary.addProperty("firstEcpm",firstEcpm);//首日ecpm = 首日收益/首日展示次数 * 1000 ；分子、分母均为money变现数据

            }catch (Exception e){
                e.printStackTrace();
            }
            rtnJson.add("summary",summary);//汇总表头


            String subGroupBy = ""; String country_query = "";
            StringBuffer subCondition = new StringBuffer();//拼接 appId，country_code查询条件
            if( null != appId && !appId.isEmpty() && !"all".equals(appId) ){//app_id是必录项
                subCondition.append(" AND app_id = '").append(appId).append( "' ");
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

//            String adUnitCondition = " AND ad_unit_id IN (SELECT ad_unit_id from app_ad_unit_config WHERE flag = '0' AND app_id='"+appId+"') \n";
            //首日安装用户 在后续日期某一天的 展示次数（非新用户广告单元）
            String imressionEvent = "SELECT event_date"+country_query+",ad_unit_id,SUM(impressions) AS impressions " +
                    "FROM app_ads_impressions_statistics WHERE 1=1 "+ subCondition.toString() +
                    "AND installed_date = '"+ date +"' AND event_date >= '" + date + "' \n"
                    +"GROUP BY event_date" + subGroupBy + ",ad_unit_id \n" +
                    "ORDER BY event_date ASC";
            //所有安装用户(安装日期后30天内) 在后续日期某一天的 展示次数（非新用户广告单元）
            String imressionAllEvent = "SELECT event_date"+country_query+",ad_unit_id,SUM(impressions) AS impressions " +
                    "FROM app_ads_impressions_statistics WHERE 1=1 " + subCondition.toString() +
                    "AND event_date >= installed_date AND event_date >= '"+date+"' \n" //含 展示日期当天的，与收益（当天的）匹配
                    + "GROUP BY event_date" + subGroupBy + ",ad_unit_id \n" +
                    "ORDER BY event_date ASC";

            //变现数据，大于安装日期的 所有老用户的收益（广告单元flag为0）
            String eventRevenueSql = "SELECT date"+country_query+",ad_unit_id,SUM(ad_revenue) AS ad_revenue,SUM(ad_impression) AS ad_impression "
                    + "from app_ad_unit_metrics_history WHERE 1=1 "+ subCondition.toString() +
                    " AND date < DATE(NOW()) AND date >= '"+date+"' \n"//含 展示日期当天的 90%老广告单元，与收益（当天的）匹配
                    + "GROUP BY date" + subGroupBy + ",ad_unit_id \n" +
                    "ORDER BY date ASC";

            Map<String,Double> impressionEventMap = new HashMap<String,Double>();//新用户在后续每日的 广告展示次数
            Map<String,Double> impressionAllEventMap = new HashMap<String,Double>();//所有用户的 广告展示次数
            Map<String,RevenueData> eventRevenueMap = new LinkedHashMap<String,RevenueData>();//收益-列表(用于存取当日收入)

            List<JSObject> impressionEventList = DB.findListBySql(imressionEvent);//首日安装用户 在后续日期某一天的 展示次数（非新用户广告单元）
            List<JSObject> impressionAllEventList = DB.findListBySql(imressionAllEvent);//所有安装用户(安装日期后30天内) 在后续日期某一天的 展示次数（非新用户广告单元）
            List<JSObject> eventRevenueList = DB.findListBySql(eventRevenueSql);//变现数据，大于安装日期的 所有老用户的收益（广告单元flag为0）

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

            RevenueData revenueData = null;//首日的

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
            if (activeList.size() > 0 && activeList.get(0).hasObjectData()) {
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
                //ecpm = revenueData.impression > 0 ? (revenueData.revenue / revenueData.impression * 1000) : 0;//变现ecpm
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
