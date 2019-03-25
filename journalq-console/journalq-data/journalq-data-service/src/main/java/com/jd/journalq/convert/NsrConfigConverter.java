package com.jd.journalq.convert;

import com.jd.journalq.model.domain.Config;

/**
 * Created by wangxiaofei1 on 2018/12/27.
 */
public class NsrConfigConverter extends Converter<Config, com.jd.journalq.domain.Config> {

    @Override
    protected com.jd.journalq.domain.Config forward(Config config) {
        com.jd.journalq.domain.Config nsrConfig = new com.jd.journalq.domain.Config();
        nsrConfig.setGroup(config.getGroup());
        nsrConfig.setKey(config.getKey());
        nsrConfig.setValue(config.getValue());
        return nsrConfig;
    }

    @Override
    protected Config backward(com.jd.journalq.domain.Config nsrConfig) {
        Config config = new Config();
        config.setId(nsrConfig.getId());
        config.setGroup(nsrConfig.getGroup());
        config.setKey(nsrConfig.getKey());
        config.setValue(nsrConfig.getValue());
        return config;
    }
}
