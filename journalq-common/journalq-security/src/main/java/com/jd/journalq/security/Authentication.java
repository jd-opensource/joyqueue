package com.jd.journalq.security;

import com.jd.journalq.exception.JMQException;
import com.jd.journalq.response.BooleanResponse;

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
