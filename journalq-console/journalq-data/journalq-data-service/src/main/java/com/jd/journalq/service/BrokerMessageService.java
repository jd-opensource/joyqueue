package com.jd.journalq.service;


import com.jd.journalq.model.domain.SimplifiedBrokeMessage;
import com.jd.journalq.model.domain.Subscribe;

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
     *  @param count 获取的消息条数
     *
     * */
    List<SimplifiedBrokeMessage> previewPendingMessage(Subscribe subscribe,int count);

    /**
     * 预览最新的消息
     * @param topic 消息主题
     * @param app   消息应用
     * @param count 获取的消息条数
     **/
    List<SimplifiedBrokeMessage> previewNewestMessage(long topicId,String topic,String app,int count);



    /**
     * 预览最新的消息
     * @param topic  消息主题
     * @param app    消息应用
     * @param partition 分区id
     * @param index     消息索引,第几条消息0～
     * @param count 获取的消息条数
     *
     **/
    List<SimplifiedBrokeMessage> viewMessage(long topicId,String topic,String app,short partition,long index,int count);


    /**
     *  下载消息
     *  @param indexOffset  第几条消息
     *  @return 消息
     *
     **/
    SimplifiedBrokeMessage  download(String ip,int port,String topic,String app,short partition,long indexOffset);









}
