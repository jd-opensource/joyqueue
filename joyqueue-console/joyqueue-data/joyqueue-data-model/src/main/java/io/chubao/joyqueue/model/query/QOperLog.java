package io.chubao.joyqueue.model.query;

import io.chubao.joyqueue.model.QKeyword;

import java.util.Date;

/**
 *
 * 操作日志查询条件
 *
 * @author liyubo4
 * @create 2017-12-06 11:12
 **/
public class QOperLog extends QKeyword {

    /**
     * 创建时间
     */
    private Date createTime;

    private Date beginTime;
    private Date endTime;
    private String erp;

    /**
     * 操作对象的类型，集群/程序/作业
     */
    private Integer type;

    private String identity;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getErp() {
        return erp;
    }

    public void setErp(String erp) {
        this.erp = erp;
    }
}
