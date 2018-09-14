package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.OperationResult;
import com.bestgo.adsmoney.bean.AppData;
import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.adsmoney.bean.AppAdUnitConfig;
import com.bestgo.common.database.services.DB;
import com.bestgo.common.database.utils.JSObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jikai on 5/31/17.
 * 广告单元管理
 */
@WebServlet(name = "AdUnitManagement", urlPatterns = {"/ad_unit_management/*"})
public class AdUnitManagement extends HttpServlet {
    private static final String[] FIELDS = {"id", "app_id", "ad_network", "ad_unit_type", "ad_unit_id", "show_type","flag", "ad_unit_name", "admob_account"};

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/create")) {
                AppAdUnitConfig adUnitConfig = new AppAdUnitConfig();
                adUnitConfig.appId = request.getParameter("app_id");
                adUnitConfig.adNetwork = request.getParameter("ad_network");
                adUnitConfig.adUnitType = request.getParameter("ad_unit_type");
                adUnitConfig.adUnitId = request.getParameter("ad_unit_id");
                adUnitConfig.showType = request.getParameter("show_type");
                adUnitConfig.flag = request.getParameter("flag");
                adUnitConfig.adUnitName = request.getParameter("ad_unit_name");
                adUnitConfig.admobAccount = request.getParameter("admob_account");

                OperationResult result = createNewAdUnit(adUnitConfig);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
                json.add("data", result.data);
            } else if (path.startsWith("/delete")) {
                String id = request.getParameter("id");
                OperationResult result = deleteAdUnit(id);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
            } else if (path.startsWith("/update")) {
                AppAdUnitConfig adUnitConfig = new AppAdUnitConfig();
                adUnitConfig.appId = request.getParameter("app_id");
                adUnitConfig.adNetwork = request.getParameter("ad_network");
                adUnitConfig.adUnitType = request.getParameter("ad_unit_type");
                adUnitConfig.adUnitId = request.getParameter("ad_unit_id");
                adUnitConfig.showType = request.getParameter("show_type");
                adUnitConfig.flag = request.getParameter("flag");
                adUnitConfig.adUnitName = request.getParameter("ad_unit_name");
                adUnitConfig.admobAccount = request.getParameter("admob_account");

                adUnitConfig.id = Utils.parseInt(request.getParameter("id"), 0);
                OperationResult result = updateAdUnit(adUnitConfig);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
                json.add("data", result.data);
            } else if (path.startsWith("/query")) {
                String word = request.getParameter("word");
                if (word != null) {
                    List<JSObject> data = fetchData(word);
                    json.addProperty("ret", 1);
                    JsonArray array = new JsonArray();
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject one = new JsonObject();
                        for (int ii = 0; ii < FIELDS.length; ii++) {
                            one.addProperty(FIELDS[ii], data.get(i).get(FIELDS[ii]).toString());
                        }
                        array.add(one);
                    }
                    json.addProperty("total", 0);
                    json.add("data", array);
                } else {
                    long count = count();
                    int index = Utils.parseInt(request.getParameter("page_index"), 0);
                    int size = Utils.parseInt(request.getParameter("page_size"), 20);
                    List<JSObject> data = fetchData(index, size);
                    json.addProperty("ret", 1);
                    JsonArray array = new JsonArray();
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject one = new JsonObject();
                        for (int ii = 0; ii < FIELDS.length; ii++) {
                            one.addProperty(FIELDS[ii], data.get(i).get(FIELDS[ii]).toString());
                        }
                        array.add(one);
                    }
                    json.addProperty("total", count);
                    json.add("data", array);
                }
            }
        } else {

        }

        response.getWriter().write(json.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * 在Search输入框，搜索时
     * @param word
     * @return
     */
    public static List<JSObject> fetchData(String word) {
        List<JSObject> list = new ArrayList<>();
        try {
            String sql = "select app_ad_unit_config.id, app_ad_unit_config.app_id, app_ad_unit_config.ad_network, app_ad_unit_config.ad_unit_type,show_type," +
                    "app_ad_unit_config.ad_unit_id, app_ad_unit_config.flag, app_ad_unit_config.ad_unit_name, app_ad_unit_config.admob_account " +
                    "from app_ad_unit_config, app_data " +
                    "where app_ad_unit_config.app_id=app_data.app_id and (app_name like " + "'%" + word + "%' or app_ad_unit_config.app_id like " + "'%" + word + "%'" +") "+
                    " order by id ASC,app_id ASC,flag ASC";
            return DB.findListBySql(sql);
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return list;
    }

    public static List<JSObject> fetchData(int index, int size) {
        List<JSObject> list = new ArrayList<>();
        try {
            String[] orders = {"id","app_id","flag"};
            return DB.scan("app_ad_unit_config").select(FIELDS).orderByAsc(orders).limit(size).start(index * size).execute();
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return list;
    }

    public static long count() {
        try {
            JSObject object = DB.simpleScan("app_ad_unit_config").select(DB.func(DB.COUNT, "id")).execute();
            return object.get("count(id)");
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return 0;
    }

    private OperationResult deleteAdUnit(String id) {
        OperationResult ret = new OperationResult();

        try {
            DB.delete("app_ad_unit_config").where(DB.filter().whereEqualTo("id", id)).execute();

            ret.result = true;
            ret.message = "执行成功";
        } catch (Exception e) {
            ret.result = false;
            ret.message = e.getMessage();
            Logger logger = Logger.getRootLogger();
            logger.error(e.getMessage(), e);
        }

        return ret;
    }

    /**
     * 创建新的广告单元
     * @param adUnitConfig
     * @return
     */
    private OperationResult createNewAdUnit(AppAdUnitConfig adUnitConfig) {
        OperationResult ret = new OperationResult();

        try {
            JSObject one = DB.simpleScan("app_ad_unit_config").select("id").where(DB.filter().whereEqualTo("ad_unit_id", adUnitConfig.adUnitId)).execute();
            if (one.get("id") != null) {
                ret.result = false;
                ret.message = "已经存在这个这个广告单元了";
            } else {
                //ad_network:AdMob/Facebook
                //ad_unit_type:banner/interstitial/native
                //flag:0/1
                DB.insert("app_ad_unit_config")
                        .put("app_id", adUnitConfig.appId)
                        .put("ad_network", adUnitConfig.adNetwork)
                        .put("ad_unit_type", adUnitConfig.adUnitType)
                        .put("ad_unit_id", adUnitConfig.adUnitId)
                        .put("show_type", adUnitConfig.showType)
                        .put("flag", adUnitConfig.flag)
                        .put("ad_unit_name", adUnitConfig.adUnitName)
                        .put("admob_account", adUnitConfig.admobAccount)
                        .execute();

                one = DB.simpleScan("app_ad_unit_config").select("id").where(DB.filter().whereEqualTo("ad_unit_id", adUnitConfig.adUnitId)).execute();
                if (one.hasObjectData()) {
                    ret.result = true;
                    ret.message = "创建成功";
                    ret.data = new JsonObject();
                    ret.data.addProperty("id", (long)one.get("id"));
                    ret.data.addProperty("app_id", adUnitConfig.appId);
                    ret.data.addProperty("ad_network", adUnitConfig.adNetwork);
                    ret.data.addProperty("ad_unit_type", adUnitConfig.adUnitType);
                    ret.data.addProperty("ad_unit_id", adUnitConfig.adUnitId);
                    ret.data.addProperty("show_type", adUnitConfig.showType);
                    ret.data.addProperty("flag", adUnitConfig.flag);
                    ret.data.addProperty("ad_unit_name", adUnitConfig.adUnitName);
                    ret.data.addProperty("admob_account", adUnitConfig.admobAccount);
                }
            }
        } catch (Exception e) {
            ret.result = false;
            ret.message = e.getMessage();
            Logger logger = Logger.getRootLogger();
            logger.error(e.getMessage(), e);
        }

        return ret;
    }

    private OperationResult updateAdUnit(AppAdUnitConfig adUnitConfig) {
        OperationResult ret = new OperationResult();

        try {
            DB.update("app_ad_unit_config")
                    .put("app_id", adUnitConfig.appId)
                    .put("ad_network", adUnitConfig.adNetwork)
                    .put("ad_unit_type", adUnitConfig.adUnitType)
                    .put("ad_unit_id", adUnitConfig.adUnitId)
                    .put("show_type", adUnitConfig.showType)
                    .put("flag", adUnitConfig.flag)
                    .put("ad_unit_name", adUnitConfig.adUnitName)
                    .put("admob_account", adUnitConfig.admobAccount)
                    .where(DB.filter().whereEqualTo("id", adUnitConfig.id))
                    .execute();

            JSObject one = DB.simpleScan("app_ad_unit_config").select("id").where(DB.filter().whereEqualTo("ad_unit_id", adUnitConfig.adUnitId)).execute();
            if (one.hasObjectData()) {
                ret.result = true;
                ret.message = "修改成功";
                ret.data = new JsonObject();
                ret.data.addProperty("id", (long)one.get("id"));
                ret.data.addProperty("app_id", adUnitConfig.appId);
                ret.data.addProperty("ad_network", adUnitConfig.adNetwork);
                ret.data.addProperty("ad_unit_type", adUnitConfig.adUnitType);
                ret.data.addProperty("ad_unit_id", adUnitConfig.adUnitId);
                ret.data.addProperty("show_type", adUnitConfig.showType);
                ret.data.addProperty("flag", adUnitConfig.flag);
                ret.data.addProperty("ad_unit_name", adUnitConfig.adUnitName);
                ret.data.addProperty("admob_account", adUnitConfig.admobAccount);
            }
        } catch (Exception e) {
            ret.result = false;
            ret.message = e.getMessage();
            Logger logger = Logger.getRootLogger();
            logger.error(e.getMessage(), e);
        }

        return ret;
    }

    public static Map<String,String> fetchAllUnitName() {
        Map<String,String> rtnMap = new HashMap<String,String>();
        try {
            String sql = "select DISTINCT ad_unit_id,ad_unit_name FROM app_ad_unit_config";
            List<JSObject> result = DB.findListBySql(sql);
            String adUnitId = null;
            String adUnitName = null;
            for (int i = 0; i < result.size(); i++) {
                adUnitId = result.get(i).get("ad_unit_id");
                adUnitName = result.get(i).get("ad_unit_name");
                rtnMap.put(adUnitId,adUnitName);
            }
        } catch (Exception ex) {
        }
        return rtnMap;
    }
    public static Map<String,String> fetchAdUnitIdAdUnitTypeMap() {
        Map<String,String> rtnMap = new HashMap<String,String>();
        try {
            String sql = "select DISTINCT ad_unit_id,ad_unit_type FROM app_ad_unit_config";
            List<JSObject> result = DB.findListBySql(sql);
            String adUnitId = null;
            String adUnitType = null;
            for (int i = 0; i < result.size(); i++) {
                adUnitId = result.get(i).get("ad_unit_id");
                adUnitType = result.get(i).get("ad_unit_type");
                rtnMap.put(adUnitId,adUnitType);
            }
        } catch (Exception ex) {
        }
        return rtnMap;
    }
    public static Map<String,Integer> fetchAdUnitIdShowTypeMap() {
        Map<String,String> rtnMap = new HashMap<String,String>();
        try {
            String sql = "select DISTINCT ad_unit_id,show_type FROM app_ad_unit_config";
            List<JSObject> result = DB.findListBySql(sql);
            String adUnitId = null;
            Integer show_type = null;
            for (int i = 0; i < result.size(); i++) {
                adUnitId = result.get(i).get("ad_unit_id");
                show_type = result.get(i).get("show_type");
                rtnMap.put(adUnitId,show_type);
            }
        } catch (Exception ex) {
        }
        return rtnMap;
    }

    public static Map<String,String> fetchAppIdByUnit() {
        Map<String,String> rtnMap = new HashMap<String,String>();
        try {
            String sql = "select ad_unit_id,app_id FROM app_ad_unit_config";
            List<JSObject> result = DB.findListBySql(sql);
            String adUnitId = null;
            String appId = null;
            for (int i = 0; i < result.size(); i++) {
                adUnitId = result.get(i).get("ad_unit_id");
                appId = result.get(i).get("app_id");
                rtnMap.put(adUnitId,appId);
            }
        } catch (Exception ex) {
        }
        return rtnMap;
    }
}
