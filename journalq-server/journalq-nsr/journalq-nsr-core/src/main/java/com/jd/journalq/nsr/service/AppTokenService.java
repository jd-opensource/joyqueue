package com.jd.journalq.nsr.service;


import com.jd.journalq.domain.AppToken;
import com.jd.journalq.nsr.model.AppTokenQuery;

/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface AppTokenService extends DataService<AppToken, AppTokenQuery, Long> {

    /**
     * 根据app和token查询数据
     *
     * @param app
     * @param token
     * @return
     */
    AppToken getByAppAndToken(String app, String token);
}
