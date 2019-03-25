package com.jd.journalq.service;

import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.query.QNamespace;
import com.jd.journalq.nsr.NsrService;

/**
 * 命名空间服务
 * Created by chenyanying3 on 2018-11-18.
 */
public interface NamespaceService extends NsrService<Namespace,QNamespace,String> {
    public Namespace findByCode(String code);

}
