/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.client.samples.springcloud.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;

/**
 * Custom Processor
 */
@Component
public interface CustomProcessor {

    String INPUT_ORDER = "inputOrder";

    String OUTPUT_ORDER = "outputOrder";

    /**
     * 主题订阅通道
     *  <p>
     *     订阅消息通道{@link SubscribableChannel}为消息通道{@link MessageChannel}子类，该通道的所有消息被{@link org.springframework.messaging.MessageHandler}消息处理器所订阅
     *  </p>
     * @return {@link SubscribableChannel}
     */
    @Input(INPUT_ORDER)
    SubscribableChannel inputOrder();

    /**
     * 主题消息发布通道
     *
     * <p>
     *  消息通道{@link MessageChannel}用于接收消息，调用{@link MessageChannel#send(Message)}方法可以将消息发送至该消息通道中
     * </p>
     * @return {@link MessageChannel}
     */
    @Output(OUTPUT_ORDER)
    MessageChannel outputOrder();

}
