package org.joyqueue.client.loadbalance.adaptive.config;

/**
 * AdaptiveLoadBalanceConfig
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class AdaptiveLoadBalanceConfig {

    private int ssthreshhold = 10;
    private String[] judges;
    private int computeInterval = 1000 * 1;

    public int getSsthreshhold() {
        return ssthreshhold;
    }

    public void setSsthreshhold(int ssthreshhold) {
        this.ssthreshhold = ssthreshhold;
    }

    public String[] getJudges() {
        return judges;
    }

    public void setJudges(String[] judges) {
        this.judges = judges;
    }

    public int getComputeInterval() {
        return computeInterval;
    }

    public void setComputeInterval(int computeInterval) {
        this.computeInterval = computeInterval;
    }
}