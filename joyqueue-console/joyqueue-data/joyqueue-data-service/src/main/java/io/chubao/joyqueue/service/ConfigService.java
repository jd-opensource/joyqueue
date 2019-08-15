package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.Config;
import io.chubao.joyqueue.model.query.QConfig;
import io.chubao.joyqueue.nsr.NsrService;

/**
 * Created by wangxiaofei1 on 2018/10/17.
 */
public interface ConfigService extends NsrService<Config,QConfig,String> {

//    List<DataCenter> findAllDataCenter();

    Config findByGroupAndKey(String group, String key);
}
