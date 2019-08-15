package io.chubao.joyqueue.broker.consumer;

import io.chubao.joyqueue.message.MessageLocation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chengzhiliang on 2018/10/24.
 */
public class AcknowledgeSupportTest {

    @Test
    public void sortMsgLocation() {
        String topic = "topic";
        short app = 1;
        List<MessageLocation> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            MessageLocation messageLocation = new MessageLocation(topic, app, (long) i);
            list.add(messageLocation);
        }
        long[] longs = AcknowledgeSupport.sortMsgLocation(list.toArray(new MessageLocation[]{}));
        System.out.println(Arrays.toString(longs));
    }
}