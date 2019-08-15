package io.chubao.joyqueue.handler.message;

import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.domain.OperLog;

import java.util.Date;

/**
 * 操作日志消息
 * Created by wangxiaofei1 on 2018/12/4.
 */
public class OperLogMessage extends OperLog{

    public OperLogMessage() {
        super();
    }

    public OperLogMessage(Integer operType, Integer type, String identity, String target, Long userId) {
        super.setOperType(operType);
        super.setType(type);
        super.setIdentity(identity);
        super.setTarget(target);
        super.setCreateBy(new Identity(userId));
        super.setCreateTime(new Date());
    }
}
