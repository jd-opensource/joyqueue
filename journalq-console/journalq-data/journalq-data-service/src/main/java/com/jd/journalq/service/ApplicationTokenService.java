package com.jd.journalq.service;

import com.jd.journalq.model.domain.ApplicationToken;
import com.jd.journalq.model.query.QApplicationToken;
import com.jd.journalq.nsr.NsrService;

import java.util.List;

/**
 * Created by yangyang115 on 18-9-6.
 */
public interface ApplicationTokenService extends NsrService<ApplicationToken,QApplicationToken,Long> {

    /**
     * 统计应用令牌次数
     *
     * @param appId
     * @return
     */
    int countByAppId(long appId);

    /**
     * 根据应用的id 查询 应用的token信息
     *
     * @param appId
     * @return
     */
    List<ApplicationToken> findByAppId(long appId);

    /**
     * 根据应用id+token 查询 token信息
     *
     * @param app
     * @param token
     * @return
     */
    ApplicationToken findByAppAndToken(String app, String token);
}
