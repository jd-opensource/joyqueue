package io.chubao.joyqueue.nsr.journalkeeper.domain;

import io.chubao.joyqueue.nsr.journalkeeper.helper.Column;

/**
 * BrokerDTO
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class BrokerDTO extends BaseDTO {

    private Long id;
    private String ip;
    private Integer port;
    @Column(alias = "data_center")
    private String dataCenter;
    @Column(alias = "retry_type")
    private String retryType;
    private String permission;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public String getRetryType() {
        return retryType;
    }

    public void setRetryType(String retryType) {
        this.retryType = retryType;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}