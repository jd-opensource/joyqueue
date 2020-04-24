package org.joyqueue.broker.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.cluster.BrokerEventBusStub;
import org.joyqueue.broker.cluster.ClusterNameService;
import org.joyqueue.broker.cluster.NameServiceStub;
import org.joyqueue.broker.cluster.StoreServiceStub;
import org.joyqueue.broker.network.codec.GetPartitionGroupClusterRequestCodec;
import org.joyqueue.broker.network.codec.GetPartitionGroupClusterResponseCodec;
import org.joyqueue.broker.network.command.GetPartitionGroupClusterRequest;
import org.joyqueue.broker.network.command.GetPartitionGroupClusterResponse;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Topic;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.transport.command.JoyQueueCommand;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GetPartitionGroupClusterRequestHandlerTest
 * author: gaohaoxiang
 * date: 2020/3/27
 */
public class GetPartitionGroupClusterRequestHandlerTest {

    private NameServiceStub nameServiceStub;
    private BrokerEventBusStub brokerEventBus;
    private PropertySupplier propertySupplier;
    private Map<String, Object> propertySupplierMap;
    private ClusterNameService clusterNameService;

    private GetPartitionGroupClusterRequestHandler getPartitionGroupClusterRequestHandler;

    @Before
    public void before() throws Exception {
        this.nameServiceStub = new NameServiceStub();
        this.brokerEventBus = new BrokerEventBusStub(null);

        this.propertySupplierMap = new HashMap<>();
        this.propertySupplier = new PropertySupplier.MapSupplier(propertySupplierMap);
        this.clusterNameService = new ClusterNameService(nameServiceStub, brokerEventBus, propertySupplier);
        this.getPartitionGroupClusterRequestHandler = new GetPartitionGroupClusterRequestHandler();

        this.nameServiceStub.start();
        this.brokerEventBus.start();
        this.clusterNameService.start();
        this.getPartitionGroupClusterRequestHandler.setBrokerContext(new BrokerContext().storeService(new StoreServiceStub(this.nameServiceStub)));
        Broker broker = new Broker();
        broker.setId(1);
        this.clusterNameService.setBroker(broker);
        initTopics();
    }

    protected void initTopics() {
        Topic topic1 = new Topic();
        topic1.setName(TopicName.parse("test1"));
        topic1.setPartitions((short) 5);
        List<PartitionGroup> topic1PartitionGroups = Lists.newArrayList();
        PartitionGroup topic1PartitionGroup1 = new PartitionGroup();
        topic1PartitionGroup1.setTopic(topic1.getName());
        topic1PartitionGroup1.setGroup(0);
        topic1PartitionGroup1.setReplicas(Sets.newHashSet(-1));
        topic1PartitionGroup1.setLeader(-1);
        topic1PartitionGroup1.setPartitions(Sets.newHashSet((short) 0, (short) 1, (short) 2, (short) 3, (short) 4));
        this.nameServiceStub.addTopic(topic1, Arrays.asList(topic1PartitionGroup1));
    }

    @After
    public void after() throws Exception {
        this.clusterNameService.stop();
        this.brokerEventBus.stop();
        this.nameServiceStub.stop();
    }

    @Test
    public void getPartitionGroupClusterTest() throws Exception {
        TopicConfig topicConfig = nameServiceStub.getTopicConfig(TopicName.parse("test1"));
        GetPartitionGroupClusterRequest getPartitionGroupClusterRequest = new GetPartitionGroupClusterRequest();
        Map<String, List<Integer>> groups = Maps.newHashMap();
        groups.put(topicConfig.getName().getFullName(), Lists.newArrayList());
        getPartitionGroupClusterRequest.setGroups(groups);
        for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
            groups.get(topicConfig.getName().getFullName()).add(entry.getKey());
        }
        groups.get(topicConfig.getName().getFullName()).add(1);

        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.heapBuffer();
        new GetPartitionGroupClusterRequestCodec().encode(getPartitionGroupClusterRequest, byteBuf);
        getPartitionGroupClusterRequest = (GetPartitionGroupClusterRequest) new GetPartitionGroupClusterRequestCodec().decode(null, byteBuf);

        GetPartitionGroupClusterResponse response = (GetPartitionGroupClusterResponse) getPartitionGroupClusterRequestHandler.handle(null, new JoyQueueCommand(getPartitionGroupClusterRequest)).getPayload();
        byteBuf = ByteBufAllocator.DEFAULT.heapBuffer();
        new GetPartitionGroupClusterResponseCodec().encode(response, byteBuf);
        response = (GetPartitionGroupClusterResponse) new GetPartitionGroupClusterResponseCodec().decode(null, byteBuf);

        Assert.assertEquals(topicConfig.getPartitionGroups().size(), response.getGroups().get(topicConfig.getName().getFullName()).size());
        for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
            GetPartitionGroupClusterResponse.PartitionGroupCluster cluster = response.getCluster(topicConfig.getName().getFullName(), entry.getKey());
            Assert.assertEquals((int) entry.getValue().getLeader(), cluster.getRWNode().getId());
        }
    }
}