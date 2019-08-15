package io.chubao.joyqueue.broker.manage.service;

/**
 * ConnectionManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public interface ConnectionManageService {

    /**
     * 关闭主题下应用的所有生产者连接
     *
     * @param topic 主题
     * @param app 应用
     * @return 被关闭的连接数量
     */
    int closeProducer(String topic, String app);

    /**
     * 关闭主题下应用的所有消费者连接
     *
     * @param topic 主题
     * @param app 应用
     * @return 被关闭的连接数量
     */
    int closeConsumer(String topic, String app);
}