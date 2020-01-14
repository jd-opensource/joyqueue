/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.consumer;

import org.joyqueue.broker.consumer.AcknowledgeSupport;
import org.joyqueue.message.MessageLocation;
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