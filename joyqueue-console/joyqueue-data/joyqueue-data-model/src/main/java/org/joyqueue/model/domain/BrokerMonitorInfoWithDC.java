package org.joyqueue.model.domain;

import org.joyqueue.monitor.BrokerMonitorInfo;

public class BrokerMonitorInfoWithDC extends BrokerMonitorInfo {
    private DataCenter dataCenter;

    public BrokerMonitorInfoWithDC(BrokerMonitorInfo info) {
        this.setBufferPoolMonitorInfo(info.getBufferPoolMonitorInfo());
        this.setConnection(info.getConnection());
        this.setDeQueue(info.getDeQueue());
        this.setElection(info.getElection());
        this.setNameServer(info.getNameServer());
        this.setReplication(info.getReplication());
        this.setStartupInfo(info.getStartupInfo());
        this.setStore(info.getStore());
        this.setEnQueue(info.getEnQueue());
    }

    public DataCenter getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter) {
        this.dataCenter = dataCenter;
    }
}
