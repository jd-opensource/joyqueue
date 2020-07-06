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
import org.joyqueue.convert.NsrAppTokenConverter;
import org.joyqueue.domain.AppToken;
import org.joyqueue.model.domain.ApplicationToken;
import org.joyqueue.model.domain.OperLog;
import org.joyqueue.nsr.AppTokenNameServerService;
import org.joyqueue.nsr.NameServerBase;
import org.joyqueue.nsr.model.AppTokenQuery;
import org.joyqueue.toolkit.time.SystemClock;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
@Service("appTokenNameServerService")
public class AppTokenNameServerServiceImpl extends NameServerBase implements AppTokenNameServerService {

    public static final String ADD_TOKEN="/apptoken/add";
    public static final String UPDATE_TOKEN="/apptoken/update";
    public static final String REMOVE_TOKEN="/apptoken/remove";
    public static final String GETBYID_TOKEN="/apptoken/getById";
    public static final String GETBYAPP_TOKEN="/apptoken/findByApp";
    public static final String GETBYAPPANDTOKEN_TOKEN="/apptoken/findByAppAndToken";

    private NsrAppTokenConverter nsrAppTokenConverter = new NsrAppTokenConverter();

    @Override
    public ApplicationToken findById(Long  id) throws Exception {
        String result = post(GETBYID_TOKEN,Long.valueOf(id));
        AppToken appToken = JSON.parseObject(result,AppToken.class);
        return nsrAppTokenConverter.convert(appToken);
    }

    @Override
    public int add(ApplicationToken applicationToken) throws Exception {
        Long id = Long.valueOf(String.valueOf(applicationToken.getApplication().getId())+ String.valueOf(SystemClock.now()/1000));
        applicationToken.setId(id);
        AppToken appToken = nsrAppTokenConverter.revert(applicationToken);
        String result = postWithLog(ADD_TOKEN, appToken,OperLog.Type.APP_TOKEN.value(),OperLog.OperType.ADD.value(),appToken.getApp());
        return isSuccess(result);
    }

    @Override
    public int update(ApplicationToken token) throws Exception {
        String result = post(GETBYID_TOKEN,token.getId());
        AppToken nsrToken = JSON.parseObject(result, AppToken.class);

        if (nsrToken == null) {
            nsrToken = new AppToken();
        }
        nsrToken.setId(token.getId());
        if (token.getApplication().getCode() != null) {
            nsrToken.setApp(token.getApplication().getCode());
        }
        if (token.getToken() != null) {
            nsrToken.setToken(token.getToken());
        }
        if (token.getEffectiveTime() != null) {
            nsrToken.setEffectiveTime(token.getEffectiveTime());
        }
        if (token.getExpirationTime() != null) {
            nsrToken.setExpirationTime(token.getExpirationTime());
        }
        String success =  postWithLog(UPDATE_TOKEN, nsrToken,OperLog.Type.APP_TOKEN.value(),OperLog.OperType.UPDATE.value(),nsrToken.getApp());
        return isSuccess(success);
    }

    @Override
    public int delete(ApplicationToken applicationToken) throws Exception {
        AppToken nsrToken = nsrAppTokenConverter.revert(applicationToken);
        String result = postWithLog(REMOVE_TOKEN, nsrToken,OperLog.Type.APP_TOKEN.value(),OperLog.OperType.DELETE.value(),nsrToken.getApp());
        return isSuccess(result);
    }

    @Override
    public List<ApplicationToken> findByApp(String app) throws Exception {
        AppTokenQuery appTokenQuery = new AppTokenQuery();
        appTokenQuery.setApp(app);
        String result = post(GETBYAPP_TOKEN, appTokenQuery);
        List<AppToken> appTokenList = JSON.parseArray(result,AppToken.class);
        return appTokenList.stream().map(appToken -> nsrAppTokenConverter.convert(appToken)).collect(Collectors.toList());
    }

    @Override
    public ApplicationToken findByAppAndToken(String app, String token) throws Exception {
        AppTokenQuery appTokenQuery = new AppTokenQuery();
        appTokenQuery.setApp(app);
        appTokenQuery.setToken(token);
        String result = post(GETBYAPPANDTOKEN_TOKEN, appTokenQuery);
        AppToken appToken = JSON.parseObject(result,AppToken.class);
        return nsrAppTokenConverter.convert(appToken);
    }

}
