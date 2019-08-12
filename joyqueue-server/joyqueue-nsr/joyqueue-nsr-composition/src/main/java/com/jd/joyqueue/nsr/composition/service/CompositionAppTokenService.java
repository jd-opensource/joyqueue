package com.jd.joyqueue.nsr.composition.service;

import com.jd.joyqueue.nsr.composition.config.CompositionConfig;
import com.jd.joyqueue.domain.AppToken;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.nsr.model.AppTokenQuery;
import com.jd.joyqueue.nsr.service.AppTokenService;

import java.util.List;

/**
 * CompositionAppTokenService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionAppTokenService implements AppTokenService {

    private CompositionConfig config;
    private AppTokenService igniteAppTokenService;
    private AppTokenService journalkeeperAppTokenService;

    public CompositionAppTokenService(CompositionConfig config, AppTokenService igniteAppTokenService,
                                      AppTokenService journalkeeperAppTokenService) {
        this.config = config;
        this.igniteAppTokenService = igniteAppTokenService;
        this.journalkeeperAppTokenService = journalkeeperAppTokenService;
    }

    @Override
    public AppToken getByAppAndToken(String app, String token) {
        return null;
    }

    @Override
    public AppToken getById(Long id) {
        return null;
    }

    @Override
    public AppToken get(AppToken model) {
        return null;
    }

    @Override
    public void addOrUpdate(AppToken appToken) {

    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public void delete(AppToken model) {

    }

    @Override
    public List<AppToken> list() {
        return null;
    }

    @Override
    public List<AppToken> list(AppTokenQuery query) {
        return null;
    }

    @Override
    public PageResult<AppToken> pageQuery(QPageQuery<AppTokenQuery> pageQuery) {
        return null;
    }
}
