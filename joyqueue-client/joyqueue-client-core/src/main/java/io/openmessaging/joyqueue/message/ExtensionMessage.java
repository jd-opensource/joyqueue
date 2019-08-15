package io.openmessaging.joyqueue.message;

import io.openmessaging.message.Message;

/**
 * ExtensionMessage
 *
 * author: gaohaoxiang
 * date: 2019/7/1
 */
public interface ExtensionMessage extends Message {

    void setStringData(String data);

    String getStringData();
}