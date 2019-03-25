package com.jd.journalq.service.impl;

import com.jd.journalq.model.domain.ApplicationUser;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.query.QApplicationUser;
import com.jd.journalq.repository.ApplicationUserRepository;
import com.jd.journalq.service.ApplicationUserService;
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
}
