package com.bestgo.adsmoney.servlet;

import com.bestgo.adsmoney.OperationResult;
import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.adsmoney.bean.AppAdMobAccount;
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
@WebServlet(name = "AdMobAccountManagement", urlPatterns = {"/admob_account_management/*"})
public class AdMobAccount extends HttpServlet {
    private static final String[] FIELDS = {"account", "account_name"};

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Utils.isAdmin(request, response)) return;

        String path = request.getPathInfo();
        JsonObject json = new JsonObject();

        if (path != null) {
            if (path.startsWith("/create")) {
                AppAdMobAccount admobAccount = new AppAdMobAccount();
                admobAccount.account = request.getParameter("account");
                admobAccount.accountName = request.getParameter("account_name");

                OperationResult result = createAdmobAccount(admobAccount);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
                json.add("data", result.data);
            } else if (path.startsWith("/delete")) {
                String account = request.getParameter("account");
                OperationResult result = deleteAdmobAccount(account);
                json.addProperty("ret", result.result ? 1 : 0);
                json.addProperty("message", result.message);
            } else if (path.startsWith("/update")) {
                AppAdMobAccount admobAccount = new AppAdMobAccount();
                admobAccount.account = request.getParameter("account");
                admobAccount.accountName = request.getParameter("account_name");
                OperationResult result = updateAdmobAccount(admobAccount);
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
            return DB.scan("app_admob_accounts").select(FIELDS)
                    .where(DB.filter().whereLikeTo("account", "%" + word + "%"))
                    .or(DB.filter().whereLikeTo("account_name", "%" + word + "%")).orderByAsc("account").execute();
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return list;
    }

    public static List<JSObject> fetchData(int index, int size) {
        List<JSObject> list = new ArrayList<>();
        try {
            return DB.scan("app_admob_accounts").select(FIELDS).limit(size).start(index * size).orderByAsc("account").execute();
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return list;
    }

    public static long count() {
        try {
            JSObject object = DB.simpleScan("app_admob_accounts").select(DB.func(DB.COUNT, "account")).execute();
            return object.get("count(account)");
        } catch (Exception ex) {
            Logger logger = Logger.getRootLogger();
            logger.error(ex.getMessage(), ex);
        }
        return 0;
    }

    private OperationResult deleteAdmobAccount(String account) {
        OperationResult ret = new OperationResult();

        try {
            DB.delete("app_admob_accounts").where(DB.filter().whereEqualTo("account", account)).execute();

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

    private OperationResult createAdmobAccount(AppAdMobAccount admobAccount) {
        OperationResult ret = new OperationResult();

        try {
            JSObject one = DB.simpleScan("app_admob_accounts").select("account").where(DB.filter().whereEqualTo("account", admobAccount.account)).execute();
            if (one.get("account") != null) {
                ret.result = false;
                ret.message = "已经存在这个账户了";
            } else {
                DB.insert("app_admob_accounts")
                        .put("account", admobAccount.account)
                        .put("account_name", admobAccount.accountName)
                        .execute();

                one = DB.simpleScan("app_admob_accounts").select("account").where(DB.filter().whereEqualTo("account", admobAccount.account)).execute();
                if (one.hasObjectData()) {
                    ret.result = true;
                    ret.message = "修改成功";
                    ret.data = new JsonObject();
                    ret.data.addProperty("account", admobAccount.account);
                    ret.data.addProperty("account_name", admobAccount.accountName);
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

    private OperationResult updateAdmobAccount(AppAdMobAccount admobAccount) {
        OperationResult ret = new OperationResult();

        try {
            DB.update("app_admob_accounts")
                    .put("account", admobAccount.account)
                    .put("account_name", admobAccount.accountName)
                    .where(DB.filter().whereEqualTo("account", admobAccount.account))
                    .execute();

            JSObject one = DB.simpleScan("app_admob_accounts").select("account").where(DB.filter().whereEqualTo("account", admobAccount.account)).execute();
            if (one.hasObjectData()) {
                ret.result = true;
                ret.message = "修改成功";
                ret.data = new JsonObject();
                ret.data.addProperty("account", admobAccount.account);
                ret.data.addProperty("account_name", admobAccount.accountName);
            }
        } catch (Exception e) {
            ret.result = false;
            ret.message = e.getMessage();
            Logger logger = Logger.getRootLogger();
            logger.error(e.getMessage(), e);
        }

        return ret;
    }

    public static List<AppAdMobAccount> fetchAllAccounts() {
        List<AppAdMobAccount> list = new ArrayList<>();
        try {
            List<JSObject> accounts = DB.scan("app_admob_accounts").select(FIELDS).execute();
            for (int i = 0; i < accounts.size(); i++) {
                AppAdMobAccount one = new AppAdMobAccount();
                one.account = accounts.get(i).get("account");
                one.accountName = accounts.get(i).get("account_name");
                list.add(one);
            }
        } catch (Exception ex) {
        }
        return list;
    }
}
