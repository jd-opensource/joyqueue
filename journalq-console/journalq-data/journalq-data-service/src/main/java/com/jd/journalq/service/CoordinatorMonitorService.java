package com.jd.journalq.service;

import com.jd.journalq.common.domain.CoordinatorGroup;
import com.jd.journalq.common.domain.CoordinatorGroupExpiredMember;
import com.jd.journalq.common.domain.CoordinatorGroupMember;
import com.jd.journalq.model.domain.CoordinatorBroker;
import com.jd.journalq.model.domain.Subscribe;

import java.util.List;
//todo 待移走
/**
 *
 * @author  wangjin18
 * @date    2019-01-02
 *
 **/
public interface CoordinatorMonitorService {


    /**
     *
     * @return app  消费组的协调信息，包括当前的协调信息和已过期的协调信息
     *
     * */
    CoordinatorGroup findCoordinatorGroup(Subscribe subscribe);

    /**
     *
     * @return  app 的协调者信息
     *
     **/
    List<CoordinatorBroker> findCoordinatorInfo(Subscribe subscribe);


    /**
     *
     * @return app  消费组成员列表
     *
     * */
    List<CoordinatorGroupMember> findCoordinatorGroupMember(Subscribe subscribe);


    /**
     *
     * @return app  过期的消费组成员列表
     *
     * */
    List<CoordinatorGroupExpiredMember> findExpiredCoordinatorGroupMember(Subscribe subscribe);



}
