package com.jd.journalq.broker.election;

import com.jd.journalq.broker.election.handler.AppendEntriesRequestHandler;
import com.jd.journalq.broker.election.handler.ReplicateConsumePosRequestHandler;
import com.jd.journalq.broker.election.handler.TimeoutNowRequestHandler;
import com.jd.journalq.broker.election.handler.VoteRequestHandler;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.handler.CommandHandlerFactory;
import com.jd.journalq.network.transport.command.support.DefaultCommandHandlerFactory;

/**
 * Created by zhuduohui on 2018/10/8.
 */
public class ElectionCommandHandlerRegistrarStub {
    public static CommandHandlerFactory register(ElectionManager electionManager, DefaultCommandHandlerFactory commandHandlerFactory) {
        commandHandlerFactory.register(CommandType.RAFT_VOTE_REQUEST, new VoteRequestHandler(electionManager));
        commandHandlerFactory.register(CommandType.RAFT_APPEND_ENTRIES_REQUEST, new AppendEntriesRequestHandler(electionManager));
        commandHandlerFactory.register(CommandType.RAFT_TIMEOUT_NOW_REQUEST, new TimeoutNowRequestHandler(electionManager));
        commandHandlerFactory.register(CommandType.REPLICATE_CONSUME_POS_REQUEST, new ReplicateConsumePosRequestHandler(new ConsumeStub()));
        return commandHandlerFactory;
    }
}
