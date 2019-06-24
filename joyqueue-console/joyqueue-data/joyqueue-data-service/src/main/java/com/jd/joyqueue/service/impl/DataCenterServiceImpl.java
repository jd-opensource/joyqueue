/**
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
package com.jd.joyqueue.service.impl;

import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.model.domain.DataCenter;
import com.jd.joyqueue.model.query.QDataCenter;
import com.jd.joyqueue.nsr.model.DataCenterQuery;
import com.jd.joyqueue.service.DataCenterService;
import com.jd.joyqueue.nsr.DataCenterNameServerService;
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
        return   dataCenterNameServerService.findAllDataCenter(null);
    }

    @Override
    public DataCenter findById(String s) throws Exception {
        return dataCenterNameServerService.findById(s);
    }

    @Override
    public PageResult<DataCenter> findByQuery(QPageQuery<QDataCenter> query) {
        try {
            return dataCenterNameServerService.findByQuery(query);
        } catch (Exception e) {
            logger.error("findByQuery exception",e);
            throw new RuntimeException("",e);
        }
    }

    @Override
    public List<DataCenter> findByQuery(QDataCenter query) {
        DataCenterQuery dataCenterQuery = new DataCenterQuery();
        if (query != null) {
            dataCenterQuery.setRegion(query.getRegion());
            dataCenterQuery.setCode(query.getCode());
        }
        try {
            return dataCenterNameServerService.findAllDataCenter(dataCenterQuery);
        } catch (Exception e) {
            logger.error("findByQuery exception",e);
            throw new RuntimeException(e);
        }
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
