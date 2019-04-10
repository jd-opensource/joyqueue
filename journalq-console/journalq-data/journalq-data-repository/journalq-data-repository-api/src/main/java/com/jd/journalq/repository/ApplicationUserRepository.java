package com.jd.journalq.repository;

import com.jd.journalq.model.domain.ApplicationUser;
import com.jd.journalq.model.query.QApplicationUser;
import org.springframework.stereotype.Repository;

/**
 * 应用-用户关联关系 仓库
 * Created by chenyanying3 on 2018-10-15
 */
@Repository
public interface ApplicationUserRepository extends PageRepository<ApplicationUser, QApplicationUser> {
    ApplicationUser findByUserApp(ApplicationUser applicationUser);
    int deleteByAppId(long appId);
}
