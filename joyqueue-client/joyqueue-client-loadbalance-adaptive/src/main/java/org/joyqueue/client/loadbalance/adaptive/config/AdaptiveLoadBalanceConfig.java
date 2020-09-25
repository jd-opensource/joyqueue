package org.joyqueue.client.loadbalance.adaptive.config;

/**
 * AdaptiveLoadBalanceConfig
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class AdaptiveLoadBalanceConfig {

    private int ssthreshhold = 1000;
    private String[] judges;
    private int cacheInterval = 1000 * 1;
    private int sliceInterval = 1000 * 60;

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

    public int getCacheInterval() {
        return cacheInterval;
    }

    public void setCacheInterval(int cacheInterval) {
        this.cacheInterval = cacheInterval;
    }

    public int getSliceInterval() {
        return sliceInterval;
    }

    public void setSliceInterval(int sliceInterval) {
        this.sliceInterval = sliceInterval;
    }
}