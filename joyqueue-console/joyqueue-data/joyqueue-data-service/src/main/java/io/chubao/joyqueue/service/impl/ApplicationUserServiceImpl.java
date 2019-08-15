package io.chubao.joyqueue.service.impl;

import io.chubao.joyqueue.model.domain.ApplicationUser;
import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.query.QApplicationUser;
import io.chubao.joyqueue.repository.ApplicationUserRepository;
import io.chubao.joyqueue.service.ApplicationUserService;
import org.springframework.stereotype.Service;

/**
 * 应用-用户关联关系 服务
 * Created by chenyanying3 on 2018-10-15
 */
@Service("applicationUserService")
public class ApplicationUserServiceImpl extends PageServiceSupport<ApplicationUser, QApplicationUser, ApplicationUserRepository> implements ApplicationUserService {

    @Override
    public ApplicationUser findByUserApp(String user, String app){
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setApplication(new Identity(app));
        applicationUser.setUser(new Identity(user));
        return repository.findByUserApp(applicationUser);
    }

    @Override
    public int deleteByAppId(long appId) {
        return repository.deleteByAppId(appId);
    }
}
