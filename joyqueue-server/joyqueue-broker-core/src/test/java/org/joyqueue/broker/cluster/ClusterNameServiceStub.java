package org.joyqueue.broker.cluster;

import com.google.common.collect.Lists;
import org.joyqueue.broker.event.BrokerEventBus;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.nsr.NameService;
import org.joyqueue.toolkit.config.PropertySupplier;

import java.util.List;
import java.util.Map;

/**
 * ClusterNameServiceStub
 * author: gaohaoxiang
 * date: 2020/3/27
 */
public class ClusterNameServiceStub extends ClusterNameService {

    private List<Map<Integer, List<Integer>>> lastRemote = Lists.newLinkedList();
    private boolean remoteResult;
    private int total = 0;
    private int sleep = 0;

    public ClusterNameServiceStub(NameService nameService, BrokerEventBus eventBus, PropertySupplier propertySupplier) {
        super(nameService, eventBus, propertySupplier);
    }

    @Override
    protected boolean doGetRemoteTopicConfig(TopicConfig topicConfig, Map<Integer, List<Integer>> splittedByGroup) {
        lastRemote.add(splittedByGroup);
        total++;
        if (sleep != 0) {
            try {
                Thread.currentThread().sleep(sleep);
            } catch (InterruptedException e) {
            }
        }
        return remoteResult;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public List<Map<Integer, List<Integer>>> getLastRemote() {
        return lastRemote;
    }

    public void setRemoteResult(boolean remoteResult) {
        this.remoteResult = remoteResult;
    }

    public int getTotal() {
        return total;
    }
}
