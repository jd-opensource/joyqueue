package org.joyqueue.client.loadbalance.adaptive;

import org.joyqueue.client.loadbalance.adaptive.config.AdaptiveLoadBalanceConfig;
import org.joyqueue.client.loadbalance.adaptive.node.Metrics;
import org.joyqueue.client.loadbalance.adaptive.node.Node;
import org.joyqueue.client.loadbalance.adaptive.node.Nodes;
import org.joyqueue.client.loadbalance.adaptive.node.WeightNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * AdaptiveLoadBalance
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class AdaptiveLoadBalance {

    protected static final Logger logger = LoggerFactory.getLogger(AdaptiveLoadBalance.class);

    private AdaptiveLoadBalanceConfig config;
    private List<ScoreJudge> scoreJudges;

    private WeightLoadBalance weightLoadBalance = new WeightLoadBalance();
    private RandomLoadBalance randomLoadBalance = new RandomLoadBalance();

    public AdaptiveLoadBalance(AdaptiveLoadBalanceConfig config) {
        this.config = config;
        this.scoreJudges = getScoreJudges(config);
        Metrics.cacheInterval = config.getCacheInterval();
        Metrics.sliceInterval = config.getSliceInterval();
    }

    protected List<ScoreJudge> getScoreJudges(AdaptiveLoadBalanceConfig config) {
        return ScoreJudgeManager.getJudges(config.getJudges());
    }

    public Node select(Nodes nodes) {
        if (nodes.getNodes() == null || nodes.getNodes().isEmpty()) {
            return null;
        }
        if (nodes.getNodes().size() == 1) {
            return nodes.getNodes().get(0);
        }
        if (!isStartup(nodes)) {
            return randomSelect(nodes.getNodes());
        }
        return adaptiveSelect(nodes);
    }

    protected boolean isStartup(Nodes nodes) {
        return nodes.getMetric().getCount() > config.getSsthreshhold();
    }

    protected Node adaptiveSelect(Nodes nodes) {
        return doAdaptiveSelect(nodes);
    }

    public Node doAdaptiveSelect(Nodes nodes) {
        List<WeightNode> weightNodes = new ArrayList<>(nodes.getNodes().size());
        for (Node node : nodes.getNodes()) {
            double score = 0;
            for (ScoreJudge scoreJudge : scoreJudges) {
                double compute = scoreJudge.compute(nodes, node);
                score += (compute / 100 * scoreJudge.getRatio());
            }
            weightNodes.add(new WeightNode(node, score));
        }
        return weightSelect(weightNodes);
    }

    protected Node weightSelect(List<WeightNode> nodes) {
        return weightLoadBalance.select(nodes).getNode();
    }

    protected Node randomSelect(List<Node> nodes) {
        return randomLoadBalance.select(nodes);
    }
}