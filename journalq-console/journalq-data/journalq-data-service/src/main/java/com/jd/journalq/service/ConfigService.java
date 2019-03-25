package com.jd.journalq.service;

import com.jd.journalq.model.domain.Config;
import com.jd.journalq.model.query.QConfig;
import com.jd.journalq.nsr.NsrService;

/**
 * Created by wangxiaofei1 on 2018/10/17.
 */
public interface ConfigService extends NsrService<Config,QConfig,String> {

//    List<DataCenter> findAllDataCenter();

    Config findByGroupAndKey(String group, String key);
}
