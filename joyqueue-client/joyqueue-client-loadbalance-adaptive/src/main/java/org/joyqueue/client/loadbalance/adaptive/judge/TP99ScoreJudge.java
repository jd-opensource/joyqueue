package org.joyqueue.client.loadbalance.adaptive.judge;

import org.joyqueue.client.loadbalance.adaptive.ScoreJudge;
import org.joyqueue.client.loadbalance.adaptive.node.Node;
import org.joyqueue.client.loadbalance.adaptive.node.Nodes;

/**
 * TP99ScoreJudge
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class TP99ScoreJudge implements ScoreJudge {

    private static final double BASE_SCORE = 50;

    @Override
    public double compute(Nodes nodes, Node node) {
        double score = node.getMetric().getTp99() - nodes.getMetric().getTp99();
        if (score > 0) {
            return 0;
        } else if (score == 0) {
            return BASE_SCORE;
        } else {
            return BASE_SCORE + Math.min(format(-score), BASE_SCORE);
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
        return "tp99";
    }
}