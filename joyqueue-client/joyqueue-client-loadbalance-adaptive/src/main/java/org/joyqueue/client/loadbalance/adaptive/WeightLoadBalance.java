package org.joyqueue.client.loadbalance.adaptive;

import org.joyqueue.client.loadbalance.adaptive.node.WeightNode;

import java.util.List;

/**
 * WeightLoadBalance
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class WeightLoadBalance {

    public WeightNode select(List<WeightNode> nodes) {
        double[] weights = new double[nodes.size()];
        double weight = 0;
        int index = 0;

        for (WeightNode node : nodes) {
            weights[index] = node.getWeight();
            if (weights[index] < 0) {
                weights[index] = 0;
            }
            weight += weights[index];
            index++;
        }

        if (weight > 0) {
            int random = (int) (Math.random() * weight) + 1;
            weight = 0;
            for (int i = 0; i < weights.length; i++) {
                weight += weights[i];
                if (random <= weight) {
                    return nodes.get(i);
                }
            }
        }
        return nodes.get((int) (Math.random() * nodes.size()));
    }
}