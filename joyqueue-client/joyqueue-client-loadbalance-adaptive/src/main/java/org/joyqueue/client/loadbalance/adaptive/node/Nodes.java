package org.joyqueue.client.loadbalance.adaptive.node;

import java.util.List;

/**
 * Nodes
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class Nodes {

    private Metric metric = new Metric();
    private List<Node> nodes;

    public Nodes() {
    }

    public Nodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}