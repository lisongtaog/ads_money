package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.OperationResult;
import com.bestgo.adsmoney.bean.AppAdUnitConfig;
import com.bestgo.adsmoney.bean.AppVersionNumber;
import com.bestgo.adsmoney.utils.Utils;
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
import java.util.*;

/**
 * @author: mengjun
 * @date: 2018/8/06 20:52
 * @desc: 应用的版本号信息
 */
@WebServlet(name = "AppVersionNumberServlet", urlPatterns = {"/app_version_number/*"})
public class AppVersionNumberServlet extends HttpServlet {
    private static final String[] FIELDS = {"id", "app_id", "app_name", "create_time", "version_number", "description"};

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;
        Map<String, String> appIdAppNameMap = fetchAllAppIdAppName();
        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.matches("/create_app_version")) {
                AppVersionNumber appVersionNumber = new AppVersionNumber();
                appVersionNumber.setAppId(request.getParameter("app_id"));
                appVersionNumber.setAppName(appIdAppNameMap.get(appVersionNumber.getAppId()));
                appVersionNumber.setCreateTime(request.getParameter("create_time"));
                appVersionNumber.setVersionNumber(request.getParameter("version_number"));
                appVersionNumber.setDescription(request.getParameter("description"));
                OperationResult result = createAppVersion(appVersionNumber);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
                json.add("data", result.data);
            } else if (path.matches("/delete_app_version")) {
                String id = request.getParameter("id");
                OperationResult result = deleteAppVersion(id);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
            } else if (path.startsWith("/update_app_version")) {
                AppVersionNumber appVersionNumber = new AppVersionNumber();
                appVersionNumber.setAppId(request.getParameter("app_id"));
                appVersionNumber.setAppName(appIdAppNameMap.get(appVersionNumber.getAppId()));
                appVersionNumber.setCreateTime(request.getParameter("create_time"));
                appVersionNumber.setVersionNumber(request.getParameter("version_number"));
                appVersionNumber.setDescription(request.getParameter("description"));
                appVersionNumber.setId(Utils.parseInt(request.getParameter("id"), 0));
                OperationResult result = updateAppVersion(appVersionNumber);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
                json.add("data", result.data);
            } else if (path.startsWith("/query_app_version")) {
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
     * 抓取所有数据
     * @param appName 如果传参不为空，则根据应用名称筛选
     * @return
     */
    public static List<JSObject> fetchData(String appName) {
        List<JSObject> list = new ArrayList<>();
        try {
            String sql = "SELECT id,app_id,app_name,create_time,version_number,description FROM app_version_number";
            if (null != appName && !appName.isEmpty()) {
                sql = "SELECT id,app_id,app_name,create_time,version_number,description FROM app_version_number WHERE app_name LIKE '%" + appName + "%'";
            }
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
            String[] orders = {"id","app_id"};
            return DB.scan("app_version_number").select(FIELDS).orderByAsc(orders).limit(size).start(index * size).execute();
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return list;
    }

    public static long count() {
        try {
            JSObject object = DB.simpleScan("app_version_number").select(DB.func(DB.COUNT, "id")).execute();
            return object.get("count(id)");
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return 0;
    }

    /**
     * 移除应用版本号信息
     * @param id
     * @return
     */
    private OperationResult deleteAppVersion(String id) {
        OperationResult ret = new OperationResult();

        try {
            DB.delete("app_version_number").where(DB.filter().whereEqualTo("id", id)).execute();

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
     * 创建新的应用版本号信息
     * @param appVersionNumber
     * @return
     */
    private OperationResult createAppVersion(AppVersionNumber appVersionNumber) {
        OperationResult ret = new OperationResult();

        try {
            JSObject one = DB.findOneBySql("SELECT id FROM app_version_number WHERE create_time = '" + appVersionNumber.getCreateTime() + "' AND app_id = '" + appVersionNumber.getAppId() + "'");
            if (one.get("id") != null) {
                ret.result = false;
                ret.message = "已经存在这条信息了";
            } else {
                DB.insert("app_version_number")
                        .put("app_id", appVersionNumber.getAppId())
                        .put("app_name", appVersionNumber.getAppName())
                        .put("create_time", appVersionNumber.getCreateTime())
                        .put("version_number", appVersionNumber.getVersionNumber())
                        .put("description", appVersionNumber.getDescription())
                        .execute();
                one = DB.findOneBySql("SELECT id FROM app_version_number WHERE create_time = '" + appVersionNumber.getCreateTime() + "' AND app_id = '" + appVersionNumber.getAppId() + "'");
                if (one.hasObjectData()){
                    ret.result =  true;
                    ret.message = "创建成功！";
                    ret.data = new JsonObject();
                    ret.data.addProperty("id", (long)one.get("id"));
                    ret.data.addProperty("app_id", appVersionNumber.getAppId());
                    ret.data.addProperty("app_name", appVersionNumber.getAppName());
                    ret.data.addProperty("create_time", appVersionNumber.getCreateTime());
                    ret.data.addProperty("version_number", appVersionNumber.getVersionNumber());
                    ret.data.addProperty("description", appVersionNumber.getDescription());
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

    /**
     * 更新应用版本号信息
     * @param appVersionNumber
     * @return
     */
    private OperationResult updateAppVersion(AppVersionNumber appVersionNumber) {
        OperationResult ret = new OperationResult();
        try {
            boolean execute = DB.updateBySql("UPDATE app_version_number\n" +
                    "SET app_id = '"+appVersionNumber.getAppId()+"',app_name = '"+appVersionNumber.getAppName()+"',\n" +
                    "create_time = '"+appVersionNumber.getCreateTime()+"',version_number = '"+appVersionNumber.getVersionNumber()+"',\n" +
                    "description = '"+appVersionNumber.getDescription()+"'\n" +
                    "WHERE id = " + appVersionNumber.getId());
            if (execute) {
                ret.result = true;
                ret.message = "执行成功";
                ret.data = new JsonObject();
                ret.data.addProperty("id", appVersionNumber.getId());
                ret.data.addProperty("app_id", appVersionNumber.getAppId());
                ret.data.addProperty("app_name", appVersionNumber.getAppName());
                ret.data.addProperty("create_time", appVersionNumber.getCreateTime());
                ret.data.addProperty("version_number", appVersionNumber.getVersionNumber());
                ret.data.addProperty("description", appVersionNumber.getDescription());
            }
        } catch (Exception e) {
            ret.result = false;
            ret.message = e.getMessage();
            Logger logger = Logger.getRootLogger();
            logger.error(e.getMessage(), e);
        }
        return ret;
    }

    private static Map<String,String> fetchAllAppIdAppName() {
        Map<String,String> rtnMap = new HashMap<String,String>();
        try {
            String sql = "SELECT app_id,app_name FROM app_data";
            List<JSObject> result = DB.findListBySql(sql);
            String appId = null;
            String appName = null;
            for (int i = 0; i < result.size(); i++) {
                appId = result.get(i).get("app_id");
                appName = result.get(i).get("app_name");
                rtnMap.put(appId,appName);
            }
        } catch (Exception ex) {
        }
        return rtnMap;
    }
}
