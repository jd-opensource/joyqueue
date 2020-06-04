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
package org.joyqueue.handler.routing.command.topic;

import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.routing.command.NsrCommandSupport;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.Namespace;
import org.joyqueue.model.query.QNamespace;
import org.joyqueue.service.NamespaceService;

import java.util.ArrayList;
import java.util.List;

/**
 * 命名空间 处理器
 * Created by chenyanying3 on 2018-11-14.
 */
public class NamespaceCommand extends NsrCommandSupport<Namespace, NamespaceService, QNamespace> {

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QNamespace> qPageQuery) throws Exception {
        List<Namespace> namespaces = service.findAll();
        List<Namespace> namespaceList = new ArrayList<>();

        QNamespace qNamespace = qPageQuery.getQuery();
        if (StringUtils.isNotBlank(qNamespace.getCode()) ||
                StringUtils.isNotBlank(qNamespace.getName()) ||
                StringUtils.isNotBlank(qNamespace.getKeyword())) {
            for (Namespace namespace: namespaces) {
                if (StringUtils.isNotBlank(qNamespace.getCode()) && StringUtils.containsIgnoreCase(namespace.getCode(),qNamespace.getCode())) {
                    namespaceList.add(namespace);
                } else if (StringUtils.isNotBlank(qNamespace.getName()) && StringUtils.containsIgnoreCase(namespace.getName(),qNamespace.getName())) {
                    namespaceList.add(namespace);
                } else if (StringUtils.isNotBlank(qNamespace.getKeyword())) {
                    if (StringUtils.containsIgnoreCase(namespace.getCode(), qNamespace.getKeyword()) ||
                        StringUtils.containsIgnoreCase(namespace.getName(), qNamespace.getKeyword())) {
                        namespaceList.add(namespace);
                    }
                }
            }
        } else {
            namespaceList.addAll(namespaces);
        }
        Pagination pagination = qPageQuery.getPagination();
        pagination.setTotalRecord(namespaceList.size());

        PageResult<Namespace> result = new PageResult<>();
        result.setPagination(pagination);
        result.setResult(namespaceList);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("findAll")
    public Response findAll() throws Exception {
        return Responses.success(service.findAll());
    }

}
