package org.joyqueue;

import org.joyqueue.broker.BrokerService;

public class BrokerServiceTestBase extends BrokerService {

    private int port;
    public BrokerServiceTestBase(int port){
        this.port=port;
    }
    @Override
    protected void validate() throws Exception {
        //super.validate();

    }
}
