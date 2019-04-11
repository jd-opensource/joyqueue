package com.jd.journalq.broker.kafka.coordinator.group.callback;

@FunctionalInterface
public interface LeaveCallback {

    void sendResponseCallback(short errorCode);
}