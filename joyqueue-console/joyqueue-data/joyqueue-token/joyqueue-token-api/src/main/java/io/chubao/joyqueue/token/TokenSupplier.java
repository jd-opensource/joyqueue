package io.chubao.joyqueue.token;

import java.util.Date;

/**
 * 令牌提供者
 */
public interface TokenSupplier {

    /**
     * 生成应用的令牌
     *
     * @param application    应用
     * @param effectiveTime  生效时间
     * @param expirationTime 失效时间
     * @return 令牌
     */
    String apply(String application, Date effectiveTime, Date expirationTime);

}
