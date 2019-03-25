package com.jd.journalq.common.security;

import com.jd.journalq.common.exception.JMQException;

/**
 * @author majun8
 */
public interface PasswordEncoder {

    String encode(String password) throws JMQException;
}
