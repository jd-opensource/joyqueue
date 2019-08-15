package io.chubao.joyqueue.broker.protocol.coordinator;

import io.chubao.joyqueue.domain.Broker;

/**
 * Coordinator
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class Coordinator {

    private io.chubao.joyqueue.broker.coordinator.Coordinator coordinator;

    public Coordinator(io.chubao.joyqueue.broker.coordinator.Coordinator coordinator) {
        this.coordinator = coordinator;
    }

    public Broker findGroup(String app) {
        return coordinator.findGroup(app);
    }

    public boolean isCurrentGroup(String app) {
        return coordinator.isCurrentGroup(app);
    }
}