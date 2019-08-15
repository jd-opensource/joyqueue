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
package io.chubao.joyqueue.nsr.ignite.service;

import io.chubao.joyqueue.domain.Namespace;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.ignite.dao.NamespaceDao;
import io.chubao.joyqueue.nsr.ignite.model.IgniteNamespace;
import io.chubao.joyqueue.nsr.model.NamespaceQuery;
import io.chubao.joyqueue.nsr.service.NamespaceService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IgniteNamespaceService implements NamespaceService {
    private NamespaceDao namespaceDao;

    public IgniteNamespaceService(NamespaceDao namespaceDao) {
        this.namespaceDao = namespaceDao;
    }

    @Override
    public Namespace getById(String id) {
        return namespaceDao.findById(id);
    }

    @Override
    public Namespace get(Namespace model) {
        return namespaceDao.findById(model.getCode());
    }

    @Override
    public void addOrUpdate(Namespace namespace) {
        namespaceDao.addOrUpdate(new IgniteNamespace(namespace));
    }

    @Override
    public void deleteById(String id) {
        namespaceDao.deleteById(id);
    }

    @Override
    public void delete(Namespace model) {
        namespaceDao.deleteById(model.getCode());
    }

    @Override
    public List<Namespace> list() {
        return this.list(null);
    }

    @Override
    public List<Namespace> list(NamespaceQuery query) {
        return convert(namespaceDao.list(query));
    }

    @Override
    public PageResult<Namespace> pageQuery(QPageQuery<NamespaceQuery> pageQuery) {
        PageResult<IgniteNamespace> pageResult = namespaceDao.pageQuery(pageQuery);

        return new PageResult<>(pageResult.getPagination(), convert(pageResult.getResult()));
    }

    public static List<Namespace> convert(List<IgniteNamespace> namespaces) {
        if (namespaces == null || namespaces.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(namespaces);
    }
}
