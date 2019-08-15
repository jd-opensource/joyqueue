package io.chubao.joyqueue.convert;

import io.chubao.joyqueue.model.domain.Namespace;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public class NsrNameSpaceConverter extends Converter<Namespace, io.chubao.joyqueue.domain.Namespace> {

    @Override
    protected io.chubao.joyqueue.domain.Namespace forward(Namespace namespace) {
        io.chubao.joyqueue.domain.Namespace nsrNameSpace = new io.chubao.joyqueue.domain.Namespace();
        if (namespace.getCode() != null) {
            nsrNameSpace.setCode(namespace.getCode());
        }
        if (namespace.getName() != null) {
            nsrNameSpace.setName(namespace.getName());
        }
        return nsrNameSpace;
    }

    @Override
    protected Namespace backward(io.chubao.joyqueue.domain.Namespace nsrNamespace) {
        Namespace namespace = new Namespace();
        namespace.setId(nsrNamespace.getCode());
        namespace.setCode(nsrNamespace.getCode());
        namespace.setName(nsrNamespace.getName());
        return namespace;
    }
}
