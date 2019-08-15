package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.ApplicationUser;
import io.chubao.joyqueue.model.query.QApplicationUser;

/**
 * 应用-用户关联关系 服务
 * Created by chenyanying on 2018-10-17.
 */
public interface ApplicationUserService extends PageService<ApplicationUser, QApplicationUser> {
    ApplicationUser findByUserApp(String user, String app);
    int deleteByAppId(long appId);
}
