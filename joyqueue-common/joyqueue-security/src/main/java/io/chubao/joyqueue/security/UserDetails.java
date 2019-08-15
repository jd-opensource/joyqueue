package io.chubao.joyqueue.security;

/**
 * @author majun8
 */
public interface UserDetails {
    /**
     * 获取用户名称
     *
     * @return 用户名称
     */
    String getUsername();

    /**
     * 获取用户密码
     *
     * @return 密码
     */
    String getPassword();

    /**
     * 是否过期
     *
     * @return 过期表示
     */
    boolean isExpired();

    /**
     * 是否锁定
     *
     * @return 锁定标示
     */
    boolean isLocked();

    /**
     * 是否启用
     *
     * @return 启用标示
     */
    boolean isEnabled();

    /**
     * 是否是管理员用户
     *
     * @return 管理员标示
     */
    boolean isAdmin();
}
