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
package org.joyqueue.service.impl;

import com.alibaba.fastjson.JSON;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.OperLog;
import org.joyqueue.nsr.NsrServiceProvider;
import org.joyqueue.service.NameServerService;
import org.joyqueue.service.OperLogService;
import org.joyqueue.toolkit.security.EscapeUtils;
import org.joyqueue.util.HttpUtil;
import org.joyqueue.util.LocalSession;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * NameServer 接口
 * Created by chenyanying3 on 2018-10-23.
 */
@Service("nameServerService")
public class NameServerServiceImpl implements NameServerService {
    private final Logger logger = LoggerFactory.getLogger(NameServerServiceImpl.class);
    private final String SPLICE = ".";


    @Autowired
    private OperLogService operLogService;

    @Autowired
    private NsrServiceProvider nsrHostProvider;

    /**
     * 带操作日志请求
     * @param uri
     * @param obj
     * @param type
     * @param identity
     * @return
     */
    private String postWithLog(String uri, Object obj, Integer type, Integer operType, String identity) {
        OperLog operLog = null;
        String result = null;
        StringBuilder target = new StringBuilder();
        //组装数据
        try {
            //记录操作日志
            operLog = new OperLog();
            Long id = LocalSession.getSession().getUser().getId();
            String code = LocalSession.getSession().getUser().getCode();
            operLog.setCreateBy(new Identity(id, code));
            operLog.setCreateTime(new Date());
            operLog.setUpdateBy(new Identity(id, code));
            operLog.setCreateTime(new Date());
            operLog.setType(type);
            operLog.setOperType(operType);
            operLog.setIdentity(identity);
            target.append(uri).append(",").append(JSON.toJSONString(obj));
            //执行请求
            result = post(uri, obj);
            //操作结果记录到数据库
            target.append(",").append(result);
        } catch (Exception e) {
            target.append(",").append(e.getMessage());
            logger.error("post exception", e);
            throw new RuntimeException("post exception", e);
        } finally {
            //执行记录日志
            try {
                //最长200
                String targetStr = target.toString();
                if (targetStr.length() > 500) {
                    targetStr = targetStr.substring(0, 490);
                }
                operLog.setTarget(targetStr);
                operLogService.add(operLog);
            } catch (Exception e) {
                logger.error("operLogService add", e);
            }
        }
        return result;
    }

    /**
     * POST 公共方法
     * @param
     * @param uri
     * @param obj
     * @return
     * @throws Exception
     */
    private String post(String uri, Object obj) throws Exception {
        HttpPost post = new HttpPost(nsrHostProvider.getBaseUrl() + uri);
        if (null != obj) {
            StringEntity entity = new StringEntity(JSON.toJSONString(obj), Charsets.UTF_8);//解决中文乱码问题
            entity.setContentEncoding(CharEncoding.UTF_8);
            entity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            post.setEntity(entity);
        }
        return HttpUtil.request(post);
    }

    @Deprecated
    private String onResponse(CloseableHttpResponse response, HttpUriRequest request) throws Exception {
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK != statusCode) {
                String message = String.format("monitorUrl [%s],reuqest[%s] error code [%s],response[%s]",
                        request.getURI().toString(), request.toString(), statusCode, EntityUtils.toString(response.getEntity()));
                throw new Exception(message);
            }
            String result = EntityUtils.toString(response.getEntity());
            logger.info("request[{}] response[{}]", request.toString(), result);

            return result;
        } finally {
            response.close();
        }
    }

    private String getEscapeTopic(String topic) {
        return EscapeUtils.escape(topic, "/", "^");
    }

}
