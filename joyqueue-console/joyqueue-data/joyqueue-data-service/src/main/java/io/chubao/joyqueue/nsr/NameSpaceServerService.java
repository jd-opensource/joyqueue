package io.chubao.joyqueue.nsr;

import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.query.QNamespace;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public interface NameSpaceServerService extends NsrService<Namespace,QNamespace,String> {

    Namespace findByCode(String code);
}
