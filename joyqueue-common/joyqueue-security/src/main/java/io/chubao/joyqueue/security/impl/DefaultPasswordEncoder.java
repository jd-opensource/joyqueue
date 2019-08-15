package io.chubao.joyqueue.security.impl;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.security.PasswordEncoder;
import io.chubao.joyqueue.toolkit.security.Encrypt;
import io.chubao.joyqueue.toolkit.security.Sha;

/**
 * @author majun8
 */
public class DefaultPasswordEncoder implements PasswordEncoder {
    @Override
    public String encode(String password) throws JoyQueueException {
        try {
            return Encrypt.encrypt(password, Encrypt.DEFAULT_KEY, Sha.INSTANCE);
        } catch (Exception e) {
            throw new JoyQueueException(JoyQueueCode.CN_AUTHENTICATION_ERROR);
        }

    }
}
