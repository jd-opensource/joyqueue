/**
 * Copyright 2018 The JoyQueue Authors.
 *
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
package io.chubao.joyqueue.nsr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.convert.NsrNameSpaceConverter;
import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.domain.OperLog;
import io.chubao.joyqueue.model.query.QNamespace;
import io.chubao.joyqueue.nsr.model.NamespaceQuery;
import io.chubao.joyqueue.nsr.NameServerBase;
import io.chubao.joyqueue.nsr.NameSpaceServerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static io.chubao.joyqueue.model.domain.OperLog.Type.NAMESPACE;

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
        io.chubao.joyqueue.domain.Namespace namespace = nsrNameSpaceConverter.convert(model);
        String result= postWithLog(ADD_NAMESPACE,namespace,NAMESPACE.value(), OperLog.OperType.ADD.value(),namespace.getCode());
        return isSuccess(result);
    }

    @Override
    public Namespace findById(String s) throws Exception {
        String result = post(GETBYID_NAMESPACE,s);
        io.chubao.joyqueue.domain.Namespace namespace = JSON.parseObject(result, io.chubao.joyqueue.domain.Namespace.class);
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
        PageResult<io.chubao.joyqueue.domain.Namespace> namespacePageResult = JSON.parseObject(result,new TypeReference<PageResult<io.chubao.joyqueue.domain.Namespace>>(){});
        PageResult<Namespace> namespacePageResult1 = new PageResult<>();
        namespacePageResult1.setPagination(namespacePageResult.getPagination());
        namespacePageResult1.setResult(namespacePageResult.getResult().stream().map(namespace -> nsrNameSpaceConverter.revert(namespace)).collect(Collectors.toList()));
        return namespacePageResult1;
    }

    @Override
    public int delete(Namespace model) {
        io.chubao.joyqueue.domain.Namespace namespace = nsrNameSpaceConverter.convert(model);
        String result = postWithLog(REMOVE_NAMESPACE,namespace,NAMESPACE.value(), OperLog.OperType.UPDATE.value(),namespace.getCode());
        return isSuccess(result);
    }

    @Override
    public int update(Namespace model) {
        io.chubao.joyqueue.domain.Namespace namespace = nsrNameSpaceConverter.convert(model);
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
        List<io.chubao.joyqueue.domain.Namespace> namespaceList = JSON.parseArray(result).toJavaList(io.chubao.joyqueue.domain.Namespace.class);
        return namespaceList.stream().map(namespace -> nsrNameSpaceConverter.revert(namespace)).collect(Collectors.toList());
    }
}
