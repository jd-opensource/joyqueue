package io.chubao.joyqueue.convert;

import io.chubao.joyqueue.model.domain.Config;

/**
 * Created by wangxiaofei1 on 2018/12/27.
 */
public class NsrConfigConverter extends Converter<Config, io.chubao.joyqueue.domain.Config> {

    @Override
    protected io.chubao.joyqueue.domain.Config forward(Config config) {
        io.chubao.joyqueue.domain.Config nsrConfig = new io.chubao.joyqueue.domain.Config();
        nsrConfig.setGroup(config.getGroup());
        nsrConfig.setKey(config.getKey());
        nsrConfig.setValue(config.getValue());
        return nsrConfig;
    }

    @Override
    protected Config backward(io.chubao.joyqueue.domain.Config nsrConfig) {
        Config config = new Config();
        config.setId(nsrConfig.getId());
        config.setGroup(nsrConfig.getGroup());
        config.setKey(nsrConfig.getKey());
        config.setValue(nsrConfig.getValue());
        return config;
    }
}
