package com.jd.journalq.security.impl;

import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.security.PasswordEncoder;
import com.jd.journalq.toolkit.security.Encrypt;
import com.jd.journalq.toolkit.security.Sha;

/**
 * @author majun8
 */
public class DefaultPasswordEncoder implements PasswordEncoder {
    @Override
    public String encode(String password) throws JMQException {
        try {
            return Encrypt.encrypt(password, Encrypt.DEFAULT_KEY, Sha.INSTANCE);
        } catch (Exception e) {
            throw new JMQException(JMQCode.CN_AUTHENTICATION_ERROR);
        }

    }
}
