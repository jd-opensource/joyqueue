package com.jd.journalq.nsr;

import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.query.QNamespace;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public interface NameSpaceServerService extends NsrService<Namespace,QNamespace,String> {

    Namespace findByCode(String code);
}
