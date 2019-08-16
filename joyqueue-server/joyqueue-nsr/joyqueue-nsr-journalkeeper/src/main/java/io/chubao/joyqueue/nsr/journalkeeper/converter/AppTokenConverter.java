package io.chubao.joyqueue.nsr.journalkeeper.converter;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.nsr.journalkeeper.domain.AppTokenDTO;
import org.apache.commons.collections.CollectionUtils;

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