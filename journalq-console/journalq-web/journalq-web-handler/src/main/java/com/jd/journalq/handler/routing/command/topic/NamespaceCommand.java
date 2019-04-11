/**
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
package com.jd.journalq.handler.routing.command.topic;

import com.jd.journalq.handler.routing.command.NsrCommandSupport;
import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.query.QNamespace;
import com.jd.journalq.service.NamespaceService;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

/**
 * 命名空间 处理器
 * Created by chenyanying3 on 2018-11-14.
 */
public class NamespaceCommand extends NsrCommandSupport<Namespace, NamespaceService, QNamespace> {

    @Path("findAll")
    public Response findAll() throws Exception {
        return Responses.success(service.findByQuery(new QNamespace()));
    }

}
