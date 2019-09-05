package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.nsr.journalkeeper.converter.AppTokenConverter;
import io.chubao.joyqueue.nsr.journalkeeper.domain.AppTokenDTO;
import io.chubao.joyqueue.nsr.journalkeeper.repository.AppTokenRepository;
import io.chubao.joyqueue.nsr.service.internal.AppTokenInternalService;

import java.util.List;

/**
 * JournalkeeperAppTokenInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperAppTokenInternalService implements AppTokenInternalService {

    private AppTokenRepository appTokenRepository;

    public JournalkeeperAppTokenInternalService(AppTokenRepository appTokenRepository) {
        this.appTokenRepository = appTokenRepository;
    }

    @Override
    public AppToken getById(long id) {
        return AppTokenConverter.convert(appTokenRepository.getById(id));
    }

    @Override
    public AppToken getByAppAndToken(String app, String token) {
        return AppTokenConverter.convert(appTokenRepository.getByAppAndToken(app, token));
    }

    @Override
    public List<AppToken> getByApp(String app) {
        return AppTokenConverter.convert(appTokenRepository.getByApp(app));
    }

    @Override
    public List<AppToken> getAll() {
        return AppTokenConverter.convert(appTokenRepository.getAll());
    }

    @Override
    public AppToken add(AppToken appToken) {
        AppTokenDTO appTokenDTO = appTokenRepository.add(AppTokenConverter.convert(appToken));
        return AppTokenConverter.convert(appTokenDTO);
    }

    @Override
    public AppToken update(AppToken appToken) {
        AppTokenDTO appTokenDTO = appTokenRepository.update(AppTokenConverter.convert(appToken));
        return AppTokenConverter.convert(appTokenDTO);
    }

    @Override
    public void delete(long id) {
        appTokenRepository.deleteById(id);
    }
}
