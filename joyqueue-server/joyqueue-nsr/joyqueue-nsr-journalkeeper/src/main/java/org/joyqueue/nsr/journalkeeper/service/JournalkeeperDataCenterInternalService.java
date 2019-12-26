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
package org.joyqueue.nsr.journalkeeper.service;

import org.joyqueue.domain.DataCenter;
import org.joyqueue.nsr.journalkeeper.converter.DataCenterConverter;
import org.joyqueue.nsr.journalkeeper.repository.DataCenterRepository;
import org.joyqueue.nsr.service.internal.DataCenterInternalService;

import java.util.List;

/**
 * JournalkeeperDataCenterInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperDataCenterInternalService implements DataCenterInternalService {

    private DataCenterRepository dataCenterRepository;

    public JournalkeeperDataCenterInternalService(DataCenterRepository dataCenterRepository) {
        this.dataCenterRepository = dataCenterRepository;
    }

    @Override
    public List<DataCenter> getAll() {
        return DataCenterConverter.convert(dataCenterRepository.getAll());
    }

    @Override
    public DataCenter getById(String id) {
        return DataCenterConverter.convert(dataCenterRepository.getById(id));
    }

    @Override
    public DataCenter add(DataCenter dataCenter) {
        return DataCenterConverter.convert(dataCenterRepository.add(DataCenterConverter.convert(dataCenter)));
    }

    @Override
    public DataCenter update(DataCenter dataCenter) {
        return DataCenterConverter.convert(dataCenterRepository.update(DataCenterConverter.convert(dataCenter)));
    }

    @Override
    public void delete(String id) {
        dataCenterRepository.deleteById(id);
    }
}