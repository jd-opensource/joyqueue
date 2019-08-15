package io.chubao.joyqueue.broker.extension;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.toolkit.service.Service;

import java.util.List;

/**
 * ExtensionManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/13
 */
public class ExtensionManager extends Service {

    private BrokerContext brokerContext;
    private List<ExtensionService> extensionServices;

    public ExtensionManager(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        this.extensionServices = loadExtensionServices();
        initExtensionServices(extensionServices);
    }

    protected List<ExtensionService> loadExtensionServices() {
        return Lists.newArrayList(com.jd.laf.extension.ExtensionManager.getOrLoadExtensions(ExtensionService.class));
    }

    protected void initExtensionServices(List<ExtensionService> extensionServices) {
        for (ExtensionService extensionService : extensionServices) {
            extensionService.init(brokerContext);
        }
    }

    @Override
    protected void doStart() throws Exception {
        for (ExtensionService extensionService : extensionServices) {
            extensionService.start();
        }
    }

    @Override
    protected void doStop() {
        for (ExtensionService extensionService : extensionServices) {
            extensionService.stop();
        }
    }
}