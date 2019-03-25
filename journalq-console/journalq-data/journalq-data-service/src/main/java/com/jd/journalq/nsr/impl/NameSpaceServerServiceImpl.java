package com.jd.journalq.nsr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.common.model.QPageQuery;
import com.jd.journalq.convert.NsrNameSpaceConverter;
import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.domain.OperLog;
import com.jd.journalq.model.query.QNamespace;
import com.jd.journalq.nsr.model.NamespaceQuery;
import com.jd.journalq.nsr.NameServerBase;
import com.jd.journalq.nsr.NameSpaceServerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.jd.journalq.model.domain.OperLog.Type.NAMESPACE;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
@Service("nameSpaceServerService")
public class NameSpaceServerServiceImpl extends NameServerBase implements NameSpaceServerService {
    public static final String ADD_NAMESPACE="/namespace/add";
    public static final String REMOVE_NAMESPACE="/namespace/remove";
    public static final String UPDATE_NAMESPACE="/namespace/update";
    public static final String LIST_NAMESPACE="/namespace/list";
    public static final String GETBYID_NAMESPACE="/namespace/getById";
    public static final String FINDBYQUERY_NAMESPACE="/namespace/findByQuery";

    private NsrNameSpaceConverter nsrNameSpaceConverter = new NsrNameSpaceConverter();

    @Override
    public Namespace findByCode(String code) {
        QNamespace qNamespace =new QNamespace();
        qNamespace.setCode(code);
        try {
            return findByQuery(qNamespace).get(0);
        } catch (Exception e) {
            logger.error("findByCode exception",e);
        }
        return null;
    }

    @Override
    public int add(Namespace model){
        com.jd.journalq.common.domain.Namespace namespace = nsrNameSpaceConverter.convert(model);
        String result= postWithLog(ADD_NAMESPACE,namespace,NAMESPACE.value(), OperLog.OperType.ADD.value(),namespace.getCode());
        return isSuccess(result);
    }

    @Override
    public Namespace findById(String s) throws Exception {
        String result = post(GETBYID_NAMESPACE,s);
        com.jd.journalq.common.domain.Namespace namespace = JSON.parseObject(result, com.jd.journalq.common.domain.Namespace.class);
        return nsrNameSpaceConverter.revert(namespace);
    }

    @Override
    public PageResult<Namespace> findByQuery(QPageQuery<QNamespace> query) throws Exception {
        QPageQuery<NamespaceQuery> queryQPageQuery =new QPageQuery<>();
        queryQPageQuery.setPagination(query.getPagination());
        if (query.getQuery()!= null) {
            NamespaceQuery namespaceQuery = new NamespaceQuery();
            namespaceQuery.setCode(query.getQuery().getCode());
            queryQPageQuery.setQuery(namespaceQuery);
        }
        String result = post(FINDBYQUERY_NAMESPACE,queryQPageQuery);
        PageResult<com.jd.journalq.common.domain.Namespace> namespacePageResult = JSON.parseObject(result,new TypeReference<PageResult<com.jd.journalq.common.domain.Namespace>>(){});
        PageResult<Namespace> namespacePageResult1 = new PageResult<>();
        namespacePageResult1.setPagination(namespacePageResult.getPagination());
        namespacePageResult1.setResult(namespacePageResult.getResult().stream().map(namespace -> nsrNameSpaceConverter.revert(namespace)).collect(Collectors.toList()));
        return namespacePageResult1;
    }

    @Override
    public int delete(Namespace model) {
        com.jd.journalq.common.domain.Namespace namespace = nsrNameSpaceConverter.convert(model);
        String result = postWithLog(REMOVE_NAMESPACE,namespace,NAMESPACE.value(), OperLog.OperType.UPDATE.value(),namespace.getCode());
        return isSuccess(result);
    }

    @Override
    public int update(Namespace model) {
        com.jd.journalq.common.domain.Namespace namespace = nsrNameSpaceConverter.convert(model);
        String result = postWithLog(UPDATE_NAMESPACE,namespace,NAMESPACE.value(), OperLog.OperType.UPDATE.value(),namespace.getCode());
        return isSuccess(result);
    }

    @Override
    public List<Namespace> findByQuery(QNamespace query) throws Exception {
        NamespaceQuery namespaceQuery = new NamespaceQuery();
        if (query != null) {
            namespaceQuery.setCode(query.getCode());
        }
        String result = post(LIST_NAMESPACE,namespaceQuery);
        List<com.jd.journalq.common.domain.Namespace> namespaceList = JSON.parseArray(result).toJavaList(com.jd.journalq.common.domain.Namespace.class);
        return namespaceList.stream().map(namespace -> nsrNameSpaceConverter.revert(namespace)).collect(Collectors.toList());
    }
}
