package com.jd.journalq.async;

import com.jd.journalq.domain.PartitionGroup;
import org.apache.http.client.methods.HttpUriRequest;

/**
 *
 * provide for PUT,POST request
 **/
public interface UpdateProvider<C> extends RetrieveProvider<C> {




    /**
     * @return  request ,such as HttpPut,HttpPost
     **/
    HttpUriRequest getRequest(String uri, PartitionGroup partitionGroup, short partition, C condition);

}
