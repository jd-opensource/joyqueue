package com.jd.journalq.sync;

public interface UserSupplier {

    /**
     * 根据代码查找应用
     *
     * @param code
     * @return
     */
    UserInfo findByCode(String code) throws Exception;
}
