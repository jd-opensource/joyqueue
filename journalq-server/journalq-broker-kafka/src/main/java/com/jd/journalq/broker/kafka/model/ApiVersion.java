package com.jd.journalq.broker.kafka.model;

/**
 * ApiVersion
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class ApiVersion {

    private short code;
    private short minVersion;
    private short maxVersion;

    public ApiVersion(short code, short minVersion, short maxVersion) {
        this.code = code;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
    }

    public short getCode() {
        return code;
    }

    public short getMinVersion() {
        return minVersion;
    }

    public short getMaxVersion() {
        return maxVersion;
    }
}