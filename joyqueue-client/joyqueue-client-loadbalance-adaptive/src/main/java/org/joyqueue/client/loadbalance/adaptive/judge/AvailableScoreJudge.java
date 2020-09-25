package org.joyqueue.client.loadbalance.adaptive.judge;

import org.joyqueue.client.loadbalance.adaptive.ScoreJudge;
import org.joyqueue.client.loadbalance.adaptive.node.Node;
import org.joyqueue.client.loadbalance.adaptive.node.Nodes;

/**
 * SuccessScoreJudge
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class AvailableScoreJudge implements ScoreJudge {

    @Override
    public double compute(Nodes nodes, Node node) {
        if (node.getMetric().getErrorCount() != 0) {
            return -Integer.MAX_VALUE;
        }
        return 0;
    }

    @Override
    public double getRatio() {
        return 30;
    }

    @Override
    public String type() {
        return "tps";
    }
}