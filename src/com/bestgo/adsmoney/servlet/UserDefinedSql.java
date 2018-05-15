package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.OperationResult;
import com.bestgo.adsmoney.Utils;
import com.bestgo.adsmoney.bean.AppAdUnitConfig;
import com.bestgo.adsmoney.bean.AppUserDefinedSql;
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

/**
 * Created by jikai on 5/31/17.
 */
@WebServlet(name = "UserDefinedSql", urlPatterns = {"/user_defined_sql/*"})
public class UserDefinedSql extends HttpServlet {
    private static final String[] FIELDS = {"id", "app_id", "sql_type", "sql_text"};

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/create")) {
                AppUserDefinedSql userDefinedSql = new AppUserDefinedSql();
                userDefinedSql.appId = request.getParameter("app_id");
                userDefinedSql.sqlType = request.getParameter("sql_type");
                userDefinedSql.sqlText = request.getParameter("sql_text");

                OperationResult result = createNewUserDefinedSql(userDefinedSql);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
                json.add("data", result.data);
            } else if (path.startsWith("/delete")) {
                String id = request.getParameter("id");
                OperationResult result = deleteSql(id);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
            } else if (path.startsWith("/update")) {
                AppUserDefinedSql userDefinedSql = new AppUserDefinedSql();
                userDefinedSql.appId = request.getParameter("app_id");
                userDefinedSql.sqlType = request.getParameter("sql_type");
                userDefinedSql.sqlText = request.getParameter("sql_text");

                userDefinedSql.id = Utils.parseInt(request.getParameter("id"), 0);
                OperationResult result = updateSql(userDefinedSql);
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
            String sql = "select app_user_sqls.id, app_user_sqls.app_id, app_user_sqls.sql_type, app_user_sqls.sql_text " +
                    "from app_user_sqls, app_data " +
                    "where app_user_sqls.app_id=app_data.app_id and (app_name like " + "'%" + word + "%' or app_user_sqls.app_id like " + "'%" + word + "%'" +") order by id";
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
            return DB.scan("app_user_sqls").select(FIELDS).limit(size).start(index * size).orderByAsc("id").execute();
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return list;
    }

    public static long count() {
        try {
            JSObject object = DB.simpleScan("app_user_sqls").select(DB.func(DB.COUNT, "id")).execute();
            return object.get("count(id)");
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return 0;
    }

    private OperationResult deleteSql(String id) {
        OperationResult ret = new OperationResult();

        try {
            DB.delete("app_user_sqls").where(DB.filter().whereEqualTo("id", id)).execute();

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

    private OperationResult createNewUserDefinedSql(AppUserDefinedSql sql) {
        OperationResult ret = new OperationResult();

        try {
            JSObject one = DB.simpleScan("app_user_sqls").select("id")
                    .where(DB.filter().whereEqualTo("app_id", sql.appId))
                    .and(DB.filter().whereEqualTo("sql_type", sql.sqlType))
                    .execute();
            if (one.hasObjectData()) {
                ret.result = false;
                ret.message = "已经存在这个类型的SQL了";
            } else {
                DB.insert("app_user_sqls")
                        .put("app_id", sql.appId)
                        .put("sql_type", sql.sqlType)
                        .put("sql_text", sql.sqlText)
                        .execute();

                one = DB.simpleScan("app_user_sqls").select("id")
                        .where(DB.filter().whereEqualTo("app_id", sql.appId))
                        .and(DB.filter().whereEqualTo("sql_type", sql.sqlType))
                        .execute();
                if (one.hasObjectData()) {
                    ret.result = true;
                    ret.message = "修改成功";
                    ret.data = new JsonObject();
                    ret.data.addProperty("id", (long)one.get("id"));
                    ret.data.addProperty("app_id", sql.appId);
                    ret.data.addProperty("sql_type", sql.sqlType);
                    ret.data.addProperty("sql_text", sql.sqlText);
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

    private OperationResult updateSql(AppUserDefinedSql sql) {
        OperationResult ret = new OperationResult();

        try {
            DB.update("app_user_sqls")
                    .put("app_id", sql.appId)
                    .put("sql_type", sql.sqlType)
                    .put("sql_text", sql.sqlText)
                    .where(DB.filter().whereEqualTo("id", sql.id))
                    .execute();

            JSObject one = DB.simpleScan("app_user_sqls").select("id")
                    .where(DB.filter().whereEqualTo("app_id", sql.appId))
                    .and(DB.filter().whereEqualTo("sql_type", sql.sqlType))
                    .execute();
            if (one.hasObjectData()) {
                ret.result = true;
                ret.message = "修改成功";
                ret.data = new JsonObject();
                ret.data.addProperty("id", (long)one.get("id"));
                ret.data.addProperty("app_id", sql.appId);
                ret.data.addProperty("sql_type", sql.sqlType);
                ret.data.addProperty("sql_text", sql.sqlText);
            }
        } catch (Exception e) {
            ret.result = false;
            ret.message = e.getMessage();
            Logger logger = Logger.getRootLogger();
            logger.error(e.getMessage(), e);
        }

        return ret;
    }

    public final static class SqlType {
        public String name;
        public String value;
        public SqlType(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public static final SqlType[] _sqlType = {
            new SqlType("活跃用户Day1", "active_user_day1"),
            new SqlType("活跃用户Day2", "active_user_day2"),
            new SqlType("活跃用户Day3", "active_user_day3"),
            new SqlType("活跃用户Day4", "active_user_day4"),
            new SqlType("广告机会Day1", "ad_chance_day1"),
            new SqlType("广告机会Day2", "ad_chance_day2"),
            new SqlType("广告机会Day3", "ad_chance_day3"),
            new SqlType("广告机会Day4", "ad_chance_day4")
    };

    public static ArrayList<SqlType> getSqlTypeList() {
        ArrayList<SqlType> list = new ArrayList<>();
        for (int i = 0; i < _sqlType.length; i++) {
            list.add(_sqlType[i]);
        }
        return list;
    }
}
