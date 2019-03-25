package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.domain.DataCenter;
import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetDataCenterAck extends JMQPayload {
    private DataCenter dataCenter;

    public GetDataCenterAck dataCenter(DataCenter dataCenter){
        this.dataCenter = dataCenter;
        return this;
    }

    public DataCenter getDataCenter() {
        return dataCenter;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_DATACENTER_ACK;
    }
}
