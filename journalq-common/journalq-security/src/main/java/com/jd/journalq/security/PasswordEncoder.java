package com.jd.journalq.security;

import com.jd.journalq.exception.JMQException;

/**
 * @author majun8
 */
public interface PasswordEncoder {

    String encode(String password) throws JMQException;
}
