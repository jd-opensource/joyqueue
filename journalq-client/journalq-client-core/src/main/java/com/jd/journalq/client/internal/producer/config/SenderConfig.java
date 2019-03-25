package com.jd.journalq.client.internal.producer.config;

/**
 * SenderConfig
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/11
 */
public class SenderConfig {

    private boolean compress = true;
    private int compressThreshold = 1024 * 1;
    private String compressType;

    public SenderConfig() {

    }

    public SenderConfig(boolean compress, int compressThreshold, String compressType) {
        this.compress = compress;
        this.compressThreshold = compressThreshold;
        this.compressType = compressType;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public int getCompressThreshold() {
        return compressThreshold;
    }

    public void setCompressThreshold(int compressThreshold) {
        this.compressThreshold = compressThreshold;
    }

    public String getCompressType() {
        return compressType;
    }

    public void setCompressType(String compressType) {
        this.compressType = compressType;
    }
}