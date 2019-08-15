package io.chubao.joyqueue.handler.routing.command.topic;

import io.chubao.joyqueue.handler.routing.command.NsrCommandSupport;
import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.query.QNamespace;
import io.chubao.joyqueue.service.NamespaceService;
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
