package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.bean.GameRoundMonitorMetrics;
import com.bestgo.adsmoney.utils.NumberUtil;
import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.adsmoney.bean.AppMonitorMetrics;
import com.bestgo.common.database.services.DB;
import com.bestgo.common.database.utils.JSObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(name = "GameTrend", urlPatterns = {"/game_trend/*"})
public class GameTrend extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        //特殊用户标记
        HttpSession session = request.getSession();
        boolean isvisitor = session.getAttribute("isvisitor") == null ? false : true;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            //get对应的是折线图，query对应的是table
            if (path.startsWith("/query") || path.equals("/get")) {
                HashMap<Date, GameRoundMonitorMetrics> metricsMap = new HashMap<>();
                ArrayList<GameRoundMonitorMetrics> tmpDataList = new ArrayList<>();
                ArrayList<GameRoundMonitorMetrics> resultList = new ArrayList<>();

                int period = Utils.parseInt(request.getParameter("period"), 1);
                Date start = null;
                Date end = null;
                String endDate = request.getParameter("end_date");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    end = sdf.parse(endDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(end);
                    if (period == 3) {
                        cal.add(Calendar.MONTH, -2);
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                    } else {
                        cal.add(Calendar.DAY_OF_MONTH, -90);
                    }
                    start = cal.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String startDate = sdf.format(start);
                int index = Utils.parseInt(request.getParameter("page_index"), 0);
                int size = Utils.parseInt(request.getParameter("page_size"), 20);
                String filter = request.getParameter("filter");
                String filterCountry = request.getParameter("filterCountry");

                if (filter == null || filter.isEmpty()) {
                    filter = "";
                }
                if (filterCountry == null || filterCountry.isEmpty()) {
                    filterCountry = "";
                }

                //为特殊用户指定 特定应用
                if ("".equals(filter) && isvisitor){
                    filter = "com.solitaire.free.lj1," +
                            "com.collection.card.free," +
                            "com.ancient_card.free," +
                            "com.pyramid_card.free," +
                            "com.solitaire_star.card.free";
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
                String appIdPart = null;
                if (appIds.size() > 0) {
                    appIdPart = " AND (";
                    for (int i = 0,len = appIds.size(); i < len; i++) {
                        if (i < len - 1) {
                            appIdPart += " app_id = '" + appIds.get(i) + "' OR ";
                        } else {
                            appIdPart += " app_id = '" + appIds.get(i) + "' ";
                        }
                    }
                    appIdPart += ") ";
                }
                String countryCodePart = null;
                if (countryCodes.size() > 0) {
                    countryCodePart = " AND (";
                    for (int i = 0,len = countryCodes.size(); i < len; i++) {
                        if (i < len - 1) {
                            countryCodePart += " country_code = '" + countryCodes.get(i) + "' OR ";
                        } else {
                            countryCodePart += " country_code = '" + countryCodes.get(i) + "' ";
                        }
                    }
                    countryCodePart += ") ";
                }

                try {
                    GameRoundMonitorMetrics grMonitorMetrics = null;

                    //app_daily_metrics_history表得到安装日期应用国家维度的总收入，总展示
                    String sql = "select date, sum(round_start_active_user_count) as rs_aucount,sum(round_win_active_user_count) as rw_aucount, \n" +
                            "sum(round_start_freash_user_count) as rs_fucount , sum(round_win_freash_user_count) as rw_fucount , \n" +
                            "sum(freash_user_round_start_count) as fu_rsecount , sum(freash_user_round_win_count) as fu_rwecount \n" +
                            "from game_app_action_history\n" +
                            "where date between '" + startDate + "' and '" + endDate + "' \n" +
                            (appIds.size() > 0 ?  appIdPart : "") +
                            (countryCodes.size() > 0 ?  countryCodePart : "") +
                            "GROUP BY date\n" ;

                    List<JSObject> list = DB.findListBySql(sql);
                    JSObject jsObject = null;
                    for (int i = 0; i < list.size(); i++) {
                        jsObject = list.get(i);
                        Date date = jsObject.get("date");
                        long rs_aucount = Utils.convertLong(jsObject.get("rs_aucount"), 0);
                        long rw_aucount = Utils.convertLong(jsObject.get("rw_aucount"), 0);
                        long rs_fucount = Utils.convertLong(jsObject.get("rs_fucount"), 0);
                        long rw_fucount = Utils.convertLong(jsObject.get("rw_fucount"), 0);
                        long fu_rsecount = Utils.convertLong(jsObject.get("fu_rsecount"), 0);
                        long fu_rwecount = Utils.convertLong(jsObject.get("fu_rwecount"), 0);

//                        double sumRevenue = Utils.trimDouble(Utils.convertDouble(jsObject.get("sum_ad_revenue"), 0)); //累计收入
//                        long impression = Utils.convertLong(jsObject.get("ad_impression"), 0);
                        grMonitorMetrics = metricsMap.get(date);
                        if (grMonitorMetrics == null) {
                            grMonitorMetrics = new GameRoundMonitorMetrics();
                            metricsMap.put(date, grMonitorMetrics);
                            tmpDataList.add(grMonitorMetrics);
                        }
                        grMonitorMetrics.date = date;
                        grMonitorMetrics.roundStartActiveUserCount = rs_aucount;
                        grMonitorMetrics.roundWinActiveUserCount = rw_aucount;
                        grMonitorMetrics.roundStartFreashUserCount = rs_fucount;
                        grMonitorMetrics.roundWinFreashUserCount = rw_fucount;
                        grMonitorMetrics.freashUserRoundStartCount = fu_rsecount;
                        grMonitorMetrics.freashUserRoundWinCount = fu_rwecount;
                        grMonitorMetrics.avgSumFreasherRoundStart =rs_fucount > 0 ?  fu_rsecount/(float)rs_fucount:0f;
                        grMonitorMetrics.avgSumFreasherRoundWin = rw_fucount > 0 ?  fu_rwecount/(float)rw_fucount:0f;

                    }



                    metricsMap.clear();
                    Collections.sort(tmpDataList, new Comparator<GameRoundMonitorMetrics>() {
                        @Override
                        public int compare(GameRoundMonitorMetrics o1, GameRoundMonitorMetrics o2) {
                            int ret = o1.date.compareTo(o2.date);
                            if (ret > 0) {
                                return -1;
                            } else if (ret == 0) {
                                return 0;
                            } else {
                                return 1;
                            }
                        }
                    });
                    int remainder = 1;
                    switch (period) {
                        case 2:
                            remainder = 7;
                            break;
                        case 3:
                            remainder = 30;
                            break;
                    }
                    grMonitorMetrics = new GameRoundMonitorMetrics();
                    double lastARPU = -1;
                    int lastMonth = -1;
                    for (int i = 0; i < tmpDataList.size(); i++) {
                        if (remainder == 30) {
                            if (lastMonth != tmpDataList.get(i).date.getMonth()) {
                                grMonitorMetrics = new GameRoundMonitorMetrics();
                                grMonitorMetrics.date = tmpDataList.get(i).date;
                                grMonitorMetrics.date.setDate(1);
                                resultList.add(grMonitorMetrics);
                                lastMonth = grMonitorMetrics.date.getMonth();
                            }
                        } else {
                            if (i % remainder == 0) {
                                grMonitorMetrics = new GameRoundMonitorMetrics();
                                grMonitorMetrics.date = tmpDataList.get(i).date;
                                resultList.add(grMonitorMetrics);
                            }
                        }
                        grMonitorMetrics.freashUserRoundStartCount += tmpDataList.get(i).freashUserRoundStartCount;
                        grMonitorMetrics.freashUserRoundWinCount += tmpDataList.get(i).freashUserRoundWinCount;
                        grMonitorMetrics.roundStartActiveUserCount += tmpDataList.get(i).roundStartActiveUserCount;
                        grMonitorMetrics.roundWinActiveUserCount += tmpDataList.get(i).roundWinActiveUserCount;
                        grMonitorMetrics.roundStartFreashUserCount += tmpDataList.get(i).roundStartFreashUserCount;
                        grMonitorMetrics.roundWinFreashUserCount += tmpDataList.get(i).roundWinFreashUserCount;

                        grMonitorMetrics.avgSumFreasherRoundStart += tmpDataList.get(i).avgSumFreasherRoundStart;
                        grMonitorMetrics.avgSumFreasherRoundWin += tmpDataList.get(i).avgSumFreasherRoundWin;


                    }

                    for (int i = 0; i < resultList.size(); i++) {
                        if (i != resultList.size() - 1) {
                            grMonitorMetrics = resultList.get(i);
                            GameRoundMonitorMetrics two = resultList.get(i + 1);
                            if (two.roundStartActiveUserCount > 0) {
                                grMonitorMetrics.roundStartActiveUserTrend = Utils.trimFloat((grMonitorMetrics.roundStartActiveUserCount - two.roundStartActiveUserCount) * 1.0f / two.roundStartActiveUserCount);
                            }
                            if (two.roundWinActiveUserCount > 0) {
                                grMonitorMetrics.roundWinActiveUserTrend = Utils.trimFloat((grMonitorMetrics.roundWinActiveUserCount - two.roundWinActiveUserCount) * 1.0f / two.roundWinActiveUserCount);
                            }

                            if (two.roundStartFreashUserCount > 0) {
                                grMonitorMetrics.roundStartFreashUserTrend = Utils.trimFloat((grMonitorMetrics.roundStartFreashUserCount - two.roundStartFreashUserCount) * 1.0f / two.roundStartFreashUserCount);
                            }

                            if (two.roundWinFreashUserCount > 0) {
                                grMonitorMetrics.roundWinFreashUserTrend = Utils.trimFloat((grMonitorMetrics.roundWinFreashUserCount - two.roundWinFreashUserCount) * 1.0f / two.roundWinFreashUserCount);
                            }

                            if (two.freashUserRoundWinCount > 0) {
                                grMonitorMetrics.freashUserRoundWinTrend = Utils.trimFloat((grMonitorMetrics.freashUserRoundWinCount - two.freashUserRoundWinCount) * 1.0f / two.freashUserRoundWinCount);
                            }

                            if (two.freashUserRoundStartCount > 0) {
                                grMonitorMetrics.freashUserRoundStartTrend = Utils.trimFloat((grMonitorMetrics.freashUserRoundStartCount - two.freashUserRoundStartCount) * 1.0f / two.freashUserRoundStartCount);
                            }


                        }
                    }
                    JsonArray array = new JsonArray();
                    if (path.equals("/get")) {
                        index = 0;
                        size = resultList.size();
                    }
                    for (int i = index * size; i < resultList.size() && i < (index * size + size); i++) {
                        grMonitorMetrics = resultList.get(i);
                        JsonObject jsonObject = new JsonObject();
                        if (path.equals("/get")) {
                            jsonObject.addProperty("date", grMonitorMetrics.date.getTime());
                        } else {
                            jsonObject.addProperty("date", grMonitorMetrics.date.toString());
                        }
                        jsonObject.addProperty("roundStartActiveUserCount", grMonitorMetrics.roundStartActiveUserCount);
                        jsonObject.addProperty("roundStartActiveUserTrend", NumberUtil.trimDouble(grMonitorMetrics.roundStartActiveUserTrend,3));
                        jsonObject.addProperty("roundWinActiveUserCount", grMonitorMetrics.roundWinActiveUserCount);
                        jsonObject.addProperty("roundWinActiveUserTrend", NumberUtil.trimDouble(grMonitorMetrics.roundWinActiveUserTrend,3));
                        jsonObject.addProperty("roundStartFreashUserCount", grMonitorMetrics.roundStartFreashUserCount);
                        jsonObject.addProperty("roundStartFreashUserTrend", NumberUtil.trimDouble(grMonitorMetrics.roundStartFreashUserTrend,3));
                        jsonObject.addProperty("roundWinFreashUserCount",grMonitorMetrics.roundWinFreashUserCount);
                        jsonObject.addProperty("roundWinFreashUserTrend", NumberUtil.trimDouble(grMonitorMetrics.roundWinFreashUserTrend,3));

                        jsonObject.addProperty("freashUserRoundStartCount",grMonitorMetrics.freashUserRoundStartCount);
                        jsonObject.addProperty("freashUserRoundStartTrend", NumberUtil.trimDouble(grMonitorMetrics.freashUserRoundStartTrend,3));

                        jsonObject.addProperty("freashUserRoundWinCount",grMonitorMetrics.freashUserRoundWinCount);
                        jsonObject.addProperty("freashUserRoundWinTrend", NumberUtil.trimDouble(grMonitorMetrics.freashUserRoundWinTrend,3));


                        jsonObject.addProperty("avgSumFreasherRoundStart", NumberUtil.trimDouble(grMonitorMetrics.avgSumFreasherRoundStart,3));
                        jsonObject.addProperty("avgSumFreasherRoundWin", NumberUtil.trimDouble(grMonitorMetrics.avgSumFreasherRoundWin,3));


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

    /**
     * 获取最近的ARPU
     * @param date
     * @param appIds
     * @param countryCodes
     * @return
     */
    private double fetchNearbyARPU(String date, ArrayList<String> appIds, ArrayList<String> countryCodes) {
        try {
            String sqlPart = "";
            if (appIds.size() > 0) {
                String ss = "";
                for (int i = 0; i < appIds.size(); i++) {
                    if (i < appIds.size() - 1) {
                        ss += "'" + appIds.get(i) + "',";
                    } else {
                        ss += "'" + appIds.get(i) + "'";
                    }
                }
                sqlPart += " and app_id in (" + ss + ")";
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
                sqlPart += " and country_code in (" + ss + ")";
            }

            //在app_user_life_time_history表中找到它的最近日期
            String sql = "select max(install_date) as target_date from app_user_life_time_history where install_date<? " + sqlPart;
            JSObject one = DB.findOneBySql(sql, date);
            if (one.hasObjectData()) {
                Date targetDate = one.get("target_date");

                long activeCount = 0;
                //在app_user_life_time_history表中根据安装日期+最近日期，找到应用国家维度的总活跃数
                sql = "select sum(active_count) as active_count " +
                        "from app_user_life_time_history " +
                        "where install_date=? and active_date=? " + sqlPart;
                one = DB.findOneBySql(sql, targetDate, targetDate);
                if (one.hasObjectData()) {
                    activeCount = Utils.convertLong(one.get("active_count"), 0);
                }
                //在app_user_life_time_history表中根据安装日期，找到应用国家维度的总预估收入
                sql = "select sum(estimated_revenue) as estimated_revenue " +
                        "from app_user_life_time_history " +
                        "where install_date=? " + sqlPart;
                one = DB.findOneBySql(sql, targetDate);
                if (one.hasObjectData()) {
                    double estimatedRevenue = Utils.convertDouble(one.get("estimated_revenue"), 0);
                    //arpu=预估收入/活跃数
                    return estimatedRevenue / activeCount;
                }
            }
        } catch (Exception ex) {
        }
        return 0;
    }
}
