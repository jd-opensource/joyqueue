package com.jd.journalq.repository;


import com.jd.journalq.model.domain.BrokerGroupRelated;
import com.jd.journalq.model.query.QBrokerGroupRelated;
import org.springframework.stereotype.Repository;

/**
 * Created by lining on 16-11-28.
 */
@Repository
public interface BrokerGroupRelatedRepository extends PageRepository<BrokerGroupRelated,QBrokerGroupRelated> {
    int updateGroupByGroupId(BrokerGroupRelated brokerGroupRelated);
}
