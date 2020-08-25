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

import org.joyqueue.domain.Namespace;
import org.joyqueue.nsr.sql.converter.NamespaceConverter;
import org.joyqueue.nsr.sql.repository.NamespaceRepository;
import org.joyqueue.nsr.service.internal.NamespaceInternalService;

import java.util.List;

/**
 * SQLNamespaceInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class SQLNamespaceInternalService implements NamespaceInternalService {

    private NamespaceRepository namespaceRepository;

    public SQLNamespaceInternalService(NamespaceRepository namespaceRepository) {
        this.namespaceRepository = namespaceRepository;
    }

    @Override
    public List<Namespace> getAll() {
        return NamespaceConverter.convert(namespaceRepository.getAll());
    }

    @Override
    public Namespace getByCode(String code) {
        return NamespaceConverter.convert(namespaceRepository.getByCode(code));
    }

    @Override
    public Namespace getById(String id) {
        return NamespaceConverter.convert(namespaceRepository.getById(id));
    }

    @Override
    public Namespace add(Namespace namespace) {
        return NamespaceConverter.convert(namespaceRepository.add(NamespaceConverter.convert(namespace)));
    }

    @Override
    public Namespace update(Namespace namespace) {
        return NamespaceConverter.convert(namespaceRepository.update(NamespaceConverter.convert(namespace)));
    }

    @Override
    public void delete(String id) {
        namespaceRepository.deleteById(id);
    }
}