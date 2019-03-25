package com.jd.journalq.broker.kafka.coordinator.callback;

@FunctionalInterface
public interface LeaveCallback {

    void sendResponseCallback(short errorCode);
}