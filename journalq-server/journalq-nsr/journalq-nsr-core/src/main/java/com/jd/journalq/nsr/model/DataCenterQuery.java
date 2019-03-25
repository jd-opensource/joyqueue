package com.jd.journalq.nsr.model;

import com.jd.journalq.common.model.Query;

public class DataCenterQuery implements Query {
    /**
     * 区域
     */
    private String region;
    /**
     * 数据中心code
     */
    private String code;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
