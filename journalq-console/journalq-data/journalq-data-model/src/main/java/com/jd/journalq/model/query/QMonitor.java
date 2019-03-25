package com.jd.journalq.model.query;

import com.jd.journalq.common.model.Query;
import com.jd.journalq.model.domain.SubscribeType;

/**
 * Created by wangxiaofei1 on 2019/3/13.
 */
public class QMonitor implements Query {
    private Long brokerId;
    private SubscribeType type; // producer or consumer

    public Long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
    }

    public SubscribeType getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = SubscribeType.resolve(type);
    }
}
