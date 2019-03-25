package com.jd.journalq.broker.kafka.coordinator.callback;

import com.jd.journalq.broker.kafka.coordinator.domain.GroupJoinGroupResult;

@FunctionalInterface
public interface JoinCallback {

    void sendResponseCallback(GroupJoinGroupResult groupJoinGroupResult);
}