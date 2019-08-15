package io.chubao.joyqueue.broker.kafka.coordinator.group.callback;

import io.chubao.joyqueue.broker.kafka.command.SyncGroupAssignment;

@FunctionalInterface
public interface SyncCallback {

    void sendResponseCallback(SyncGroupAssignment assignment, short errorCode);
}