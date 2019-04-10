package com.jd.journalq.nsr.network;

import com.jd.journalq.network.event.TransportEvent;
import com.jd.journalq.network.transport.codec.Codec;
import com.jd.journalq.network.transport.command.handler.CommandHandlerFactory;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.network.transport.support.DefaultTransportServerFactory;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class NsrTransportServerFactory extends DefaultTransportServerFactory {
    private static NsrCommandHandlerFactory nsrCommandHandlerFactory = new NsrServerCommandHandlerFactory();
    protected static EventBus<TransportEvent> eventEventBus = new EventBus<>();
    public NsrTransportServerFactory(NameService nameService){
        this(NsrCodecFactory.getInstance(),nsrCommandHandlerFactory,new NsrExceptionHandler(),eventEventBus);
        nsrCommandHandlerFactory.register(nameService);
    }

    public NsrTransportServerFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler, EventBus<TransportEvent> eventBus) {
        super(codec, commandHandlerFactory, exceptionHandler, eventBus);
        this.eventEventBus = eventBus;
    }

    static class NsrServerCommandHandlerFactory extends NsrCommandHandlerFactory {

        protected static final Logger logger = LoggerFactory.getLogger(NsrCommandHandlerFactory.class);

        @Override
        public String getType() {
            return NsrCommandHandler.SERVER_TYPE;
        }

        @Override
        public void doWithHandler(NsrCommandHandler nsrCommandHandler) {
            if(nsrCommandHandler instanceof EventListener){
                eventEventBus.addListener((EventListener<TransportEvent>) nsrCommandHandler);
            }
        }
    }
}
