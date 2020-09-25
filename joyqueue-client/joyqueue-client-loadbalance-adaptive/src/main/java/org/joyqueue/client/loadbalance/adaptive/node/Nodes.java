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
    private Object attachment;

    public Nodes() {
    }

    public Nodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Metric getMetric() {
        metric.refresh();
        return metric;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "Nodes{" +
                "nodes=" + nodes +
                ", attachment=" + attachment +
                '}';
    }
}