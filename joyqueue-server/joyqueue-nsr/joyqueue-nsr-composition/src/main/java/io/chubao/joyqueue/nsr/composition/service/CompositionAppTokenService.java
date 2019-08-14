package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.model.AppTokenQuery;
import io.chubao.joyqueue.nsr.service.AppTokenService;

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
