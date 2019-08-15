package io.chubao.joyqueue.nsr.network;

import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import io.chubao.joyqueue.network.transport.support.DefaultTransportClientFactory;
import io.chubao.joyqueue.nsr.NameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/21
 */
public class NsrTransportClientFactory extends DefaultTransportClientFactory {
    private static NsrCommandHandlerFactory nsrCommandHandlerFactory = new NsrClientCommandHandlerFactory();
    public NsrTransportClientFactory(NameService nameService) {
        this(nsrCommandHandlerFactory);
        nsrCommandHandlerFactory.register(nameService);
    }

    public NsrTransportClientFactory(CommandHandlerFactory commandHandlerFactory) {
        this(commandHandlerFactory, new NsrExceptionHandler());
    }

    public NsrTransportClientFactory(CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        this(NsrCodecFactory.getInstance(), commandHandlerFactory, exceptionHandler);
    }

    public NsrTransportClientFactory(Codec codec) {
        super(codec);
    }

    public NsrTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory) {
        super(codec, commandHandlerFactory);
    }

    public NsrTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        super(codec, commandHandlerFactory, exceptionHandler);
    }

    static class NsrClientCommandHandlerFactory extends NsrCommandHandlerFactory {

        protected static final Logger logger = LoggerFactory.getLogger(NsrCommandHandlerFactory.class);

        @Override
        public String getType() {
            return NsrCommandHandler.THIN_TYPE;
        }

        @Override
        public void doWithHandler(NsrCommandHandler nsrCommandHandler) {
            //doNothing
        }
    }
}