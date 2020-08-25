package org.joyqueue.client.loadbalance.adaptive;

import org.joyqueue.client.loadbalance.adaptive.config.AdaptiveLoadBalanceConfig;
import org.joyqueue.client.loadbalance.adaptive.node.Metric;
import org.joyqueue.client.loadbalance.adaptive.node.Node;
import org.joyqueue.client.loadbalance.adaptive.node.Nodes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * AdaptiveLoadBalanceTest
 * author: gaohaoxiang
 * date: 2020/8/10
 */
public class AdaptiveLoadBalanceTest {

    private Nodes nodes;
    private Node node1;
    private Node node2;
    private Node node3;
    private AdaptiveLoadBalance adaptiveLoadBalance;

    @Before
    public void before() {
        AdaptiveLoadBalanceConfig config = new AdaptiveLoadBalanceConfig();
        adaptiveLoadBalance = new AdaptiveLoadBalance(config);

        node1 = Mockito.mock(Node.class);
        Mockito.when(node1.getUrl()).thenReturn("node1");
        Mockito.when(node1.isNearby()).thenReturn(false);
        Mockito.when(node1.getMetric()).thenReturn(Mockito.mock(Metric.class));
        Mockito.when(node1.getMetric().getTp99()).thenReturn(0.1);

        node2 = Mockito.mock(Node.class);
        Mockito.when(node2.getUrl()).thenReturn("node2");
        Mockito.when(node2.isNearby()).thenReturn(false);
        Mockito.when(node2.getMetric()).thenReturn(Mockito.mock(Metric.class));
        Mockito.when(node2.getMetric().getTp99()).thenReturn(0.2);

        node3 = Mockito.mock(Node.class);
        Mockito.when(node3.getUrl()).thenReturn("node3");
        Mockito.when(node3.isNearby()).thenReturn(false);
        Mockito.when(node3.getMetric()).thenReturn(Mockito.mock(Metric.class));
        Mockito.when(node3.getMetric().getTp99()).thenReturn(0.3);

        nodes = Mockito.mock(Nodes.class);
        Mockito.when(nodes.getMetric()).thenReturn(Mockito.mock(Metric.class));
        Mockito.when(nodes.getMetric().getTp99()).thenReturn(0.2);
        Mockito.when(nodes.getMetric().getTps()).thenReturn(100L);
        Mockito.when(nodes.getNodes()).thenReturn(Arrays.asList(node1, node2, node3)).getMock();
    }

    protected Map<String, Integer> execute() {
        Map<String, Integer> counterMap = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            Node node = adaptiveLoadBalance.select(nodes);
            if (node != null) {
                int counter = counterMap.getOrDefault(node.getUrl(), 0);
                counter++;
                counterMap.put(node.getUrl(), counter);
            }
        }
        return counterMap;
    }

    @Test
    public void simpleTest() {
        Map<String, Integer> counterMap = execute();

        Assert.assertTrue(counterMap.get("node1") > counterMap.get("node2"));
        Assert.assertTrue(counterMap.get("node2") > counterMap.get("node3"));
    }

    @Test
    public void regionTest() {
        Mockito.when(node3.isNearby()).thenReturn(true);

        Map<String, Integer> counterMap = execute();

        Assert.assertTrue(counterMap.get("node3") > counterMap.get("node1"));
        Assert.assertTrue(counterMap.get("node1") > counterMap.get("node2"));
    }

    @Test
    public void errorTest() {
        Mockito.when(node1.getMetric().getErrorTps()).thenReturn(1L);
        Mockito.when(node2.getMetric().getErrorTps()).thenReturn(2L);
        Mockito.when(node3.getMetric().getErrorTps()).thenReturn(3L);
        Mockito.when(nodes.getMetric().getErrorTps()).thenReturn(5L);

        Map<String, Integer> counterMap = execute();

//        Assert.assertTrue(counterMap.get("node1") >= counterMap.get("node2"));
//        Assert.assertTrue(counterMap.get("node2") >= counterMap.get("node3"));
    }

    @Test
    public void regionAndErrorTest() {
        Mockito.when(node3.getMetric().getErrorTps()).thenReturn(3L);
        Mockito.when(node3.isNearby()).thenReturn(true);
        Mockito.when(nodes.getMetric().getErrorTps()).thenReturn(3L);

        Map<String, Integer> counterMap = execute();

        Assert.assertTrue(counterMap.get("node1") > counterMap.get("node3"));
        Assert.assertTrue(counterMap.get("node1") > counterMap.get("node2"));

        Mockito.when(node3.isNearby()).thenReturn(false);

        counterMap = execute();

        Assert.assertTrue(counterMap.get("node1") > counterMap.get("node2"));
//        Assert.assertTrue(counterMap.get("node2") > counterMap.get("node3"));
    }

    @Test
    public void customJudgeTest() {
        AdaptiveLoadBalanceConfig config = new AdaptiveLoadBalanceConfig();
        config.setJudges(new String[] {"tp99"});
        adaptiveLoadBalance = new AdaptiveLoadBalance(config);

        Mockito.when(node3.isNearby()).thenReturn(true);

        Map<String, Integer> counterMap = execute();

        Assert.assertTrue(counterMap.get("node1") > counterMap.get("node2"));
        Assert.assertTrue(counterMap.get("node2") > counterMap.get("node3"));
    }

    @Test
    public void randomTest() {
        Mockito.when(nodes.getMetric().getTps()).thenReturn(0L).getMock();

        Map<String, Integer> counterMap = execute();

        Assert.assertTrue(counterMap.containsKey("node1"));
        Assert.assertTrue(counterMap.containsKey("node2"));
        Assert.assertTrue(counterMap.containsKey("node3"));
    }

    @Test
    public void singoleNodeTest() {
        Mockito.when(nodes.getNodes()).thenReturn(Arrays.asList(node1)).getMock();

        Map<String, Integer> counterMap = execute();

        Assert.assertTrue(counterMap.containsKey("node1"));
        Assert.assertTrue(!counterMap.containsKey("node2"));
        Assert.assertTrue(!counterMap.containsKey("node3"));
    }

    @Test
    public void emptyNodeTest() {
        Mockito.when(nodes.getNodes()).thenReturn(Collections.emptyList()).getMock();
        Map<String, Integer> counterMap = execute();
        Assert.assertTrue(counterMap.isEmpty());
    }
}