package io.chubao.joyqueue.broker.monitor.service;

import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.response.BooleanResponse;

/**
 * MetadataMonitorService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/11
 */
public interface MetadataMonitorService {

    /**
     * 获取主题元数据
     *
     * @param topic 主题
     * @param isCluster true - 从ClusterManager获取，false - 从NameServer获取
     * @return 主题元数据
     */
    TopicConfig getTopicMetadata(String topic, boolean isCluster);

    /**
     * 是否有读权限
     *
     * @param topic 主题
     * @param app 应用
     * @param address 地址
     * @return 结果
     */
    BooleanResponse getReadableResult(String topic, String app, String address);

    /**
     * 是否有写权限
     *
     * @param topic 主题
     * @param app 应用
     * @param address 地址
     * @return 结果
     */
    BooleanResponse getWritableResult(String topic, String app, String address);
}