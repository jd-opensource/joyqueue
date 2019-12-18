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
package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.service.internal.DataCenterInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionDataCenterInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionDataCenterInternalService implements DataCenterInternalService {

    protected final Logger logger = LoggerFactory.getLogger(CompositionDataCenterInternalService.class);

    private CompositionConfig config;
    private DataCenterInternalService igniteDataCenterService;
    private DataCenterInternalService journalkeeperDataCenterService;

    public CompositionDataCenterInternalService(CompositionConfig config, DataCenterInternalService igniteDataCenterService,
                                                DataCenterInternalService journalkeeperDataCenterService) {
        this.config = config;
        this.igniteDataCenterService = igniteDataCenterService;
        this.journalkeeperDataCenterService = journalkeeperDataCenterService;
    }

    @Override
    public DataCenter getById(String id) {
        if (config.isReadIgnite()) {
            return igniteDataCenterService.getById(id);
        } else {
            try {
                return journalkeeperDataCenterService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id: {}", id, e);
                return igniteDataCenterService.getById(id);
            }
        }
    }

    @Override
    public DataCenter add(DataCenter dataCenter) {
        DataCenter result = null;
        if (config.isWriteIgnite()) {
            result = igniteDataCenterService.add(dataCenter);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperDataCenterService.add(dataCenter);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}", dataCenter, e);
            }
        }
        return result;
    }

    @Override
    public DataCenter update(DataCenter dataCenter) {
        DataCenter result = null;
        if (config.isWriteIgnite()) {
            result = igniteDataCenterService.update(dataCenter);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperDataCenterService.update(dataCenter);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", dataCenter, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteIgnite()) {
            igniteDataCenterService.delete(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperDataCenterService.delete(id);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", id, e);
            }
        }
    }

    @Override
    public List<DataCenter> getAll() {
        if (config.isReadIgnite()) {
            return igniteDataCenterService.getAll();
        } else {
            try {
                return journalkeeperDataCenterService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return igniteDataCenterService.getAll();
            }
        }
    }
}
