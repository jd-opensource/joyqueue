package org.joyqueue.broker.joyqueue0.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuduohui on 2018/1/31.
 */
public class CollectorConfig implements Serializable{
    private List<Collector> collectors = new ArrayList<Collector>();

    private int profileInterval = 30000;
    private int machineMetricInterval = 20000;
    private boolean enableProfileMetric = true;
    private boolean enableMachineMetric = true;

    public CollectorConfig() {}

    public List<Collector> getCollectors() {
        return collectors;
    }

    public void setCollectors(List<Collector> collectors) {
        this.collectors = collectors;
    }

    public int getProfileInterval() {
        return profileInterval;
    }

    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }

    public int getMachineMetricInterval() {
        return machineMetricInterval;
    }

    public void setMachineMetricInterval(int machineMetricInterval) {
        this.machineMetricInterval = machineMetricInterval;
    }

    public boolean isEnableProfileMetric() {
        return enableProfileMetric;
    }

    public void setEnableProfileMetric(boolean enableProfileMetric) {
        this.enableProfileMetric = enableProfileMetric;
    }

    public boolean isEnableMachineMetric() {
        return enableMachineMetric;
    }

    public void setEnableMachineMetric(boolean enableMachineMetric) {
        this.enableMachineMetric = enableMachineMetric;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{\"collectors\":").append(collectors)
                .append(",\"profileInterval\":\"").append(profileInterval)
                .append(",\"machineMetricInterval\":\"").append(machineMetricInterval)
                .append(",\"enableProfileMetric\":\"").append(enableProfileMetric)
                .append(",\"enableMachineMetric\":\"").append(enableMachineMetric)
                .append("}");
        return sb.toString();
    }
}
