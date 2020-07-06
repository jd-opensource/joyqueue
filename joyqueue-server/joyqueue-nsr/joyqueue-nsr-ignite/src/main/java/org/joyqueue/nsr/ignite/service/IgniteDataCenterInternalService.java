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
package org.joyqueue.nsr.ignite.service;

import com.google.inject.Inject;
import org.joyqueue.domain.DataCenter;
import org.joyqueue.event.DataCenterEvent;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.nsr.ignite.dao.DataCenterDao;
import org.joyqueue.nsr.ignite.message.IgniteMessenger;
import org.joyqueue.nsr.ignite.model.IgniteDataCenter;
import org.joyqueue.nsr.service.internal.DataCenterInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IgniteDataCenterInternalService implements DataCenterInternalService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private DataCenterDao dataCenterDao;
    @Inject
    protected IgniteMessenger messenger;

    public IgniteDataCenterInternalService(DataCenterDao dataCenterDao) {
        this.dataCenterDao = dataCenterDao;
    }

    @Override
    public DataCenter getById(String id) {
        return dataCenterDao.findById(id);
    }

    @Override
    public DataCenter add(DataCenter dataCenter) {
        try {
            dataCenterDao.addOrUpdate(new IgniteDataCenter(dataCenter));
            this.publishEvent(DataCenterEvent.add(dataCenter.getRegion(),dataCenter.getCode(),dataCenter.getUrl()));
            return dataCenter;
        } catch (Exception e) {
            String message = String.format("add data center.", dataCenter.toString());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }

    }

    @Override
    public DataCenter update(DataCenter dataCenter) {
        try {
            dataCenterDao.addOrUpdate(new IgniteDataCenter(dataCenter));
            this.publishEvent(DataCenterEvent.add(dataCenter.getRegion(),dataCenter.getCode(),dataCenter.getUrl()));
            return dataCenter;
        } catch (Exception e) {
            String message = String.format("add data center.", dataCenter.toString());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    public void publishEvent(MetaEvent event) {
        messenger.publish(event);
    }

    @Override
    public void delete(String id) {
        IgniteDataCenter dataCenter = dataCenterDao.findById(id);
        try {
            dataCenterDao.deleteById(id);
            this.publishEvent(DataCenterEvent.remove(dataCenter.getRegion(),dataCenter.getCode(),dataCenter.getUrl()));
        } catch (Exception e) {
            String message = String.format("delete data center.", dataCenter.toString());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }

    }

    @Override
    public List<DataCenter> getAll() {
        return convert(dataCenterDao.list(null));
    }

    private List<DataCenter> convert(List<IgniteDataCenter> dataCenters) {
        if (dataCenters == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(dataCenters);
    }

}
