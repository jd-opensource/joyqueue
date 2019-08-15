package io.chubao.joyqueue.repository;


import io.chubao.joyqueue.model.domain.BrokerGroupRelated;
import io.chubao.joyqueue.model.query.QBrokerGroupRelated;
import org.springframework.stereotype.Repository;

/**
 * Created by lining on 16-11-28.
 */
@Repository
public interface BrokerGroupRelatedRepository extends PageRepository<BrokerGroupRelated,QBrokerGroupRelated> {
    int updateGroupByGroupId(BrokerGroupRelated brokerGroupRelated);
    int deleteByGroupId(long groupId);
}
