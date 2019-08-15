package io.chubao.joyqueue.model.domain;

import io.chubao.joyqueue.model.domain.nsr.BaseNsrModel;

import java.util.regex.Pattern;

/**
 * Created by wangxiaofei1 on 2018/10/30.
 */
public class DataCenter extends BaseNsrModel {

    private static Pattern P_RANGE1 = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$");
    private static Pattern P_RANGE2 = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\-([0-9]{1,3})$");
    private static Pattern P_RANGE3 = Pattern.compile(
            "^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\-([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})"
                    + "\\.([0-9]{1,3})$");
    private static Pattern P_RANGE4 = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.\\*$");

    private String code;
    private String name;
    private String ips;

    private String matchType;

    private String url;
    private boolean scalable;
    private String region;

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public String getIps() {
        return ips;
    }

    public void setIps(String ips) {
        this.ips = ips;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isScalable() {
        return scalable;
    }

    public void setScalable(boolean scalable) {
        this.scalable = scalable;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
