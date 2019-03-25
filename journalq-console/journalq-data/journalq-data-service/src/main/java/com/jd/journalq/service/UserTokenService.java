package com.jd.journalq.service;


import com.jd.journalq.common.model.QKeyword;
import com.jd.journalq.model.domain.UserToken;

public interface UserTokenService extends PageService<UserToken,QKeyword> {

    UserToken findByCode(String code);
}
