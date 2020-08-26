package org.joyqueue.store.network;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.TransportClientFactory;
import org.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.network.transport.support.DefaultTransportClientFactory;
import org.joyqueue.store.ha.election.ElectionService;

/**
 * Raft client transport factory
 *
 **/
public class RaftClientTransportFactory implements TransportClientFactory {

    private DefaultTransportClientFactory transportClientFactory;
    public RaftClientTransportFactory(BrokerContext brokerContext, ElectionService electionService){
        this.transportClientFactory=new DefaultTransportClientFactory(RaftServerTransportFactory.raftCodec(),
                                                                      raftClientCommandFactory(brokerContext,electionService));
    }

    @Override
    public TransportClient create(ClientConfig config) {
        return transportClientFactory.create(config);
    }

    public CommandHandlerFactory raftClientCommandFactory( BrokerContext brokerContext, ElectionService electionService){
         return new RaftCommandFactory(brokerContext,electionService);
    }
}
