package com.jd.journalq.nsr.ignite.service;


import com.jd.journalq.domain.AppToken;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.nsr.ignite.dao.AppTokenDao;
import com.jd.journalq.nsr.ignite.model.IgniteAppToken;
import com.jd.journalq.nsr.model.AppTokenQuery;
import com.jd.journalq.nsr.service.AppTokenService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author wylixiaobin
 * 下午9:30 2018/11/26
 */
public class IgniteAppTokenService implements AppTokenService {
    private AppTokenDao appTokenDao;

    public IgniteAppTokenService(AppTokenDao appTokenDao) {
        this.appTokenDao = appTokenDao;
    }


    public IgniteAppToken toIgniteModel(AppToken model) {
        return new IgniteAppToken(model);
    }

    @Override
    public PageResult<AppToken> pageQuery(QPageQuery pageQuery) {
        return appTokenDao.pageQuery(pageQuery);
    }

    @Override
    public AppToken getById(Long id) {
        return appTokenDao.findById(id);
    }

    @Override
    public AppToken get(AppToken model) {
        return this.getById(toIgniteModel(model).getId());
    }

    @Override
    public void addOrUpdate(AppToken appToken) {
        appTokenDao.addOrUpdate(toIgniteModel(appToken));
    }

    @Override
    public void deleteById(Long id) {
        appTokenDao.deleteById(id);
    }

    @Override
    public void delete(AppToken model) {
        appTokenDao.deleteById(toIgniteModel(model).getId());
    }

    @Override
    public List<AppToken> list() {
        return this.list(null);
    }

    @Override
    public List<AppToken> list(AppTokenQuery query) {
        return convert(appTokenDao.list(query));
    }


    @Override
    public AppToken getByAppAndToken(String app, String token) {

        AppTokenQuery query = new AppTokenQuery(app, token);
        List<AppToken> list = list(query);
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
