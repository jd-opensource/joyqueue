package com.jd.journalq.broker.kafka.exception;

/**
 * Thrown when a request is made for partition, but no leader exists for that partition
 */
public class LeaderNotAvailableException extends KafkaException {

    public LeaderNotAvailableException(String message, Throwable e) {
        super(message, e);
    }

    public LeaderNotAvailableException(String message) {
        super(message);
    }
}