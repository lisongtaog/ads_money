package com.bestgo.common.database;

import com.bestgo.common.database.services.DB;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.List;

public class MySqlHelper {
	private static DataSource ds = null;
	
	public static void uninit() {
	    try {
	        Enumeration<Driver> drivers = DriverManager.getDrivers();
	        while(drivers.hasMoreElements()) {
	            DriverManager.deregisterDriver(drivers.nextElement());
	        }
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
		ds = null;
	}
	
	public static void init() {
		if (ds == null) {
			Context ctx = null;
			Object obj = null;
			try {
				ctx = new InitialContext();
				obj = ctx.lookup("java:comp/env/jdbc/mysql");
			} catch (NamingException e1) {
				e1.printStackTrace();
			}
			
			ds = (DataSource) obj;

			DB.init();
		}
	}
	
	public Connection getConnection() {
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return conn;
	}

	/**
	 * 批量持久化 到数据库
	 * @param sqlList
	 * @return
	 */
	public boolean excuteBatch2DB(List<String> sqlList){
		boolean rtn = true;
		Connection conn = getConnection();
		Statement stmt = null;
		try {
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			int index = 0;
			for (String sql : sqlList){
				index ++;
				stmt.addBatch(sql);
				if (index % 1000 == 0){//1000条一次提交
					stmt.executeBatch();
					conn.commit();
				}
			}
			stmt.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);
		}catch (Exception e){
			rtn = false;
			e.printStackTrace();
		}
		return rtn;
	}

	public boolean excuteBatch2DB(String sql,List<List<Object>> valueList){
		boolean rtn = true;
		Connection conn = getConnection();
		PreparedStatement pstmt = null;
		try {
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(sql);
			int index = 0;
			for (List<Object> vals : valueList){
				index ++;
				setValue(pstmt,vals);
                pstmt.addBatch();
				if (index % 1000 == 0){//1000条一次提交
					pstmt.executeBatch();
					conn.commit();
				}
			}
			pstmt.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);
		}catch (Exception e){
			rtn = false;
			e.printStackTrace();
		}
		return rtn;
	}

	private void setValue(PreparedStatement pstmt,List<Object> vals) throws SQLException{
		Object param = null;int index;
		for (int i = 0; i< vals.size() ; i++){
			param = vals.get(i);
			index = i + 1;
			if (param instanceof Integer) {
				int value = ((Integer) param).intValue();
				pstmt.setInt(index, value);
			} else if (param instanceof String) {
				String s = (String) param;
				pstmt.setString(index, s);
			} else if (param instanceof Double) {
				double d = ((Double) param).doubleValue();
				pstmt.setDouble(index, d);
			} else if (param instanceof Float) {
				float f = ((Float) param).floatValue();
				pstmt.setFloat(index, f);
			} else if (param instanceof Long) {
				long l = ((Long) param).longValue();
				pstmt.setLong(index, l);
			} else if (param instanceof Boolean) {
				boolean b = ((Boolean) param).booleanValue();
				pstmt.setBoolean(index, b);
			} else if (param instanceof java.util.Date) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				//pstmt.setDate(index, (Date) param); //yyyy-MM-dd
				pstmt.setString(index, sdf.format((java.util.Date)param));
			}
		}
	}
}
