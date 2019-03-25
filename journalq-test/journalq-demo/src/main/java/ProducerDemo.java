import com.alibaba.fastjson.JSON;
import com.jd.journalq.client.nameserver.NameServerConfig;
import com.jd.journalq.client.producer.MessageProducer;
import com.jd.journalq.client.producer.MessageProducerFactory;
import com.jd.journalq.client.producer.config.ProducerConfig;
import com.jd.journalq.client.producer.domain.ProduceMessage;
import com.jd.journalq.client.producer.domain.SendResult;
import com.jd.journalq.client.transport.config.TransportConfig;
import com.jd.journalq.toolkit.network.IpUtil;

public class ProducerDemo {
    public static void main(String[] args) throws Exception {
        TransportConfig transportConfig = new TransportConfig();
        transportConfig.setConnections(10);

        NameServerConfig nameServerConfig = new NameServerConfig();
        nameServerConfig.setAddress("192.168.72.109");
        nameServerConfig.setPort(50088);
        nameServerConfig.setToken("09d74863-65bc-482d-885e-8a9860e678c2");
        nameServerConfig.setApp("jmqTest");

        ProducerConfig producerConfig = new ProducerConfig();
        producerConfig.setApp(nameServerConfig.getApp());

        MessageProducer messageProducer = MessageProducerFactory.create(producerConfig, nameServerConfig, transportConfig);
        messageProducer.start();

        for (int i = 0; i < 100; i++) {
            long startTime = System.currentTimeMillis();

            for (int j = 0; j < 3000; j++) {
                ProduceMessage message = new ProduceMessage("demoTopic", "test_" + j);
                try {
                    SendResult send = messageProducer.send(message);
                    if (j % 100 == 0) {
                        System.out.println(JSON.toJSONString(send));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            System.out.println(System.currentTimeMillis() - startTime);
        }
    }
}
