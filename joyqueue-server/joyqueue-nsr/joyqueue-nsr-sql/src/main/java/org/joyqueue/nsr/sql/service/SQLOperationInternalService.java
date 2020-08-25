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
package org.joyqueue.nsr.sql.service;

import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.nsr.sql.repository.BaseRepository;
import org.joyqueue.nsr.service.internal.OperationInternalService;

import java.util.List;

/**
 * SQLOperationInternalService
 * author: gaohaoxiang
 * date: 2019/9/6
 */
public class SQLOperationInternalService implements OperationInternalService {

    private BaseRepository baseRepository;

    public SQLOperationInternalService(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    @Override
    public Object query(String operator, List<Object> params) {
        if (CollectionUtils.isEmpty(params)) {
            return baseRepository.query(operator);
        } else {
            return baseRepository.query(operator, params.toArray(new Object[0]));
        }
    }

    @Override
    public Object insert(String operator, List<Object> params) {
        if (CollectionUtils.isEmpty(params)) {
            return baseRepository.insert(operator);
        } else {
            return baseRepository.insert(operator, params.toArray(new Object[0]));
        }
    }

    @Override
    public Object update(String operator, List<Object> params) {
        if (CollectionUtils.isEmpty(params)) {
            return baseRepository.update(operator);
        } else {
            return baseRepository.update(operator, params.toArray(new Object[0]));
        }
    }

    @Override
    public Object delete(String operator, List<Object> params) {
        if (CollectionUtils.isEmpty(params)) {
            return baseRepository.delete(operator);
        } else {
            return baseRepository.delete(operator, params.toArray(new Object[0]));
        }
    }
}