package io.openmessaging.journalq.domain;

import io.openmessaging.OMSBuiltinKeys;

/**
 * JournalQNameServerBuiltinKeys
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public interface JournalQNameServerBuiltinKeys extends OMSBuiltinKeys {

    String NAMESPACE = "NAMESERVER_NAMESPACE";

    String METADATA_UPDATE_INTERVAL = "NAMESERVER_METADATA_UPDATE_INTERVAL";

    String METADATA_TEMP_INTERVAL = "NAMESERVER_METADATA_TEMP_INTERVAL";

    String METADATA_UPDATE_THREAD = "NAMESERVER_METADATA_UPDATE_THREAD";

    String METADATA_UPDATE_QUEUE_SIZE = "NAMESERVER_METADATA_UPDATE_QUEUE_SIZE";
}