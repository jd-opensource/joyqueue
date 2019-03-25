package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.domain.Config;
import com.jd.journalq.common.network.transport.command.JMQPayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetAllConfigsAck extends JMQPayload {
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
