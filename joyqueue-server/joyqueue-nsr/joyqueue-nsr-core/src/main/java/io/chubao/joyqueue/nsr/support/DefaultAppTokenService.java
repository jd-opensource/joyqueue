package io.chubao.joyqueue.nsr.support;

import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.nsr.service.AppTokenService;
import io.chubao.joyqueue.nsr.service.internal.AppTokenInternalService;

import java.util.List;

/**
 * DefaultAppTokenService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultAppTokenService implements AppTokenService {

    private AppTokenInternalService appTokenInternalService;

    public DefaultAppTokenService(AppTokenInternalService appTokenInternalService) {
        this.appTokenInternalService = appTokenInternalService;
    }

    @Override
    public AppToken getById(long id) {
        return appTokenInternalService.getById(id);
    }

    @Override
    public AppToken getByAppAndToken(String app, String token) {
        return appTokenInternalService.getByAppAndToken(app, token);
    }

    @Override
    public List<AppToken> getByApp(String app) {
        return appTokenInternalService.getByApp(app);
    }

    @Override
    public List<AppToken> getAll() {
        return appTokenInternalService.getAll();
    }

    @Override
    public AppToken add(AppToken appToken) {
        return appTokenInternalService.add(appToken);
    }

    @Override
    public AppToken update(AppToken appToken) {
        return appTokenInternalService.update(appToken);
    }

    @Override
    public void delete(long id) {
        appTokenInternalService.delete(id);
    }
}