package com.jd.journalq.common.security;

import com.jd.journalq.common.exception.JMQException;
import com.jd.journalq.common.response.BooleanResponse;

/**
 * @author majun8
 */
public interface Authentication {

    @Deprecated
    UserDetails getUser(String user) throws JMQException;

    @Deprecated
    PasswordEncoder getPasswordEncode();

    BooleanResponse auth(String userName, String password);

    BooleanResponse auth(String userName, String password, boolean checkAdmin);

    boolean isAdmin(String userName);
}
