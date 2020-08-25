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
package org.joyqueue.nsr.sql.converter;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.domain.AppToken;
import org.joyqueue.nsr.sql.domain.AppTokenDTO;

import java.util.Collections;
import java.util.List;

/**
 * AppTokenConverter
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class AppTokenConverter {

    public static AppTokenDTO convert(AppToken appToken) {
        if (appToken == null) {
            return null;
        }
        AppTokenDTO appTokenDTO = new AppTokenDTO();
        appTokenDTO.setId(appToken.getId());
        appTokenDTO.setApp(appToken.getApp());
        appTokenDTO.setToken(appToken.getToken());
        appTokenDTO.setEffectiveTime(appToken.getEffectiveTime());
        appTokenDTO.setExpirationTime(appToken.getExpirationTime());
        return appTokenDTO;
    }

    public static AppToken convert(AppTokenDTO appTokenDTO) {
        if (appTokenDTO == null) {
            return null;
        }
        AppToken appToken = new AppToken();
        appToken.setId(appTokenDTO.getId());
        appToken.setApp(appTokenDTO.getApp());
        appToken.setToken(appTokenDTO.getToken());
        appToken.setEffectiveTime(appTokenDTO.getEffectiveTime());
        appToken.setExpirationTime(appTokenDTO.getExpirationTime());
        return appToken;
    }

    public static List<AppToken> convert(List<AppTokenDTO> appTokenDTOList) {
        if (CollectionUtils.isEmpty(appTokenDTOList)) {
            return Collections.emptyList();
        }
        List<AppToken> result = Lists.newArrayListWithCapacity(appTokenDTOList.size());
        for (AppTokenDTO appTokenDTO : appTokenDTOList) {
            result.add(convert(appTokenDTO));
        }
        return result;
    }
}