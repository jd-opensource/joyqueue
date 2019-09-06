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
package io.chubao.joyqueue.handler.routing.command.topic;

import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.chubao.joyqueue.handler.annotation.PageQuery;
import io.chubao.joyqueue.handler.routing.command.NsrCommandSupport;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.Pagination;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.query.QNamespace;
import io.chubao.joyqueue.service.NamespaceService;

import java.util.List;

/**
 * 命名空间 处理器
 * Created by chenyanying3 on 2018-11-14.
 */
public class NamespaceCommand extends NsrCommandSupport<Namespace, NamespaceService, QNamespace> {

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QNamespace> qPageQuery) throws Exception {
        List<Namespace> namespaces = service.findAll();

        Pagination pagination = qPageQuery.getPagination();
        pagination.setTotalRecord(namespaces.size());

        PageResult<Namespace> result = new PageResult();
        result.setPagination(pagination);
        result.setResult(namespaces);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("findAll")
    public Response findAll() throws Exception {
        return Responses.success(service.findAll());
    }

}
