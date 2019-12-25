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
package org.joyqueue.nsr.ignite.service;


import org.joyqueue.domain.AppToken;
import org.joyqueue.nsr.ignite.dao.AppTokenDao;
import org.joyqueue.nsr.ignite.model.IgniteAppToken;
import org.joyqueue.nsr.model.AppTokenQuery;
import org.joyqueue.nsr.service.internal.AppTokenInternalService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author wylixiaobin
 * 下午9:30 2018/11/26
 */
public class IgniteAppTokenInternalService implements AppTokenInternalService {
    private AppTokenDao appTokenDao;

    public IgniteAppTokenInternalService(AppTokenDao appTokenDao) {
        this.appTokenDao = appTokenDao;
    }


    public IgniteAppToken toIgniteModel(AppToken model) {
        return new IgniteAppToken(model);
    }

    @Override
    public AppToken getById(long id) {
        return appTokenDao.findById(id);
    }

    @Override
    public AppToken add(AppToken appToken) {
        appTokenDao.add(toIgniteModel(appToken));
        return appToken;
    }

    @Override
    public List<AppToken> getAll() {
        return convert(appTokenDao.list(null));
    }

    @Override
    public AppToken update(AppToken appToken) {
        return null;
    }

    @Override
    public void delete(long id) {
        appTokenDao.deleteById(id);
    }

    @Override
    public List<AppToken> getByApp(String app) {
        AppTokenQuery appTokenQuery = new AppTokenQuery();
        appTokenQuery.setApp(app);
        return convert(appTokenDao.list(appTokenQuery));
    }

    @Override
    public AppToken getByAppAndToken(String app, String token) {
        AppTokenQuery query = new AppTokenQuery(app, token);
        List<AppToken> list = convert(appTokenDao.list(query));
        if (null == list || list.size() < 1) {
            return null;
        }

        if (list.size() > 1) {
            throw new IllegalStateException("duplicated app token");
        }
        return list.get(0);
    }

    List<AppToken> convert(List<IgniteAppToken> iAppTokens) {
        if (iAppTokens == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(iAppTokens);
    }
}
