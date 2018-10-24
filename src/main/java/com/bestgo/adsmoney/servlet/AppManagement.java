package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.OperationResult;
import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.adsmoney.bean.AppData;
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
import java.util.List;

/**
 * Created by jikai on 5/31/17.
 */
@WebServlet(name = "AppManagement", urlPatterns = {"/app_management/*"})
public class AppManagement extends HttpServlet {
    private static final String[] FIELDS = {"id", "app_id", "app_name", "fb_access_token", "fb_app_id", "admob_account", "firebase_project_id"};

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/create")) {
                AppData appData = new AppData();
                appData.appId = request.getParameter("app_id");
                appData.appName = request.getParameter("app_name");
                appData.fbAccessToken = request.getParameter("fb_access_token");
                appData.fbAppId = request.getParameter("fb_app_id");
                appData.admobAccount = request.getParameter("admob_account");
                appData.firebaseProjectId = request.getParameter("firebase_project_id");

                OperationResult result = createNewAppData(appData);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
                json.add("data", result.data);
            } else if (path.startsWith("/delete")) {
                String id = request.getParameter("id");
                OperationResult result = deleteAppData(id);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
            } else if (path.startsWith("/update")) {
                AppData appData = new AppData();
                appData.appId = request.getParameter("app_id");
                appData.appName = request.getParameter("app_name");
                appData.fbAccessToken = request.getParameter("fb_access_token");
                appData.fbAppId = request.getParameter("fb_app_id");
                appData.admobAccount = request.getParameter("admob_account");
                appData.firebaseProjectId = request.getParameter("firebase_project_id");
                appData.id = Utils.parseInt(request.getParameter("id"), 0);
                OperationResult result = updateAppData(appData);
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

    public static List<JSObject> fetchData(String word) {
        List<JSObject> list = new ArrayList<>();
        try {
            return DB.scan("app_data").select(FIELDS)
                    .where(DB.filter().whereLikeTo("app_id", "%" + word + "%"))
                    .or(DB.filter().whereLikeTo("app_name", "%" + word + "%")).orderByAsc("id").execute();
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return list;
    }

    public static List<JSObject> fetchData(int index, int size) {
        List<JSObject> list = new ArrayList<>();
        try {
            return DB.scan("app_data").select(FIELDS).limit(size).start(index * size).orderByAsc("id").execute();
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return list;
    }

    public static long count() {
        try {
            JSObject object = DB.simpleScan("app_data").select(DB.func(DB.COUNT, "id")).execute();
            return object.get("count(id)");
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return 0;
    }

    private OperationResult deleteAppData(String id) {
        OperationResult ret = new OperationResult();

        try {
            DB.delete("app_data").where(DB.filter().whereEqualTo("id", id)).execute();

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

    private OperationResult createNewAppData(AppData appData) {
        OperationResult ret = new OperationResult();

        try {
            JSObject one = DB.simpleScan("app_data").select("id").where(DB.filter().whereEqualTo("app_id", appData.appId)).execute();
            if (one.get("id") != null) {
                ret.result = false;
                ret.message = "已经存在这个应用了";
            } else {
                DB.insert("app_data")
                        .put("app_id", appData.appId)
                        .put("app_name", appData.appName)
                        .put("fb_access_token", appData.fbAccessToken)
                        .put("fb_app_id", appData.fbAppId)
                        .put("admob_account", appData.admobAccount)
                        .put("firebase_project_id", appData.firebaseProjectId)
                        .execute();

                one = DB.simpleScan("app_data").select("id").where(DB.filter().whereEqualTo("app_id", appData.appId)).execute();
                if (one.hasObjectData()) {
                    ret.result = true;
                    ret.message = "修改成功";
                    ret.data = new JsonObject();
                    ret.data.addProperty("id", (long) one.get("id"));
                    ret.data.addProperty("app_id", appData.appId);
                    ret.data.addProperty("app_name", appData.appName);
                    ret.data.addProperty("fb_access_token", appData.fbAccessToken);
                    ret.data.addProperty("fb_app_id", appData.fbAppId);
                    ret.data.addProperty("admob_account", appData.admobAccount);
                    ret.data.addProperty("firebase_project_id", appData.firebaseProjectId);
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

    private OperationResult updateAppData(AppData appData) {
        OperationResult ret = new OperationResult();

        try {
            DB.update("app_data")
                    .put("app_id", appData.appId)
                    .put("app_name", appData.appName)
                    .put("fb_access_token", appData.fbAccessToken)
                    .put("fb_app_id", appData.fbAppId)
                    .put("admob_account", appData.admobAccount)
                    .put("firebase_project_id", appData.firebaseProjectId)
                    .where(DB.filter().whereEqualTo("id", appData.id))
                    .execute();

            JSObject one = DB.simpleScan("app_data").select("id").where(DB.filter().whereEqualTo("app_id", appData.appId)).execute();
            if (one.hasObjectData()) {
                ret.result = true;
                ret.message = "修改成功";
                ret.data = new JsonObject();
                ret.data.addProperty("id", (long) one.get("id"));
                ret.data.addProperty("app_id", appData.appId);
                ret.data.addProperty("app_name", appData.appName);
                ret.data.addProperty("fb_access_token", appData.fbAccessToken);
                ret.data.addProperty("fb_app_id", appData.fbAppId);
                ret.data.addProperty("admob_account", appData.admobAccount);
                ret.data.addProperty("firebase_project_id", appData.firebaseProjectId);
            }
        } catch (Exception e) {
            ret.result = false;
            ret.message = e.getMessage();
            Logger logger = Logger.getRootLogger();
            logger.error(e.getMessage(), e);
        }

        return ret;
    }

    public static List<AppData> fetchAllAppData(int... isvisitor) {

        List<AppData> list = new ArrayList<>();
        try {
            List<JSObject> accounts = DB.scan("app_data").select(FIELDS).execute();
            if (isvisitor.length == 0) {
                for (int i = 0; i < accounts.size(); i++) {
                    AppData one = new AppData();
                    one.id = accounts.get(i).get("id");
                    one.appId = accounts.get(i).get("app_id");
                    one.appName = accounts.get(i).get("app_name");
                    one.fbAccessToken = accounts.get(i).get("fb_access_token");
                    one.fbAppId = accounts.get(i).get("fb_app_id");
                    one.admobAccount = accounts.get(i).get("admob_account");
                    one.firebaseProjectId = accounts.get(i).get("firebase_project_id");
                    list.add(one);
                }
            } else if (isvisitor.length == 1) {
                String filter = "com.solitaire.free.lj1," +
                        "com.collection.card.free," +
                        "com.ancient_card.free," +
                        "com.pyramid_card.free," +
                        "com.solitaire_star.card.free";
                for (int i = 0; i < accounts.size(); i++) {
                    AppData one = new AppData();
                    one.id = accounts.get(i).get("id");
                    one.appId = accounts.get(i).get("app_id");
                    one.appName = accounts.get(i).get("app_name");
                    one.fbAccessToken = accounts.get(i).get("fb_access_token");
                    one.fbAppId = accounts.get(i).get("fb_app_id");
                    one.admobAccount = accounts.get(i).get("admob_account");
                    one.firebaseProjectId = accounts.get(i).get("firebase_project_id");
                    if (filter.contains(one.appId)) {
                        list.add(one);
                    }
                }
            }

        } catch (Exception ex) {
        }
        return list;
    }
}
