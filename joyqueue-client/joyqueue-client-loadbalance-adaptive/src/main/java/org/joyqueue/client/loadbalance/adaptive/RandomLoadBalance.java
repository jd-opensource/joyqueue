package org.joyqueue.client.loadbalance.adaptive;

import org.joyqueue.client.loadbalance.adaptive.node.Node;

import java.util.List;
import java.util.Random;

/**
 * WeightLoadBalance
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class RandomLoadBalance {

    private static final Random RANDOM = new Random();

    public Node select(List<Node> nodes) {
        return nodes.get(RANDOM.nextInt(nodes.size()));
    }
}