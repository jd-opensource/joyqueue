package com.jd.journalq.model.query;

import com.jd.journalq.common.model.QKeyword;

public class QMetric extends QKeyword {

    /**
     * metric code, abbr.
     */
    private String code;
    /**
     * metric value, inner used, unique
     */
    private String aliasCode;
    /**
     * metric name
     */
    private String name;
    /**
     * metric type, atomic or aggregator
     */
    private Integer type;
    /**
     * only for aggregator metric, which describe metric's origin metric code
     */
    private String source;
    /**
     * describe metric aggregate method or others
     */
    private String description;
    /**
     * metric provider
     */
    private String provider;

    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public String getAliasCode() {
        return aliasCode;
    }

    public void setAiasCode(String aliasCode) {
        this.aliasCode = aliasCode;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }


}
