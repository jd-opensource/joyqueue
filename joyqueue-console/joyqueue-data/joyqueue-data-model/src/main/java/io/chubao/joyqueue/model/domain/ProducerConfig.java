package io.chubao.joyqueue.model.domain;

import io.chubao.joyqueue.model.domain.nsr.BaseNsrModel;

import java.util.HashMap;
import java.util.Map;

public class ProducerConfig extends BaseNsrModel {

    private String producerId;


    /**
     * 是否就近发送
     **/
    private boolean nearBy;

    /**
     * 集群实例发送权重
     **/
    private String weight;

    /**
     * 是否归档
     */
    private boolean archive;

    /**
     * 单个发送者
     **/
    private boolean single = false;

    private String blackList;

    private int limitTps;

    private int limitTraffic;

    public String getProducerId() {
        return producerId;
    }

    public void setProducerId(String producerId) {
        this.producerId = producerId;
    }

    public boolean isNearBy() {
        return nearBy;
    }

    public void setNearBy(boolean nearBy) {
        this.nearBy = nearBy;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public boolean isSingle() {
        return single;
    }

    public void setSingle(boolean single) {
        this.single = single;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public String getBlackList() {
        return blackList;
    }

    public void setBlackList(String blackList) {
        this.blackList = blackList;
    }

    public void setLimitTps(int limitTps) {
        this.limitTps = limitTps;
    }

    public int getLimitTps() {
        return limitTps;
    }

    public void setLimitTraffic(int limitTraffic) {
        this.limitTraffic = limitTraffic;
    }

    public int getLimitTraffic() {
        return limitTraffic;
    }

    /**
     * 获取权重
     *
     * @return 权重
     */
    public Map<String, Short> weights() {
        if (weight == null || weight.isEmpty()) {
            return null;
        }
        Map<String, Short> map = new HashMap<String, Short>();
        String[] values = weight.split(",");
        String[] parts;
        for (String value : values) {
            parts = value.split(":");
            if (parts.length >= 2) {
                try {
                    map.put(parts[0], Short.parseShort(parts[1]));
                } catch (NumberFormatException e) {
                }
            }
        }

        return map;
    }
}
