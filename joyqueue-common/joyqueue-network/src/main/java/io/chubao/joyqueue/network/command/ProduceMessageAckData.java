package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;

import java.util.List;

/**
 * ProduceMessageAckData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessageAckData {

    private List<ProduceMessageAckItemData> item;
    private JoyQueueCode code;

    public ProduceMessageAckData() {

    }

    public ProduceMessageAckData(List<ProduceMessageAckItemData> item, JoyQueueCode code) {
        this.item = item;
        this.code = code;
    }

    public void setItem(List<ProduceMessageAckItemData> item) {
        this.item = item;
    }

    public List<ProduceMessageAckItemData> getItem() {
        return item;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public JoyQueueCode getCode() {
        return code;
    }
}