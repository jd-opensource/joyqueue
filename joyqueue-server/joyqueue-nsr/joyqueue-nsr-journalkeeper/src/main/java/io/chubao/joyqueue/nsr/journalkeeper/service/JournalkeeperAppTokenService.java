package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.nsr.journalkeeper.converter.AppTokenConverter;
import io.chubao.joyqueue.nsr.journalkeeper.domain.AppTokenDTO;
import io.chubao.joyqueue.nsr.journalkeeper.repository.AppTokenRepository;
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
    public AppToken add(AppToken appToken) {
        AppTokenDTO appTokenDTO = appTokenRepository.add(AppTokenConverter.convert(appToken));
        return AppTokenConverter.convert(appTokenDTO);
    }

    @Override
    public AppToken update(AppToken appToken) {
        AppTokenDTO appTokenDTO = appTokenRepository.add(AppTokenConverter.convert(appToken));
        return AppTokenConverter.convert(appTokenDTO);
    }

    @Override
    public void delete(long id) {
        appTokenRepository.deleteById(id);
    }
}
