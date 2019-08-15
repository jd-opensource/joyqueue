package io.chubao.joyqueue.nsr.ignite.service;


import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.ignite.dao.AppTokenDao;
import io.chubao.joyqueue.nsr.ignite.model.IgniteAppToken;
import io.chubao.joyqueue.nsr.model.AppTokenQuery;
import io.chubao.joyqueue.nsr.service.AppTokenService;

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
