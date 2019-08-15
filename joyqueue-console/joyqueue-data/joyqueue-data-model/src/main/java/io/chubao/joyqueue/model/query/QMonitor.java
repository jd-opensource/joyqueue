package io.chubao.joyqueue.model.query;

import io.chubao.joyqueue.model.Query;
import io.chubao.joyqueue.model.domain.SubscribeType;

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
