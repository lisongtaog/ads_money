package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.OperationResult;
import com.bestgo.adsmoney.Utils;
import com.bestgo.adsmoney.bean.AppData;
import com.bestgo.adsmoney.bean.FirebaseProject;
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
@WebServlet(name = "FirebaseManagement", urlPatterns = {"/firebase_management/*"})
public class FirebaseManagement extends HttpServlet {
    private static final String[] FIELDS = {"id", "project_id", "project_name", "property_id"};

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/create")) {
                FirebaseProject firebaseProject = new FirebaseProject();
                firebaseProject.projectId = request.getParameter("project_id");
                firebaseProject.projectName = request.getParameter("project_name");
                firebaseProject.propertyId = request.getParameter("property_id");

                OperationResult result = createNewFirebaseProject(firebaseProject);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
                json.add("data", result.data);
            } else if (path.startsWith("/delete")) {
                String id = request.getParameter("id");
                OperationResult result = deleteFirebaseProject(id);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
            } else if (path.startsWith("/update")) {
                FirebaseProject project = new FirebaseProject();
                project.projectId = request.getParameter("project_id");
                project.projectName = request.getParameter("project_name");
                project.propertyId = request.getParameter("property_id");
                project.id = Utils.parseInt(request.getParameter("id"), 0);
                OperationResult result = updateFirebaseProject(project);
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
            return DB.scan("app_firebase_project").select(FIELDS)
                    .where(DB.filter().whereLikeTo("project_id", "%" + word + "%"))
                    .or(DB.filter().whereLikeTo("project_name", "%" + word + "%")).orderByAsc("id").execute();
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return list;
    }

    public static List<JSObject> fetchData(int index, int size) {
        List<JSObject> list = new ArrayList<>();
        try {
            return DB.scan("app_firebase_project").select(FIELDS).limit(size).start(index * size).orderByAsc("id").execute();
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return list;
    }

    public static long count() {
        try {
            JSObject object = DB.simpleScan("app_firebase_project").select(DB.func(DB.COUNT, "id")).execute();
            return object.get("count(id)");
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return 0;
    }

    private OperationResult deleteFirebaseProject(String id) {
        OperationResult ret = new OperationResult();

        try {
            DB.delete("app_firebase_project").where(DB.filter().whereEqualTo("id", id)).execute();

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

    private OperationResult createNewFirebaseProject(FirebaseProject project) {
        OperationResult ret = new OperationResult();

        try {
            JSObject one = DB.simpleScan("app_firebase_project").select("id").where(DB.filter().whereEqualTo("project_id", project.projectId)).execute();
            if (one.get("id") != null) {
                ret.result = false;
                ret.message = "已经存在这个应用了";
            } else {
                DB.insert("app_firebase_project")
                        .put("project_id", project.projectId)
                        .put("project_name", project.projectName)
                        .put("property_id", project.propertyId)
                        .execute();

                one = DB.simpleScan("app_firebase_project").select("id").where(DB.filter().whereEqualTo("project_id", project.projectId)).execute();
                if (one.hasObjectData()) {
                    ret.result = true;
                    ret.message = "修改成功";
                    ret.data = new JsonObject();
                    ret.data.addProperty("id", (long)one.get("id"));
                    ret.data.addProperty("project_id", project.projectId);
                    ret.data.addProperty("project_name", project.projectName);
                    ret.data.addProperty("property_id", project.propertyId);
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

    private OperationResult updateFirebaseProject(FirebaseProject project) {
        OperationResult ret = new OperationResult();

        try {
            DB.update("app_firebase_project")
                    .put("project_id", project.projectId)
                    .put("project_name", project.projectName)
                    .put("property_id", project.propertyId)
                    .where(DB.filter().whereEqualTo("id", project.id))
                    .execute();

            JSObject one = DB.simpleScan("app_firebase_project").select("id").where(DB.filter().whereEqualTo("project_id", project.projectId)).execute();
            if (one.hasObjectData()) {
                ret.result = true;
                ret.message = "修改成功";
                ret.data = new JsonObject();
                ret.data.addProperty("id", (long)one.get("id"));
                ret.data.addProperty("project_id", project.projectId);
                ret.data.addProperty("project_name", project.projectName);
                ret.data.addProperty("property_id", project.propertyId);
            }
        } catch (Exception e) {
            ret.result = false;
            ret.message = e.getMessage();
            Logger logger = Logger.getRootLogger();
            logger.error(e.getMessage(), e);
        }

        return ret;
    }

    public static List<FirebaseProject> fetchAllFirebaseProject() {
        List<FirebaseProject> list = new ArrayList<>();
        try {
            List<JSObject> projects = DB.scan("app_firebase_project").select(FIELDS).execute();
            for (int i = 0; i < projects.size(); i++) {
                FirebaseProject one = new FirebaseProject();
                one.id = projects.get(i).get("id");
                one.projectId = projects.get(i).get("project_id");
                one.projectName = projects.get(i).get("project_name");
                one.propertyId = projects.get(i).get("property_id");
                list.add(one);
            }
        } catch (Exception ex) {
        }
        return list;
    }
}
