package org.joyqueue.client.loadbalance.adaptive;

import org.joyqueue.client.loadbalance.adaptive.judge.RegionScoreJudge;
import org.joyqueue.client.loadbalance.adaptive.node.Metric;
import org.joyqueue.client.loadbalance.adaptive.node.Node;
import org.joyqueue.client.loadbalance.adaptive.node.Nodes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

/**
 * TP99ScoreJudgeTest
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class RegionScoreJudgeTest {

    private Nodes nodes;
    private Node node1;
    private Node node2;
    private Node node3;
    private RegionScoreJudge regionScoreJudge;

    @Before
    public void before() {
        regionScoreJudge = new RegionScoreJudge();

        node1 = Mockito.mock(Node.class);
        Mockito.when(node1.isNearby()).thenReturn(true);

        node2 = Mockito.mock(Node.class);
        Mockito.when(node2.isNearby()).thenReturn(true);

        node3 = Mockito.mock(Node.class);
        Mockito.when(node3.isNearby()).thenReturn(false);

        nodes = Mockito.mock(Nodes.class);
        Mockito.when(nodes.getMetric()).thenReturn(Mockito.mock(Metric.class));
        Mockito.when(nodes.getNodes()).thenReturn(Arrays.asList(node1, node2, node3)).getMock();
    }

    @Test
    public void computeTest() {
        double compute1 = regionScoreJudge.compute(nodes, node1);
        double compute2 = regionScoreJudge.compute(nodes, node2);
        double compute3 = regionScoreJudge.compute(nodes, node3);

        Assert.assertTrue(compute1 <= 100);
        Assert.assertTrue(compute2 <= 100);
        Assert.assertTrue(compute3 <= 100);
        Assert.assertTrue((compute1 == compute2) && (compute2 > compute3));
    }
}