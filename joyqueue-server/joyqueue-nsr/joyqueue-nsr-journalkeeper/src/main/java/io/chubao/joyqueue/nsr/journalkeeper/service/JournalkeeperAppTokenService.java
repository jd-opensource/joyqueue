package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.converter.AppTokenConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.AppTokenRepository;
import io.chubao.joyqueue.nsr.model.AppTokenQuery;
import io.chubao.joyqueue.nsr.service.AppTokenService;

import java.util.List;

/**
 * JournalkeeperAppTokenService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperAppTokenService implements AppTokenService {

    private AppTokenRepository appTokenRepository;

    public JournalkeeperAppTokenService(AppTokenRepository appTokenRepository) {
        this.appTokenRepository = appTokenRepository;
    }

    @Override
    public AppToken getByAppAndToken(String app, String token) {
        return AppTokenConverter.convert(appTokenRepository.getByAppAndToken(app, token));
    }

    @Override
    public AppToken getById(Long id) {
        return AppTokenConverter.convert(appTokenRepository.getById(id));
    }

    @Override
    public AppToken get(AppToken model) {
        return getById(model.getId());
    }

    @Override
    public void addOrUpdate(AppToken appToken) {
        appTokenRepository.add(AppTokenConverter.convert(appToken));
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
