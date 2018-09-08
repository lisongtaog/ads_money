package com.bestgo.adsmoney.bean;

import java.util.List;

public class MetricsResponse extends BaseResponse {
    public List<AppAdUnitMetrics> daily;
    public List<AppAdUnitMetrics> country;

    public MetricsResponse() {
        ret = 0;
    }

    public MetricsResponse(List<AppAdUnitMetrics> daily, List<AppAdUnitMetrics> country) {
        ret = 1;
        this.daily = daily;
        this.country = country;
    }
}
