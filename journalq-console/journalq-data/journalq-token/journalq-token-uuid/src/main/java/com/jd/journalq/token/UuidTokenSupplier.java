package com.jd.journalq.token;

import java.util.Date;
import java.util.UUID;

/**
 * UUID实现
 */
public class UuidTokenSupplier implements TokenSupplier {

    @Override
    public String apply(final String application, final Date effectiveTime, final Date expirationTime) {
        return UUID.randomUUID().toString().replaceAll("-" , "");
    }
}
