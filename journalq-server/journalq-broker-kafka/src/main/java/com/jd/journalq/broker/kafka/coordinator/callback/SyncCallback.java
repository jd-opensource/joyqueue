package com.jd.journalq.broker.kafka.coordinator.callback;

import com.jd.journalq.broker.kafka.command.SyncGroupAssignment;

@FunctionalInterface
public interface SyncCallback {

    void sendResponseCallback(SyncGroupAssignment assignment, short errorCode);
}