package io.chubao.joyqueue.broker.manage.service;

/**
 * BrokerManageService
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public interface BrokerManageService extends ConnectionManageService, MessageManageService, StoreManageService, ConsumerManageService,
        CoordinatorManageService, ElectionManageService {

}