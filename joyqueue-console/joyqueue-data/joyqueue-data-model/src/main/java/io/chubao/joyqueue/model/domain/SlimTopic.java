package io.chubao.joyqueue.model.domain;

import java.util.List;

public class SlimTopic {
    private String code;
    private List<String> ips;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }
}
