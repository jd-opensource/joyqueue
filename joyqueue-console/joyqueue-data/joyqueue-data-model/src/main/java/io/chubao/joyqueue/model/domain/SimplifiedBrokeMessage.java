package io.chubao.joyqueue.model.domain;

import java.util.Map;

public class SimplifiedBrokeMessage {

    /**
     *  ip + port
     *
     **/
    private String queryId;
    /**
     *
     * partition+partition index
     *
     **/
    private String id;

    /**
     * receive time on broker
     * 精确到毫秒
     *
     **/
    private long sendTime;

    /**
     * relative time offset to sendTime,when write to file
     * 存储时间
     *
     **/
    private int storeTime;

    /**
     * bussiness id
     **/

    private String businessId;

    private Map<String,String>  attributes;
    /**
     *
     * 消息体
     **/
    private String body;

    /**
     * 是否已消费
     **/
    private  boolean flag;

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public int getStoreTime() {
        return storeTime;
    }

    public void setStoreTime(int storeTime) {
        this.storeTime = storeTime;
    }

    public String getBusinessId() {
        return businessId;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
