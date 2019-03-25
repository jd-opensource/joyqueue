package com.jd.journalq.service;

import com.jd.journalq.model.domain.ApplicationUser;
import com.jd.journalq.model.query.QApplicationUser;

/**
 * 应用-用户关联关系 服务
 * Created by chenyanying on 2018-10-17.
 */
public interface ApplicationUserService extends PageService<ApplicationUser, QApplicationUser> {
    ApplicationUser findByUserApp(String user, String app);
}
