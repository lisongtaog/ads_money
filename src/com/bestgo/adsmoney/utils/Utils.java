package com.bestgo.adsmoney.utils;

import com.bestgo.common.database.services.DB;
import com.bestgo.common.database.utils.JSObject;
import com.google.gson.JsonObject;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Utils {
    public static HashMap<String, String> countryCodeMap;
    private static Object lock = new Object();

    public static boolean isAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json");
        HttpSession session = request.getSession();
        Object isAdmin = session.getAttribute("isAdmin");
        if (isAdmin == null) {
            JsonObject json = new JsonObject();
            json.addProperty("ret", 0);
            json.addProperty("message", "请先登录");
            response.getWriter().write(json.toString());
            return false;
        }
        return true;
    }

    public static String getRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream) request.getInputStream(), "utf-8"));
        StringBuffer sb = new StringBuffer("");
        String temp;
        while ((temp = br.readLine()) != null) {
            sb.append(temp);
        }
        br.close();
        return sb.toString();
    }

    public static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static long convertLong(Object value, long defaultValue) {
        try {
            if (value instanceof BigDecimal) {
                return ((BigDecimal)value).longValue();
            } else if (value instanceof Double) {
                return (long)value;
            } else if (value instanceof Long) {
                return (Long)value;
            } else if (value instanceof Integer) {
                return (Integer)value;
            } else if(value instanceof String){
                Long.parseLong(value.toString());
            }
        } catch (Exception ex) {
        }
        return defaultValue;
    }

    public static double convertDouble(Object value, double defaultValue) {
        try {
            if (value instanceof BigDecimal) {
                return ((BigDecimal)value).doubleValue();
            } else if (value instanceof Double) {
                return (double)value;
            } else if (value instanceof Long) {
                return (Long)value;
            } else if (value instanceof Integer) {
                return (Integer)value;
            } else if(value instanceof String){
                Double.parseDouble(value.toString());
            }
        } catch (Exception ex) {
        }
        return defaultValue;
    }

    public static String formatDate(Date date) {
        return String.format("%d-%02d-%02d", date.getYear() + 1900, date.getMonth() + 1, date.getDate());
    }

    public static double trimDouble(double value) {
        return Double.parseDouble(String.format("%.4f", value));
    }

    public static float trimFloat(float value) {
        return Float.parseFloat(String.format("%.4f", value));
    }

    public static String trimString(String str) {
        if (str != null) {
            return str.trim();
        }
        return str;
    }

    public static HashMap getCountryMap() {
        if (countryCodeMap == null) {
            try {
                synchronized (lock) {
                    countryCodeMap = new HashMap<>();
                    List<JSObject> list = DB.scan("app_country_code_dict").select("country_code", "country_name").execute();
                    for (JSObject one : list) {
                        String countryCode = one.get("country_code");
                        String countryName = one.get("country_name");
                        countryCodeMap.put(countryCode, countryName);
                    }
                }
            } catch (Exception ex) {
            }
        }
        return countryCodeMap;
    }
}
