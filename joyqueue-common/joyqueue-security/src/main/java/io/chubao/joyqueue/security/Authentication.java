package io.chubao.joyqueue.security;

import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.response.BooleanResponse;

/**
 * @author majun8
 */
public interface Authentication {

    @Deprecated
    UserDetails getUser(String user) throws JoyQueueException;

    @Deprecated
    PasswordEncoder getPasswordEncode();

    BooleanResponse auth(String userName, String password);

    BooleanResponse auth(String userName, String password, boolean checkAdmin);

    boolean isAdmin(String userName);
}
