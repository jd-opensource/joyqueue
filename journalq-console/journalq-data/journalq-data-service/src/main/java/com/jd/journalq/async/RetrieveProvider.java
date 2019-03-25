package com.jd.journalq.async;

import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.model.domain.Broker;

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
