package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.query.QNamespace;
import io.chubao.joyqueue.nsr.NsrService;

/**
 * 命名空间服务
 * Created by chenyanying3 on 2018-11-18.
 */
public interface NamespaceService extends NsrService<Namespace,QNamespace,String> {
    Namespace findByCode(String code);

}
