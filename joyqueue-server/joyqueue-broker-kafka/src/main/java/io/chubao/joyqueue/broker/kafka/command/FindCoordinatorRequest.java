package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.coordinator.CoordinatorType;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;

/**
 * Created by zhangkepeng on 17-2-9.
 */
public class FindCoordinatorRequest extends KafkaRequestOrResponse {

    private String coordinatorKey;
    private CoordinatorType coordinatorType;

    public String getCoordinatorKey() {
        return coordinatorKey;
    }

    public void setCoordinatorKey(String coordinatorKey) {
        this.coordinatorKey = coordinatorKey;
    }

    public CoordinatorType getCoordinatorType() {
        return coordinatorType;
    }

    public void setCoordinatorType(CoordinatorType coordinatorType) {
        this.coordinatorType = coordinatorType;
    }

    @Override
    public String toString() {
        StringBuilder requestStringBuilder = new StringBuilder();
        requestStringBuilder.append("Name: " + this.getClass().getSimpleName());
        requestStringBuilder.append("; coordinatorKey: " + coordinatorKey);
        return requestStringBuilder.toString();
    }

    @Override
    public int type() {
        return KafkaCommandType.FIND_COORDINATOR.getCode();
    }
}
