package com.jd.journalq.repository;

import com.jd.journalq.common.model.QKeyword;
import com.jd.journalq.model.domain.UserToken;
import org.springframework.stereotype.Repository;


@Repository
public interface UserTokenRepository extends PageRepository<UserToken, QKeyword> {

    UserToken findByCode(String code);
}
