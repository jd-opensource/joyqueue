package io.chubao.joyqueue.convert;

import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.model.domain.ApplicationToken;
import io.chubao.joyqueue.model.domain.Identity;

/**
 * Created by wangxiaofei1 on 2018/12/27.
 */
public class NsrAppTokenConverter extends Converter<AppToken, ApplicationToken> {

    public NsrAppTokenConverter() {
    }

    public NsrAppTokenConverter(boolean checkNull) {
        super(checkNull);
    }

    @Override
    protected ApplicationToken forward(AppToken appToken) {
        ApplicationToken applicationToken = new ApplicationToken();
        if (appToken.getId() != null) {
            applicationToken.setId(appToken.getId());
        }
        if (appToken.getApp() != null) {
            applicationToken.setApplication(new Identity(appToken.getApp()));
        }
        if (appToken.getToken() != null) {
            applicationToken.setToken(appToken.getToken());
        }
        if (appToken.getEffectiveTime() != null) {
            applicationToken.setEffectiveTime(appToken.getEffectiveTime());
        }
        if (appToken.getExpirationTime() != null) {
            applicationToken.setExpirationTime(appToken.getExpirationTime());
        }
        return applicationToken;
    }

    @Override
    protected AppToken backward(ApplicationToken applicationToken) {
        AppToken appToken = new AppToken();
        appToken.setId(applicationToken.getId());
        if (applicationToken.getApplication().getCode() != null) {
            appToken.setApp(applicationToken.getApplication().getCode());
        }
        if (applicationToken.getToken() != null) {
            appToken.setToken(applicationToken.getToken());
        }
        if (applicationToken.getEffectiveTime() != null) {
            appToken.setEffectiveTime(applicationToken.getEffectiveTime());
        }
        if (applicationToken.getExpirationTime() != null) {
            appToken.setExpirationTime(applicationToken.getExpirationTime());
        }
        return appToken;
    }
}
