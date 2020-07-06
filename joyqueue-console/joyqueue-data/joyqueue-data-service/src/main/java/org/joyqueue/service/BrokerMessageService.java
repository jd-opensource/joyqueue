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
package org.joyqueue.service;


import org.joyqueue.model.domain.ProducerSendMessage;
import org.joyqueue.model.domain.SimplifiedBrokeMessage;
import org.joyqueue.model.domain.Subscribe;
import org.joyqueue.monitor.BrokerMessageInfo;

import java.util.List;

/**
 *
 *  用于预览消息
 * @author  wangjin
 * @time   2018-11-27
 *
 **/
public interface BrokerMessageService {

    /**
     *  积压消息预览，即将消费的消息
     *  @param topic 消息主题
     *  @param app   消息应用
     *  @param messageDecodeType message decode type
     *  @param count 获取的消息条数
     *
     * */
    List<SimplifiedBrokeMessage> previewMessage(Subscribe subscribe,String messageDecodeType ,int count);

    /**
     * 预览最新的消息
     * @param topic 消息主题
     * @param app   消息应用
     * @param count 获取的消息条数
     **/
    List<SimplifiedBrokeMessage> previewNewestMessage(long topicId,String topic,String app,int count);



    /**
     * @param partition 分区id
     * @param index     消息索引,第几条消息0～
     * @param count 获取的消息条数
     * @param messageDecodeType
     **/
    List<BrokerMessageInfo> viewMessage(Subscribe subscribe,String messageDecodeType , String partition, String index, int count);

    Long getPartitionIndexByTime(Subscribe subscribe,String partition, String timestamp);
    /**
     *  下载消息
     *  @param indexOffset  第几条消息
     *  @return 消息
     *
     **/
    SimplifiedBrokeMessage  download(String ip,int port,String topic,String app,short partition,long indexOffset);

    void sendMessage(ProducerSendMessage sendMessage);









}
