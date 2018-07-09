package com.bestgo.adsmoney.cache;

import com.bestgo.adsmoney.utils.Utils;
import com.bestgo.adsmoney.bean.AppAdUnitMetrics;
import com.bestgo.common.database.services.DB;
import com.bestgo.common.database.utils.JSObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalCache {
    private static ConcurrentHashMap<String, CacheItem> cacheMap = new ConcurrentHashMap<>();

    public static void clear() {
        cacheMap.clear();
    }

    public static CacheItem findCacheItem(String appId) {
        CacheItem item = cacheMap.get(appId);
        if (item == null) {
            List<AppAdUnitMetrics> list = fetchAppAdsMetrics(appId);
            if (list.size() > 0) {
                HashMap<String, AppAdUnitMetrics> map = new HashMap<>();
                HashMap<String, List<AppAdUnitMetrics>> countryMap = new HashMap<>();
                for (int i = 0; i < list.size(); i++) {
                    AppAdUnitMetrics one = list.get(i);
                    String adUnitId = one.adUnitId;
                    AppAdUnitMetrics metric = map.get(adUnitId);
                    if (metric == null) {
                        metric = new AppAdUnitMetrics();
                        map.put(adUnitId, metric);
                    }
                    metric.appId = one.appId;
                    metric.adUnitId = one.adUnitId;
                    metric.adNetwork = one.adNetwork;
                    metric.adRequest += one.adRequest;
                    metric.adFilled += one.adFilled;
                    metric.adImpression += one.adImpression;
                    metric.adClicked += one.adClicked;
                    metric.adRevenue += one.adRevenue;

                    List<AppAdUnitMetrics> countryCodes = countryMap.get(one.countryCode);
                    if (countryCodes == null) {
                        countryCodes = new ArrayList<>();
                        countryMap.put(one.countryCode, countryCodes);
                    }
                    countryCodes.add(one);
                }
                if (map.size() > 0 && countryMap.size() > 0) {
                    item = new CacheItem();
                    item.metrics = new ArrayList<>(map.values());
                    item.countryCacheMap = countryMap;
                    map.clear();
                    cacheMap.put(appId, item);
                }
            }
        }
        return item;
    }

    private static List<AppAdUnitMetrics> fetchAppAdsMetrics(String appId) {
        List<AppAdUnitMetrics> metrics = new ArrayList<>();
        String sql = "select app_id, ad_network, ad_unit_id, country_code, sum(ad_request) as ad_request, sum(ad_filled) as ad_filled, " +
                "sum(ad_impression) as ad_impression, sum(ad_click) as ad_click, sum(ad_revenue) as ad_revenue from app_ad_unit_metrics " +
                "where app_id=? "+
                "group by app_id, ad_network, ad_unit_id, country_code";
        try {
            List<JSObject> list = DB.findListBySql(sql, appId);
            for (int i = 0; i < list.size(); i++) {
                AppAdUnitMetrics metric = new AppAdUnitMetrics();
                metric.appId = list.get(i).get("app_id");
                metric.adNetwork = list.get(i).get("ad_network");
                metric.adUnitId = list.get(i).get("ad_unit_id");
                metric.countryCode = list.get(i).get("country_code");
                metric.adRequest = Utils.convertLong(list.get(i).get("ad_request"), 0);
                metric.adFilled = Utils.convertLong(list.get(i).get("ad_filled"), 0);
                metric.adImpression = Utils.convertLong(list.get(i).get("ad_impression"), 0);
                metric.adClicked = Utils.convertLong(list.get(i).get("ad_click"), 0);
                metric.adRevenue = Utils.trimDouble(list.get(i).get("ad_revenue"));
                metrics.add(metric);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return metrics;
    }
}
