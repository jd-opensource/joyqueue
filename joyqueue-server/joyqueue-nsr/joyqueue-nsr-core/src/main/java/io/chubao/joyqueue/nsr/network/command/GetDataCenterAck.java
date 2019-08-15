package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetDataCenterAck extends JoyQueuePayload {
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
