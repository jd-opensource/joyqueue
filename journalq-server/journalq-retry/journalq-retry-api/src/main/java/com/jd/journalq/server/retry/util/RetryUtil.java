package com.jd.journalq.server.retry.util;

/**
 * 重试工具类
 *
 * Created by chengzhiliang on 2019/2/10.
 */
public class RetryUtil {

    /**
     * 生成消息Id
     *
     * @param topic     消息主题
     * @param partition 分区
     * @param index     消息序号
     * @return 消息ID
     */
    public static String generateMessageId(String topic, short partition, long index) {
        StringBuilder sb = new StringBuilder();
        sb.append(topic);
        sb.append('-').append(partition);
        sb.append('-').append(index);

        return sb.toString();
    }

}
