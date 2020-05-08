package org.joyqueue.server.retry.model;

import java.util.List;

/**
 * Retry query extend
 *
 **/
public class RetryQueryConditionExt extends RetryQueryCondition {
    private List<String> topics;

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }
}
