package org.joyqueue.client.internal.producer.support;

import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;
import org.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import org.joyqueue.client.internal.metadata.domain.PartitionNode;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.loadbalance.adaptive.AdaptiveLoadBalance;
import org.joyqueue.client.loadbalance.adaptive.config.AdaptiveLoadBalanceConfig;
import org.joyqueue.client.loadbalance.adaptive.node.Metric;
import org.joyqueue.client.loadbalance.adaptive.node.Node;
import org.joyqueue.client.loadbalance.adaptive.node.Nodes;
import org.joyqueue.network.domain.BrokerNode;

import java.util.List;

/**
 * AdaptivePartitionSelector
 * author: gaohaoxiang
 * date: 2020/8/11
 */
public class AdaptivePartitionSelector extends AbstractPartitionSelector {

    public static final String NAME = "adaptive";

    protected static final String NODES_CACHE_KEY = "_ADAPTIVE_NODES_CACHE_";
    protected static final String NODE_CACHE_KEY = "_ADAPTIVE_NODE_CACHE_";

    private AdaptiveLoadBalanceConfig adaptiveLoadBalanceConfig;
    private AdaptiveLoadBalance adaptiveLoadBalance;
    private WeightedPartitionSelector weightedPartitionSelector;

    public AdaptivePartitionSelector() {
        // TODO 处理配置
        this.adaptiveLoadBalanceConfig = new AdaptiveLoadBalanceConfig();
        this.adaptiveLoadBalance = new AdaptiveLoadBalance(adaptiveLoadBalanceConfig);
        this.weightedPartitionSelector = new WeightedPartitionSelector();
    }

    @Override
    protected PartitionNode nextPartition(ProduceMessage message, TopicMetadata topicMetadata, List<BrokerNode> brokerNodes) {
        if (topicMetadata.getProducerPolicy() != null && MapUtils.isNotEmpty(topicMetadata.getProducerPolicy().getWeight())) {
            return weightedPartitionSelector.select(message, topicMetadata, brokerNodes);
        }

        Nodes topicNodes = getTopicNodes(topicMetadata, brokerNodes);
        Node selectedNode = adaptiveLoadBalance.select(topicNodes);
        BrokerNode brokerNode = topicMetadata.getBroker(Integer.valueOf(selectedNode.getUrl()));
        PartitionMetadata partitionMetadata = randomSelectPartition(topicMetadata, brokerNode);
        return new AdaptivePartitionNode(partitionMetadata, topicNodes, selectedNode);
    }

    protected Nodes getTopicNodes(TopicMetadata topicMetadata, List<BrokerNode> brokerNodes) {
        Nodes cacheNodes = topicMetadata.getAttachment(NODES_CACHE_KEY);
        if (cacheNodes == null) {
            cacheNodes = new Nodes();
            Nodes oldNodes = topicMetadata.putIfAbsentAttachment(NODES_CACHE_KEY, cacheNodes);
            if (oldNodes != null) {
                cacheNodes = oldNodes;
            }
        }

        List<Node> cacheNodeList = Lists.newArrayListWithCapacity(brokerNodes.size());
        for (BrokerNode brokerNode : brokerNodes) {
            Node node = brokerNode.getAttachment(NODE_CACHE_KEY);
            if (node == null) {
                node = new Node();
                node.setUrl(String.valueOf(brokerNode.getId()));
                node.setNearby(brokerNode.isNearby());

                Node oldNode = brokerNode.putIfAbsentAttachment(NODE_CACHE_KEY, node);
                if (oldNode != null) {
                    node = oldNode;
                }
            }
            cacheNodeList.add(node);
        }

        Nodes nodes = new Nodes();
        nodes.setMetric(cacheNodes.getMetric());
        nodes.setNodes(cacheNodeList);
        return nodes;
    }

    @Override
    public String type() {
        return NAME;
    }

    public static class AdaptivePartitionNode extends PartitionNode {
        private PartitionMetadata partitionMetadata;
        private Nodes nodes;
        private Node node;

        public AdaptivePartitionNode(PartitionMetadata partitionMetadata, Nodes nodes, Node node) {
            super(partitionMetadata);
            this.nodes = nodes;
            this.node = node;
        }

        @Override
        public PartitionNodeTracer begin() {
            Metric.Tracer nodesTracer = nodes.getMetric().begin();
            Metric.Tracer nodeTracer = node.getMetric().begin();

            return new AdaptivePartitionNodeTracer(nodesTracer, nodeTracer);
        }

        public static class AdaptivePartitionNodeTracer extends PartitionNodeTracer {

            private Metric.Tracer nodesTracer;
            private Metric.Tracer nodeTracer;

            public AdaptivePartitionNodeTracer(Metric.Tracer nodesTracer, Metric.Tracer nodeTracer) {
                this.nodesTracer = nodesTracer;
                this.nodeTracer = nodeTracer;
            }

            @Override
            public void error() {
                nodesTracer.error();
                nodeTracer.error();
            }

            @Override
            public void end() {
                nodesTracer.end();
                nodeTracer.end();
            }
        }
    }
}