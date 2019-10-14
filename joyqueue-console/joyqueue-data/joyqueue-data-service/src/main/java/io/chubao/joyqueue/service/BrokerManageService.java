package io.chubao.joyqueue.service;

import io.chubao.joyqueue.toolkit.io.Directory;

public interface BrokerManageService {

    /**
     * Broker store tree view
     * @return store tree view
     **/
    Directory storeTreeView(int brokerId);

    /**
     * Delete garbage file on broker, which name start with .d.
     * @return true if delete success
     **/
    boolean deleteGarbageFile(int brokerId,String path);



}
