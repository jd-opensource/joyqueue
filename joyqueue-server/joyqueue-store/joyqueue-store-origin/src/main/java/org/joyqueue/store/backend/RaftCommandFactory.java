package org.joyqueue.store.backend;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.consumer.handler.ReplicateConsumePosRequestHandler;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import org.joyqueue.store.ha.election.ElectionService;
import org.joyqueue.store.ha.election.handler.AppendEntriesRequestHandler;
import org.joyqueue.store.ha.election.handler.TimeoutNowRequestHandler;
import org.joyqueue.store.ha.election.handler.VoteRequestHandler;

public class RaftCommandFactory extends DefaultCommandHandlerFactory {
    private BrokerContext brokerContext;
    private ElectionService electionService;
    public RaftCommandFactory(BrokerContext brokerContext, ElectionService electionService){
        this.brokerContext = brokerContext;
        this.electionService=electionService;
        registerRaftCommandHandler();
    }

    /**
     *
     * Register raft command handler
     **/
    public void registerRaftCommandHandler(){
        // raft related command
        register(CommandType.RAFT_VOTE_REQUEST, new VoteRequestHandler(electionService));
        register(CommandType.RAFT_APPEND_ENTRIES_REQUEST, new AppendEntriesRequestHandler(electionService));
        register(CommandType.RAFT_TIMEOUT_NOW_REQUEST, new TimeoutNowRequestHandler(electionService));
        register(CommandType.REPLICATE_CONSUME_POS_REQUEST, new ReplicateConsumePosRequestHandler(brokerContext));
    }

}
