package com.jd.journalq.service.impl;

import com.jd.journalq.model.QKeyword;
import com.jd.journalq.model.domain.UserToken;
import com.jd.journalq.repository.UserTokenRepository;
import com.jd.journalq.service.UserTokenService;
import org.springframework.stereotype.Service;

@Service("userTokenService")
public class UserTokenServiceImpl extends PageServiceSupport<UserToken, QKeyword, UserTokenRepository> implements UserTokenService {

    @Override
    public UserToken findByCode(String code) {
        return repository.findByCode(code);
    }
}
