package io.chubao.joyqueue.nsr.journalkeeper.messenger;

import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.nsr.message.MessageListener;
import io.chubao.joyqueue.nsr.message.Messenger;

/**
 * JournalkeeperMessenger
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperMessenger implements Messenger<MetaEvent> {

    @Override
    public void publish(MetaEvent metaEvent) {

    }

    @Override
    public void addListener(MessageListener listener) {

    }
}