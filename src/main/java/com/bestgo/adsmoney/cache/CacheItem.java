package com.bestgo.adsmoney.cache;

import com.bestgo.adsmoney.bean.AppAdUnitMetrics;

import java.util.HashMap;
import java.util.List;

public class CacheItem {
    public List<AppAdUnitMetrics> metrics;
    public HashMap<String, List<AppAdUnitMetrics>> countryCacheMap;

    public CacheItem() {
    }
}