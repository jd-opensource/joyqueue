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

import org.joyqueue.domain.Namespace;
import org.joyqueue.nsr.ignite.dao.NamespaceDao;
import org.joyqueue.nsr.ignite.model.IgniteNamespace;
import org.joyqueue.nsr.service.internal.NamespaceInternalService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IgniteNamespaceInternalService implements NamespaceInternalService {
    private NamespaceDao namespaceDao;

    public IgniteNamespaceInternalService(NamespaceDao namespaceDao) {
        this.namespaceDao = namespaceDao;
    }


    @Override
    public List<Namespace> getAll() {
        return convert(namespaceDao.list(null));
    }

    @Override
    public Namespace getByCode(String code) {
        return namespaceDao.findById(code);
    }

    @Override
    public Namespace getById(String id) {
        return namespaceDao.findById(id);
    }

    @Override
    public Namespace add(Namespace namespace) {
        namespaceDao.addOrUpdate(new IgniteNamespace(namespace));
        return namespace;
    }

    @Override
    public Namespace update(Namespace namespace) {
        namespaceDao.addOrUpdate(new IgniteNamespace(namespace));
        return namespace;
    }

    @Override
    public void delete(String id) {
        namespaceDao.deleteById(id);
    }

    public static List<Namespace> convert(List<IgniteNamespace> namespaces) {
        if (namespaces == null || namespaces.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(namespaces);
    }
}
