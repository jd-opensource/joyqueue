package org.joyqueue;

import com.google.common.collect.Lists;
import org.joyqueue.broker.BrokerService;
import org.joyqueue.broker.config.Args;
import org.joyqueue.broker.config.ConfigDef;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.broker.producer.Produce;
import org.joyqueue.broker.producer.PutResult;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.domain.*;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.helper.PortHelper;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.protocol.ProtocolService;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.session.Producer;
import org.joyqueue.nsr.InternalServiceProvider;
import org.joyqueue.nsr.NameService;
import org.joyqueue.nsr.ServiceProvider;
import org.joyqueue.nsr.messenger.Messenger;
import org.joyqueue.plugin.SingletonController;
import org.joyqueue.store.StoreService;
import org.joyqueue.store.WriteResult;
import org.joyqueue.toolkit.io.Files;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TestClean extends Service {

    private String DEFAULT_JOYQUEUE="joyqueue";
    private String ROOT_DIR =System.getProperty("java.io.tmpdir")+ File.separator+DEFAULT_JOYQUEUE;


    @Test
    public void cleanup() throws Exception{

        Files.deleteDirectory(new File(ROOT_DIR));
    }
}
