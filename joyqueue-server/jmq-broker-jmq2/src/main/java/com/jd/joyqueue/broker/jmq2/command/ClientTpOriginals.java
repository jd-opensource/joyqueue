package com.jd.joyqueue.broker.jmq2.command;

import org.joyqueue.toolkit.stat.TPStat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClientTpAndMachineStat
 *
 * @author luoruiheng
 * @since 11/24/16
 */
public class ClientTpOriginals extends TPStat {

    private static final long serialVersionUID = 7603677380283596564L;
    
    /**
     * tp times map
     * key   - tp time digit  (Short)
     * value - tp time counts (Short)
     */
    private Map<Integer, AtomicInteger> tpTimes = new HashMap<Integer, AtomicInteger>();

    public ClientTpOriginals() {

    }

    public ClientTpOriginals(long count, long success, long error, long size, long time) {
        super(count, success, error, size, time);
    }

    public Map<Integer, AtomicInteger> getTpTimes() {
        return tpTimes;
    }

    public void setTpTimes(Map<Integer, AtomicInteger> tpTimes) {
        this.tpTimes = tpTimes;
    }

    @Override
    public String toString() {
        return "ClientTpOriginals{" +
                "tpTimes=" + tpTimes +
                '}';
    }
}
