package com.jd.journalq.network.command;

import com.jd.journalq.exception.JMQCode;

import java.util.List;

/**
 * ProduceMessageAckData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessageAckData {

    private List<ProduceMessageAckItemData> item;
    private JMQCode code;

    public ProduceMessageAckData() {

    }

    public ProduceMessageAckData(List<ProduceMessageAckItemData> item, JMQCode code) {
        this.item = item;
        this.code = code;
    }

    public void setItem(List<ProduceMessageAckItemData> item) {
        this.item = item;
    }

    public List<ProduceMessageAckItemData> getItem() {
        return item;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }

    public JMQCode getCode() {
        return code;
    }
}