/**
 * Copyright 2019 The JoyQueue Authors.
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
package org.joyqueue.nsr.impl;

import com.alibaba.fastjson.JSON;
import org.joyqueue.convert.NsrNameSpaceConverter;
import org.joyqueue.model.domain.Namespace;
import org.joyqueue.model.domain.OperLog;
import org.joyqueue.nsr.NameServerBase;
import org.joyqueue.nsr.NameSpaceServerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.joyqueue.model.domain.OperLog.Type.NAMESPACE;

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
    public static final String GETBYCODE_NAMESPACE="/namespace/getById";

    private NsrNameSpaceConverter nsrNameSpaceConverter = new NsrNameSpaceConverter();

    @Override
    public Namespace findByCode(String code) throws Exception {
        String result = post(GETBYCODE_NAMESPACE, code);
        org.joyqueue.domain.Namespace namespace = JSON.parseObject(result, org.joyqueue.domain.Namespace.class);
        return nsrNameSpaceConverter.revert(namespace);
    }

    @Override
    public int add(Namespace model) throws Exception {
        org.joyqueue.domain.Namespace namespace = nsrNameSpaceConverter.convert(model);
        String result= postWithLog(ADD_NAMESPACE,namespace,NAMESPACE.value(), OperLog.OperType.ADD.value(),namespace.getCode());
        return isSuccess(result);
    }

    @Override
    public Namespace findById(String s) throws Exception {
        String result = post(GETBYID_NAMESPACE,s);
        org.joyqueue.domain.Namespace namespace = JSON.parseObject(result, org.joyqueue.domain.Namespace.class);
        return nsrNameSpaceConverter.revert(namespace);
    }

    @Override
    public int delete(Namespace model) throws Exception {
        org.joyqueue.domain.Namespace namespace = nsrNameSpaceConverter.convert(model);
        String result = postWithLog(REMOVE_NAMESPACE,namespace,NAMESPACE.value(), OperLog.OperType.UPDATE.value(),namespace.getCode());
        return isSuccess(result);
    }

    @Override
    public int update(Namespace model) throws Exception {
        org.joyqueue.domain.Namespace namespace = nsrNameSpaceConverter.convert(model);
        String result = postWithLog(UPDATE_NAMESPACE,namespace,NAMESPACE.value(), OperLog.OperType.UPDATE.value(),namespace.getCode());
        return isSuccess(result);
    }

    @Override
    public List<Namespace> findAll() throws Exception {
        String result = post(LIST_NAMESPACE, null);
        List<org.joyqueue.domain.Namespace> namespaceList = JSON.parseArray(result).toJavaList(org.joyqueue.domain.Namespace.class);
        return namespaceList.stream().map(namespace -> nsrNameSpaceConverter.revert(namespace)).collect(Collectors.toList());
    }
}
