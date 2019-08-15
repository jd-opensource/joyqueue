package io.chubao.joyqueue.broker.kafka.coordinator.group.callback;

import io.chubao.joyqueue.broker.kafka.coordinator.group.domain.GroupJoinGroupResult;

@FunctionalInterface
public interface JoinCallback {

    void sendResponseCallback(GroupJoinGroupResult groupJoinGroupResult);
}