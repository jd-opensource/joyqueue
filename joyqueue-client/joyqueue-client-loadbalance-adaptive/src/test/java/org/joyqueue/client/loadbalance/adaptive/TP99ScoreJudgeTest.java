package org.joyqueue.client.loadbalance.adaptive;

import org.joyqueue.client.loadbalance.adaptive.judge.TP99ScoreJudge;
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
public class TP99ScoreJudgeTest {

    private Nodes nodes;
    private Node node1;
    private Node node2;
    private Node node3;
    private TP99ScoreJudge tp99ScoreJudge;

    @Before
    public void before() {
        tp99ScoreJudge = new TP99ScoreJudge();

        node1 = Mockito.mock(Node.class);
        Mockito.when(node1.getMetric()).thenReturn(Mockito.mock(Metric.class));
        Mockito.when(node1.getMetric().getTp99()).thenReturn(0.1);

        node2 = Mockito.mock(Node.class);
        Mockito.when(node2.getMetric()).thenReturn(Mockito.mock(Metric.class));
        Mockito.when(node2.getMetric().getTp99()).thenReturn(0.2);

        node3 = Mockito.mock(Node.class);
        Mockito.when(node3.getMetric()).thenReturn(Mockito.mock(Metric.class));
        Mockito.when(node3.getMetric().getTp99()).thenReturn(0.3);

        nodes = Mockito.mock(Nodes.class);
        Mockito.when(nodes.getMetric()).thenReturn(Mockito.mock(Metric.class));
        Mockito.when(nodes.getMetric().getTp99()).thenReturn(0.2);
        Mockito.when(nodes.getNodes()).thenReturn(Arrays.asList(node1, node2, node3)).getMock();
    }

    @Test
    public void computeTest() {
        double compute1 = tp99ScoreJudge.compute(nodes, node1);
        double compute2 = tp99ScoreJudge.compute(nodes, node2);
        double compute3 = tp99ScoreJudge.compute(nodes, node3);

        Assert.assertTrue(compute1 <= 100);
        Assert.assertTrue(compute2 <= 100);
        Assert.assertTrue(compute3 <= 100);
        Assert.assertTrue((compute1 > compute2) && (compute2 >= compute3));
    }
}