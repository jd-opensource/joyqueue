package org.joyqueue.client.loadbalance.adaptive.judge;

import org.joyqueue.client.loadbalance.adaptive.ScoreJudge;
import org.joyqueue.client.loadbalance.adaptive.node.Node;
import org.joyqueue.client.loadbalance.adaptive.node.Nodes;

/**
 * AvgScoreJudge
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class AvgScoreJudge implements ScoreJudge {

    public static int threshhold = 500;

    @Override
    public double compute(Nodes nodes, Node node) {
        double score = node.getMetric().getAvg() / nodes.getMetric().getAvg() * 100;

        if (score > threshhold) {
            return -Integer.MAX_VALUE;
        } else {
            return 100;
        }
    }

    protected double format(double score) {
        if (score < 1) {
            return score * 100;
        } else if (score < 10) {
            return score * 10;
        } else {
            return Math.min(score, 100);
        }
    }

    @Override
    public double getRatio() {
        return 40;
    }

    @Override
    public String type() {
        return "avg";
    }
}