package com.jd.journalq.server.retry.remote;


import com.jd.journalq.common.network.transport.TransportClient;

import java.util.Set;

/**
 * 远程重试扩展接口
 * <p>
 * Created by chengzhiliang on 2019/2/14.
 */
public interface RemoteRetryProvider {

    /**
     * 获取远程重试地址集合
     *
     * @return
     */
    Set<String/*ip:port*/> getUrls();

    /**
     * 获取网络通道客户端
     *
     * @return
     */
    TransportClient createTransportClient();

}
