/**
 *  Copyright [2020] JD.com, Inc. TIG. ChubaoStream team.
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
package org.joyqueue.nsr.composition.service;

import org.joyqueue.domain.DataCenter;
import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.service.internal.DataCenterInternalService;
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
    private DataCenterInternalService sourceDataCenterService;
    private DataCenterInternalService targetDataCenterService;

    public CompositionDataCenterInternalService(CompositionConfig config, DataCenterInternalService sourceDataCenterService,
                                                DataCenterInternalService targetDataCenterService) {
        this.config = config;
        this.sourceDataCenterService = sourceDataCenterService;
        this.targetDataCenterService = targetDataCenterService;
    }

    @Override
    public DataCenter getById(String id) {
        if (config.isReadSource()) {
            return sourceDataCenterService.getById(id);
        } else {
            try {
                return targetDataCenterService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id: {}", id, e);
                return sourceDataCenterService.getById(id);
            }
        }
    }

    @Override
    public DataCenter add(DataCenter dataCenter) {
        DataCenter result = null;
        if (config.isWriteSource()) {
            result = sourceDataCenterService.add(dataCenter);
        }
        if (config.isWriteTarget()) {
            try {
                targetDataCenterService.add(dataCenter);
            } catch (Exception e) {
                logger.error("add exception, params: {}", dataCenter, e);
            }
        }
        return result;
    }

    @Override
    public DataCenter update(DataCenter dataCenter) {
        DataCenter result = null;
        if (config.isWriteSource()) {
            result = sourceDataCenterService.update(dataCenter);
        }
        if (config.isWriteTarget()) {
            try {
                targetDataCenterService.update(dataCenter);
            } catch (Exception e) {
                logger.error("update exception, params: {}", dataCenter, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteSource()) {
            sourceDataCenterService.delete(id);
        }
        if (config.isWriteTarget()) {
            try {
                targetDataCenterService.delete(id);
            } catch (Exception e) {
                logger.error("delete exception, params: {}", id, e);
            }
        }
    }

    @Override
    public List<DataCenter> getAll() {
        if (config.isReadSource()) {
            return sourceDataCenterService.getAll();
        } else {
            try {
                return targetDataCenterService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return sourceDataCenterService.getAll();
            }
        }
    }
}
