package io.chubao.joyqueue.nsr.message.support.network.command;

import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;

/**
 * MessengerPublishRequest
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerPublishRequest extends JoyQueuePayload implements Type {

    private String type;
    private String classType;
    private MetaEvent event;

    public MessengerPublishRequest() {

    }

    public MessengerPublishRequest(MetaEvent event) {
        this.type = event.getTypeName();
        this.classType = event.getClass().getName();
        this.event = event;
    }

    public MessengerPublishRequest(String type, String classType, MetaEvent event) {
        this.type = type;
        this.classType = classType;
        this.event = event;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public MetaEvent getEvent() {
        return event;
    }

    public void setEvent(MetaEvent event) {
        this.event = event;
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_MESSENGER_PUBLISH_REQUEST;
    }
}
