package io.chubao.joyqueue.nsr.service.internal;

import java.net.URI;
import java.util.List;

/**
 * ClusterInternalService
 * author: gaohaoxiang
 * date: 2019/10/31
 */
public interface ClusterInternalService {

    /**
     * 返回集群信息
     * @return
     */
    String getCluster();

    /**
     * 添加集群
     * @param uri
     * @return
     */
    String addNode(URI uri);

    /**
     * 删除节点
     * @param uri
     * @return
     */
    String removeNode(URI uri);

    /**
     * 更新节点
     * @param uris
     * @return
     */
    String updateNodes(List<URI> uris);
}