package org.joyqueue.store.network;

import org.joyqueue.network.event.TransportEvent;
import org.joyqueue.network.transport.codec.Codec;
import org.joyqueue.network.transport.codec.PayloadCodecFactory;
import org.joyqueue.network.transport.codec.support.JoyQueueCodec;
import org.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.joyqueue.network.transport.command.handler.ExceptionHandler;
import org.joyqueue.network.transport.support.DefaultTransportServerFactory;
import org.joyqueue.store.ha.election.codec.*;
import org.joyqueue.toolkit.concurrent.EventBus;

/**
 *
 * Raft transport sever factory
 *
 **/
public class RaftServerTransportFactory extends DefaultTransportServerFactory {

    public RaftServerTransportFactory(CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler, EventBus<TransportEvent> eventBus){
        super(raftCodec(),commandHandlerFactory,exceptionHandler,eventBus);
    }

    /**
     * Raft codec
     *
     **/
    public static Codec raftCodec(){
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        // raft election command codec
        payloadCodecFactory.register(new VoteRequestDecoder());
        payloadCodecFactory.register(new VoteRequestEncoder());
        payloadCodecFactory.register(new VoteResponseDecoder());
        payloadCodecFactory.register(new VoteResponseEncoder());
        payloadCodecFactory.register(new TimeoutNowRequestDecoder());
        payloadCodecFactory.register(new TimeoutNowRequestEncoder());
        payloadCodecFactory.register(new TimeoutNowResponseDecoder());
        payloadCodecFactory.register(new TimeoutNowResponseEncoder());
        payloadCodecFactory.register(new AppendEntriesRequestDecoder());
        payloadCodecFactory.register(new AppendEntriesRequestEncoder());
        payloadCodecFactory.register(new AppendEntriesResponseDecoder());
        payloadCodecFactory.register(new AppendEntriesResponseEncoder());
        payloadCodecFactory.register(new ReplicateConsumePosRequestDecoder());
        payloadCodecFactory.register(new ReplicateConsumePosRequestEncoder());
        payloadCodecFactory.register(new ReplicateConsumePosResponseDecoder());
        payloadCodecFactory.register(new ReplicateConsumePosResponseEncoder());

        return new JoyQueueCodec(payloadCodecFactory);
    }




}
