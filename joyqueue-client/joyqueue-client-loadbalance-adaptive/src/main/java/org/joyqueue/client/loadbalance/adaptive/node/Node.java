package org.joyqueue.client.loadbalance.adaptive.node;

/**
 * Node
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class Node {

    private Metric metric = new Metric();
    private String url;
    private boolean nearby;

    public Node() {
    }

    public Node(String url, boolean nearby) {
        this.url = url;
        this.nearby = nearby;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setNearby(boolean nearby) {
        this.nearby = nearby;
    }

    public boolean isNearby() {
        return nearby;
    }
}