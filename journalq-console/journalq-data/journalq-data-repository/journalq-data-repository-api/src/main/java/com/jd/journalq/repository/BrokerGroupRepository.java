package com.jd.journalq.repository;

import com.jd.journalq.model.domain.BrokerGroup;
import com.jd.journalq.model.query.QBrokerGroup;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 分组 仓库
 * Created by chenyanying3 on 2018-10-18
 */
@Repository
public interface BrokerGroupRepository extends PageRepository<BrokerGroup, QBrokerGroup> {
    /**
     * 查找全部
     * @return
     */
    List<BrokerGroup> findAll(QBrokerGroup qBrokerGroup);
}
