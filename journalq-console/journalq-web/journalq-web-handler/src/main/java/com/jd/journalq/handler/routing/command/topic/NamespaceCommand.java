package com.jd.journalq.handler.routing.command.topic;

import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.handler.routing.command.NsrCommandSupport;
import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.query.QNamespace;
import com.jd.journalq.service.NamespaceService;
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
