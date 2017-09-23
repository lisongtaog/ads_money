package com.bestgo.common.database.dao;

import com.bestgo.common.database.utils.JSObject;

import java.util.ArrayList;
import java.util.List;

public class DaoProxy implements IDao {
    private DatabaseConnection dbc = null;
    private IDao dao = null;

    public DaoProxy() {
        this.dbc = new DatabaseConnection();
        this.dao = new DaoImpl(this.dbc.getConnection());
    }

    public List<JSObject> findModeResult(String con, Object... params) {
        ArrayList<JSObject> list = new ArrayList();
        try {
            return this.dao.findModeResult(con, params);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                this.dbc.releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }


    public JSObject findSimpleResult(String con, Object... params) {
        JSObject map = new JSObject();
        try {
            return this.dao.findSimpleResult(con, params);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                this.dbc.releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }


    public boolean updateByPreparedStatement(String con, Object... params) {
        boolean flag = false;
        try {
            return this.dao.updateByPreparedStatement(con, params);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                this.dbc.releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
    }
}