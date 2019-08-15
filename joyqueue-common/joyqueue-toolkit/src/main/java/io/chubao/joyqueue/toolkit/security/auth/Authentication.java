package io.chubao.joyqueue.toolkit.security.auth;

/**
 * 用户认证
 */
public interface Authentication {

    /**
     * 返回用户信息
     *
     * @param user 用户
     * @throws AuthException
     */
    UserDetails getUser(String user) throws AuthException;

    /**
     * 获取加密器
     *
     * @return 加密器
     */
    PasswordEncoder getPasswordEncode();

}