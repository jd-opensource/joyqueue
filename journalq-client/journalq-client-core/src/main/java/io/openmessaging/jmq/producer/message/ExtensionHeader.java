package io.openmessaging.jmq.producer.message;

/**
 * ExtensionHeader
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/20
 */
public interface ExtensionHeader extends io.openmessaging.extension.ExtensionHeader {

    ExtensionHeader setFlag(short flag);

    short getFlag();
}