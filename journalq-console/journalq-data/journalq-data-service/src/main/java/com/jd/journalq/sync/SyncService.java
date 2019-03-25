package com.jd.journalq.sync;

import com.jd.journalq.model.domain.Application;
import com.jd.journalq.model.domain.User;

/**
 * 同步服务
 */
public interface SyncService {

    /**
     * 同步应用
     *
     * @param application
     * @return
     * @throws Exception
     */
    ApplicationInfo syncApp(Application application) throws Exception;

    /**
     * 同步应用
     *
     * @param user
     * @return
     * @throws Exception
     */
    UserInfo syncUser(User user) throws Exception;

    /**
     * 更新应用数据
     *
     * @param info
     * @return
     */
    Application addOrUpdateApp(ApplicationInfo info);

    /**
     * 更新用户数据
     *
     * @param info
     * @return
     */
    User addOrUpdateUser(UserInfo info);
}
