package io.chubao.joyqueue.async;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.model.domain.Broker;

public interface RetrieveProvider<C> {

    /**
     *
     * @return  key as response id,not null
     **/
    String getKey(Broker broker, PartitionGroup partitionGroup,short partition ,C condition);
    /**
     *
     *
     * @return  asyncQueryOnBroker path for condition
     *  not null
     **/
    String getPath(String pathTemplate,PartitionGroup partitionGroup,short partition ,C condition);
}
