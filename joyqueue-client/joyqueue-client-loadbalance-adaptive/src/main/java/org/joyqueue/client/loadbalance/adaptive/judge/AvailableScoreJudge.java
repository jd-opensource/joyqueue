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
        if (node.getMetric().getErrorTps() == 0) {
            return 0;
        } else if (nodes.getMetric().getErrorTps() == 0) {
            return -100;
        } else if (node.getMetric().getErrorTps() >= nodes.getMetric().getErrorTps()) {
            return -100;
        } else {
            return -Math.min(100 - ((double) (node.getMetric().getErrorTps() / nodes.getMetric().getErrorTps()) * 100), 100);
        }
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