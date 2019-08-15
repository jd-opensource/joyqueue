package io.chubao.joyqueue.model.domain;

import io.chubao.joyqueue.domain.Broker;

public class CoordinatorBroker {
    private Broker broker;
    private boolean isCoordinator;


    public Broker getBroker() {
        return broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public boolean isCoordinator() {
        return isCoordinator;
    }

    public void setCoordinator(boolean coordinator) {
        isCoordinator = coordinator;
    }
}
