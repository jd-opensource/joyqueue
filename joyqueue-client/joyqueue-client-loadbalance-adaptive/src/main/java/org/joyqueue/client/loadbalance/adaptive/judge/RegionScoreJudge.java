package org.joyqueue.client.loadbalance.adaptive.judge;

import org.joyqueue.client.loadbalance.adaptive.ScoreJudge;
import org.joyqueue.client.loadbalance.adaptive.node.Node;
import org.joyqueue.client.loadbalance.adaptive.node.Nodes;

/**
 * RegionScoreJudge
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class RegionScoreJudge implements ScoreJudge {

    @Override
    public double compute(Nodes nodes, Node node) {
        if (node.isNearby()) {
            return 100;
        }
        return 1;
    }

    @Override
    public double getRatio() {
        return 30;
    }

    @Override
    public String type() {
        return "region";
    }
}