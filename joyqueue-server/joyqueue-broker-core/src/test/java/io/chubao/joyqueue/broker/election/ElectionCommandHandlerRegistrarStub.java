package io.chubao.joyqueue.broker.election;

import io.chubao.joyqueue.broker.config.Configuration;
import io.chubao.joyqueue.broker.election.handler.AppendEntriesRequestHandler;
import io.chubao.joyqueue.broker.election.handler.ReplicateConsumePosRequestHandler;
import io.chubao.joyqueue.broker.election.handler.TimeoutNowRequestHandler;
import io.chubao.joyqueue.broker.election.handler.VoteRequestHandler;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import io.chubao.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;

/**
 * Created by zhuduohui on 2018/10/8.
 */
public class ElectionCommandHandlerRegistrarStub {
    public static CommandHandlerFactory register(ElectionManager electionManager, DefaultCommandHandlerFactory commandHandlerFactory) {
        commandHandlerFactory.register(CommandType.RAFT_VOTE_REQUEST, new VoteRequestHandler(electionManager));
        commandHandlerFactory.register(CommandType.RAFT_APPEND_ENTRIES_REQUEST, new AppendEntriesRequestHandler(electionManager));
        commandHandlerFactory.register(CommandType.RAFT_TIMEOUT_NOW_REQUEST, new TimeoutNowRequestHandler(electionManager));
        commandHandlerFactory.register(CommandType.REPLICATE_CONSUME_POS_REQUEST, new ReplicateConsumePosRequestHandler(
                new ElectionConfig(new Configuration()), new ConsumeStub()));
        return commandHandlerFactory;
    }
}
