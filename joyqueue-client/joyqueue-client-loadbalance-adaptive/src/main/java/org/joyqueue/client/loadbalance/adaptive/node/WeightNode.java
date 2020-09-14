package org.joyqueue.client.loadbalance.adaptive.node;

/**
 * WeightNode
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class WeightNode {

    private Node node;
    private double weight;

    public WeightNode(Node node, double weight) {
        this.node = node;
        this.weight = weight;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}