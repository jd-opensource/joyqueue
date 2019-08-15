package io.chubao.joyqueue.domain;

/**
 * @author wylixiaobin
 * Date: 2018/8/30
 */
public class DataCenter {
    private static final String UNKNOWN = "UNKNOWN";
    public static DataCenter DEFAULT = new DataCenter(UNKNOWN, UNKNOWN);
    /**
     * 数据中心code
     */
    protected String code;

    /**
     * 数据中心名称
     */
    protected String name;
    /**
     * 数据中心所在区域
     */
    protected String region;

    /**
     * 数据中心匹配规则
     */
    private String url;

    public String getId() {
        return getRegion() + "_" + getCode();
    }

    public DataCenter(String code, String region) {
        this.code = code;
        this.region = region;

    }

    public DataCenter(String code, String name, String region, String url) {
        this.code = code;
        this.region = region;
        this.name = name;
        this.url = url;
    }

    public DataCenter() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
