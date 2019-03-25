package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.ApiVersion;

import java.util.List;

/**
 * ApiVersionsResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class ApiVersionsResponse extends KafkaRequestOrResponse {

    private short errorCode;
    private List<ApiVersion> apis;

    public ApiVersionsResponse(short errorCode, List<ApiVersion> apis) {
        this.errorCode = errorCode;
        this.apis = apis;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public void setApis(List<ApiVersion> apis) {
        this.apis = apis;
    }

    public List<ApiVersion> getApis() {
        return apis;
    }

    @Override
    public int type() {
        return KafkaCommandType.API_VERSIONS.getCode();
    }
}