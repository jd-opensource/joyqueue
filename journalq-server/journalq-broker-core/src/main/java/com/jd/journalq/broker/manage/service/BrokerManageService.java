package com.jd.journalq.broker.manage.service;

/**
 * BrokerManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public interface BrokerManageService extends ConnectionManageService, MessageManageService, StoreManageService, ConsumerManageService, CoordinatorManageService, ElectionManageService {

}