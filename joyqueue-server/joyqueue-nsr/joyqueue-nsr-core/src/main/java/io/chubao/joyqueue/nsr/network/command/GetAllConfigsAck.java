package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetAllConfigsAck extends JoyQueuePayload {
    private List<Config> configs;

    public GetAllConfigsAck configs(List<Config> configs){
        this.configs = configs;
        return this;
    }

    public List<Config> getConfigs() {
        return configs;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_ALL_CONFIG_ACK;
    }
}
