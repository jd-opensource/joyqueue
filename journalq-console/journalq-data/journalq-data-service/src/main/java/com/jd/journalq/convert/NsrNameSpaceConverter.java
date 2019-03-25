package com.jd.journalq.convert;

import com.jd.journalq.model.domain.Namespace;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public class NsrNameSpaceConverter extends Converter<Namespace, com.jd.journalq.domain.Namespace> {

    @Override
    protected com.jd.journalq.domain.Namespace forward(Namespace namespace) {
        com.jd.journalq.domain.Namespace nsrNameSpace = new com.jd.journalq.domain.Namespace();
        if (namespace.getCode() != null) {
            nsrNameSpace.setCode(namespace.getCode());
        }
        if (namespace.getName() != null) {
            nsrNameSpace.setName(namespace.getName());
        }
        return nsrNameSpace;
    }

    @Override
    protected Namespace backward(com.jd.journalq.domain.Namespace nsrNamespace) {
        Namespace namespace = new Namespace();
        namespace.setId(nsrNamespace.getCode());
        namespace.setCode(nsrNamespace.getCode());
        namespace.setName(nsrNamespace.getName());
        return namespace;
    }
}
