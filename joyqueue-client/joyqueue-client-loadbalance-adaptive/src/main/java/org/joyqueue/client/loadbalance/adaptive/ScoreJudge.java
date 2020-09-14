package org.joyqueue.client.loadbalance.adaptive;

import org.joyqueue.client.loadbalance.adaptive.node.Node;
import org.joyqueue.client.loadbalance.adaptive.node.Nodes;

/**
 * ScoreJudge
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public interface ScoreJudge {

    double compute(Nodes nodes, Node node);

    double getRatio();

    String type();
}