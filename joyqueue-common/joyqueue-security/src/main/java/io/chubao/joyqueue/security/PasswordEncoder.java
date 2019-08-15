package io.chubao.joyqueue.security;

import io.chubao.joyqueue.exception.JoyQueueException;

/**
 * @author majun8
 */
public interface PasswordEncoder {

    String encode(String password) throws JoyQueueException;
}
