/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.service.impl;

import org.joyqueue.model.domain.DataCenter;
import org.joyqueue.nsr.DataCenterNameServerService;
import org.joyqueue.service.DataCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/12/27.
 */
@Service("dataCenterService")
public class DataCenterServiceImpl implements DataCenterService {
    private static final Logger logger = LoggerFactory.getLogger(DataCenterServiceImpl.class);
    @Autowired
    private DataCenterNameServerService dataCenterNameServerService;

    @Override
    public List<DataCenter> findAllDataCenter() throws Exception {
        return dataCenterNameServerService.findAllDataCenter();
    }

    @Override
    public DataCenter findByIp(String ip) throws Exception {
        return dataCenterNameServerService.findByIp(ip);
    }

    @Override
    public List<DataCenter> findByIps(List<String> ips) throws Exception {
        return dataCenterNameServerService.findByIps(ips);
    }

    @Override
    public DataCenter findById(String s) throws Exception {
        return dataCenterNameServerService.findById(s);
    }

    @Override
    public int add(DataCenter model) {
        try {
            return dataCenterNameServerService.add(model);
        } catch (Exception e) {
            logger.error("add exception",e);
        }
        return 0;
    }

    @Override
    public int delete(DataCenter model) {
        try {
            return dataCenterNameServerService.delete(model);
        } catch (Exception e) {
            logger.error("delete exception",e);
        }
        return 0;
    }

    @Override
    public int update(DataCenter model) {
        try {
           return dataCenterNameServerService.update(model);
        } catch (Exception e) {
            logger.error("update exception",e);
        }
        return 0;
    }

}
