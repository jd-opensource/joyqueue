package io.chubao.joyqueue.nsr.admin;

import io.chubao.joyqueue.domain.Broker;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

@Ignore
public class NamingTest {

    private static final String ConnectionStr="196.0.0.1:50091,127.0.0.1:50091";
    @Test
    public void multiNodeTest() throws Exception{
        AdminClient client=new AdminClient(ConnectionStr);
        BrokerAdmin.ListArg listArg=new BrokerAdmin.ListArg();
        int maxTries=3;
        List<Broker> brokers=null;
        while(maxTries-->0) {
            try {
                brokers= client.listBroker(listArg);
            } catch (Exception e) {

            }
        }
        Assert.assertNotEquals(null,brokers);
    }
}
