package io.chubao.joyqueue.broker.limit.config;

/**
 * RateLimiterConfig
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/17
 */
public class LimiterConfig {

    private int tps;
    private int traffic;

    public LimiterConfig() {

    }

    public LimiterConfig(int tps, int traffic) {
        this.tps = tps;
        this.traffic = traffic;
    }

    public int getTps() {
        return tps;
    }

    public void setTps(int tps) {
        this.tps = tps;
    }

    public int getTraffic() {
        return traffic;
    }

    public void setTraffic(int traffic) {
        this.traffic = traffic;
    }
}