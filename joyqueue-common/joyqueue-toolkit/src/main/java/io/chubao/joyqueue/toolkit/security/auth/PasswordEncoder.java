package io.chubao.joyqueue.toolkit.security.auth;

/**
 * 密码加密
 */
public interface PasswordEncoder {

    /**
     * 加密
     *
     * @param password 密码
     * @return 加密后的密文
     * @throws AuthException
     */
    String encode(String password) throws AuthException;

}