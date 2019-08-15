package io.chubao.joyqueue.util;

import io.chubao.joyqueue.model.domain.User;

/**
 * Created by wangxiaofei1 on 2018/12/12.
 */
public class LocalSession {
    private static final ThreadLocal<User> local = new ThreadLocal<User>();

    private static final LocalSession session =new  LocalSession();

    public User getUser() {
        return local.get();
    }

    public void setUser(User user) {
        local.set(user);
    }

    /**
     * 单例函数
     *
     * @return 会话
     */
    public static LocalSession getSession() {
        return session;
    }
}
