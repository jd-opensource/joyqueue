package com.jd.journalq.broker.profile;

import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.stat.ClientTPStat;
import com.jd.journalq.toolkit.lang.LifeCycle;

import java.util.List;

/**
 * 性能存储
 * User: weiqisong
 * Date: 14-9-23
 * Time: 上午10:49
 */
public interface ClientStatManager extends LifeCycle {

    // 生产消息统计
    String CLIENT_PRODUCE = "client.produce";
    // 生产消息统计tps
    String CLIENT_PRODUCE_TPS = "client.produce.tps";
    //生产消息失败数量
    String CLIENT_PRODUCE_ERROR = "client.produce.error";
    //生产消息成功大小
    String CLIENT_PRODUCE_SIZE = "client.produce.size";
    //生产消息总时间
    String CLIENT_PRODUCE_TIME = "client.produce.time";
    //生产消息TP999
    String CLIENT_PRODUCE_TP999 = "client.produce.tp999";
    //生产消息TP99
    String CLIENT_PRODUCE_TP99 = "client.produce.tp99";
    //生产消息TP90
    String CLIENT_PRODUCE_TP90 = "client.produce.tp90";
    //生产消息TP50
    String CLIENT_PRODUCE_TP50 = "client.produce.tp50";
    //生产消息最大值
    String CLIENT_PRODUCE_MAX = "client.produce.max";
    //生产消息最小值
    String CLIENT_PRODUCE_MIN = "client.produce.min";
    //生产消息成功率
    String CLIENT_PRODUCE_RATIO = "client.produce.ratio";
    //接收消息数量
    String CLIENT_RECEIVE = "client.receive";
    //接收消息数量tps
    String CLIENT_RECEIVE_TPS = "client.receive.tps";
    //接收消息大小
    String CLIENT_RECEIVE_SIZE = "client.receive.size";
    //接收消息时间
    String CLIENT_RECEIVE_TIME = "client.receive.time";
    //接收消息TP999
    String CLIENT_RECEIVE_TP999 = "client.receive.tp999";
    //接收消息TP99
    String CLIENT_RECEIVE_TP99 = "client.receive.tp99";
    //接收消息TP90
    String CLIENT_RECEIVE_TP90 = "client.receive.tp90";
    //接收消息TP50
    String CLIENT_RECEIVE_TP50 = "client.receive.tp50";
    //接收消息最大值
    String CLIENT_RECEIVE_MAX = "client.receive.max";
    //接收消息最小值
    String CLIENT_RECEIVE_MIN = "client.receive.min";
    //接收消息成功率
    String CLIENT_RECEIVE_RATIO = "client.receive.ratio";
    //消费成功条数
    String CLIENT_CONSUME = "client.consume";
    //消费成功条数tps
    String CLIENT_CONSUME_TPS = "client.consume.tps";
    //消费失败条数
    String CLIENT_CONSUME_ERROR = "client.consume.error";
    //消费总时间
    String CLIENT_CONSUME_TIME = "client.consume.time";
    //消费消息TP999
    String CLIENT_CONSUME_TP999 = "client.consume.tp999";
    //消费消息TP99
    String CLIENT_CONSUME_TP99 = "client.consume.tp99";
    //消费消息TP90
    String CLIENT_CONSUME_TP90 = "client.consume.tp90";
    //消费消息TP50
    String CLIENT_CONSUME_TP50 = "client.consume.tp50";
    //消费消息最大值
    String CLIENT_CONSUME_MAX = "client.consume.max";
    //消费消息最小值
    String CLIENT_CONSUME_MIN = "client.consume.min";
    //消费消息成功率
    String CLIENT_CONSUME_RATIO = "client.consume.ratio";

    //重试条数
    String CLIENT_RETRY = "client.retry";
    //重试条数tps
    String CLIENT_RETRY_TPS = "client.retry.tps";
    //重试失败条数
    String CLIENT_RETRY_ERROR = "client.retry.error";
    //重试总时间
    String CLIENT_RETRY_TIME = "client.retry.time";
    //重试TP999
    String CLIENT_RETRY_TP999 = "client.retry.tp999";
    //重试TP99
    String CLIENT_RETRY_TP99 = "client.retry.tp99";
    //重试TP90
    String CLIENT_RETRY_TP90 = "client.retry.tp90";
    //重试TP50
    String CLIENT_RETRY_TP50 = "client.retry.tp50";
    //重试最大值
    String CLIENT_RETRY_MAX = "client.retry.max";
    //重试最小值
    String CLIENT_RETRY_MIN = "client.retry.min";
    //重试成功率
    String CLIENT_RETRY_RATIO = "client.retry.ratio";

    /**
     * 设置配置信息。
     *
     * @param config 配置信息
     */
    void setConfig(ClientStatConfig config);

    /**
     * 设置集群信息
     *
     * @param clusterManager 集群管理器
     */
    void setClusterManager(ClusterManager clusterManager);

    /**
     * 保存客户端的性能数据。
     *
     * @param clientStats 客户端统计
     */
    void update(List<ClientTPStat> clientStats);


}
