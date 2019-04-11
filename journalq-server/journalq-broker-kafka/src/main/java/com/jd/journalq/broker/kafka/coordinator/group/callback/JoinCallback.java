package com.jd.journalq.broker.kafka.coordinator.group.callback;

import com.jd.journalq.broker.kafka.coordinator.group.domain.GroupJoinGroupResult;

@FunctionalInterface
public interface JoinCallback {

    void sendResponseCallback(GroupJoinGroupResult groupJoinGroupResult);
}