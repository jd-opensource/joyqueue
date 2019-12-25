/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.nsr.ignite;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.joyqueue.nsr.InternalServiceProvider;
import org.joyqueue.nsr.ignite.config.IgniteConfigKey;
import org.joyqueue.nsr.ignite.dao.AppTokenDao;
import org.joyqueue.nsr.ignite.dao.BrokerDao;
import org.joyqueue.nsr.ignite.dao.ConfigDao;
import org.joyqueue.nsr.ignite.dao.ConsumerConfigDao;
import org.joyqueue.nsr.ignite.dao.ConsumerDao;
import org.joyqueue.nsr.ignite.dao.DataCenterDao;
import org.joyqueue.nsr.ignite.dao.NamespaceDao;
import org.joyqueue.nsr.ignite.dao.PartitionGroupDao;
import org.joyqueue.nsr.ignite.dao.PartitionGroupReplicaDao;
import org.joyqueue.nsr.ignite.dao.ProducerConfigDao;
import org.joyqueue.nsr.ignite.dao.ProducerDao;
import org.joyqueue.nsr.ignite.dao.TopicDao;
import org.joyqueue.nsr.ignite.dao.impl.IgniteAppTokenDao;
import org.joyqueue.nsr.ignite.dao.impl.IgniteBrokerDao;
import org.joyqueue.nsr.ignite.dao.impl.IgniteConfigDao;
import org.joyqueue.nsr.ignite.dao.impl.IgniteConsumerConfigDao;
import org.joyqueue.nsr.ignite.dao.impl.IgniteConsumerDao;
import org.joyqueue.nsr.ignite.dao.impl.IgniteDataCenterDao;
import org.joyqueue.nsr.ignite.dao.impl.IgniteNamespaceDao;
import org.joyqueue.nsr.ignite.dao.impl.IgnitePartitionGroupDao;
import org.joyqueue.nsr.ignite.dao.impl.IgnitePartitionGroupReplicaDao;
import org.joyqueue.nsr.ignite.dao.impl.IgniteProducerConfigDao;
import org.joyqueue.nsr.ignite.dao.impl.IgniteProducerDao;
import org.joyqueue.nsr.ignite.dao.impl.IgniteTopicDao;
import org.joyqueue.nsr.ignite.message.IgniteMessenger;
import org.joyqueue.nsr.ignite.service.IgniteAppTokenInternalService;
import org.joyqueue.nsr.ignite.service.IgniteBrokerInternalService;
import org.joyqueue.nsr.ignite.service.IgniteConfigInternalService;
import org.joyqueue.nsr.ignite.service.IgniteConsumerInternalService;
import org.joyqueue.nsr.ignite.service.IgniteDataCenterInternalService;
import org.joyqueue.nsr.ignite.service.IgniteNamespaceInternalService;
import org.joyqueue.nsr.ignite.service.IgnitePartitionGroupInternalService;
import org.joyqueue.nsr.ignite.service.IgnitePartitionGroupReplicaInternalService;
import org.joyqueue.nsr.ignite.service.IgniteProducerInternalService;
import org.joyqueue.nsr.ignite.service.IgniteTopicInternalService;
import org.joyqueue.nsr.service.internal.AppTokenInternalService;
import org.joyqueue.nsr.service.internal.BrokerInternalService;
import org.joyqueue.nsr.service.internal.ConfigInternalService;
import org.joyqueue.nsr.service.internal.ConsumerInternalService;
import org.joyqueue.nsr.service.internal.DataCenterInternalService;
import org.joyqueue.nsr.service.internal.NamespaceInternalService;
import org.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import org.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;
import org.joyqueue.nsr.service.internal.ProducerInternalService;
import org.joyqueue.nsr.service.internal.TopicInternalService;
import org.joyqueue.nsr.service.internal.TransactionInternalService;
import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.joyqueue.toolkit.service.Service;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.BinaryConfiguration;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.DeploymentMode;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.WALMode;
import org.apache.ignite.failure.RestartProcessFailureHandler;
import org.apache.ignite.internal.IgnitionEx;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;

import java.util.Arrays;

import static org.apache.ignite.events.EventType.EVT_CACHE_OBJECT_PUT;
import static org.apache.ignite.events.EventType.EVT_CACHE_OBJECT_REMOVED;

public class IgniteInternalServiceProvider extends Service implements Module, InternalServiceProvider, PropertySupplierAware {
    private static Injector injector;
    private PropertySupplier propertySupplier;
    private String name = null;

    public IgniteInternalServiceProvider() {
    }

    public IgniteInternalServiceProvider(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        Preconditions.checkArgument(propertySupplier != null, "property supplier can not be null.");

    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        injector = Guice.createInjector(this);
    }

    @Override
    public void configure(Binder binder) {
        try {
            Ignite ignite = startIgnite();
            name = ignite.name();
            ignite.cluster().active(true);
            ignite.cluster().setBaselineTopology(ignite.cluster().topologyVersion());
            binder.bind(Ignite.class).toInstance(ignite);
            binder.bind(BrokerDao.class).toInstance(new IgniteBrokerDao(ignite));
            binder.bind(ConfigDao.class).toInstance(new IgniteConfigDao(ignite));
            binder.bind(ConsumerConfigDao.class).toInstance(new IgniteConsumerConfigDao(ignite));
            binder.bind(ConsumerDao.class).toInstance(new IgniteConsumerDao(ignite));
            binder.bind(ProducerDao.class).toInstance(new IgniteProducerDao(ignite));
            binder.bind(TopicDao.class).toInstance(new IgniteTopicDao(ignite));
            binder.bind(PartitionGroupDao.class).toInstance(new IgnitePartitionGroupDao(ignite));
            binder.bind(ProducerConfigDao.class).toInstance(new IgniteProducerConfigDao(ignite));
            binder.bind(PartitionGroupReplicaDao.class).toInstance(new IgnitePartitionGroupReplicaDao(ignite));
            binder.bind(AppTokenDao.class).toInstance(new IgniteAppTokenDao(ignite));
            binder.bind(DataCenterDao.class).toInstance(new IgniteDataCenterDao(ignite));
            binder.bind(NamespaceDao.class).toInstance(new IgniteNamespaceDao(ignite));

            binder.bind(IgniteMessenger.class).toConstructor(IgniteMessenger.class.getConstructor(Ignite.class));
            binder.bind(BrokerInternalService.class).toConstructor(IgniteBrokerInternalService.class.getConstructor(BrokerDao.class));
            binder.bind(ConfigInternalService.class).toConstructor(IgniteConfigInternalService.class.getConstructor(ConfigDao.class));
            binder.bind(ConsumerInternalService.class).toConstructor(IgniteConsumerInternalService.class.getConstructor(ConsumerDao.class, ConsumerConfigDao.class));
            binder.bind(ProducerInternalService.class).toConstructor(IgniteProducerInternalService.class.getConstructor(ProducerDao.class, ProducerConfigDao.class));
            binder.bind(PartitionGroupInternalService.class).toConstructor(IgnitePartitionGroupInternalService.class.getConstructor(PartitionGroupDao.class));
            binder.bind(PartitionGroupReplicaInternalService.class).toConstructor(IgnitePartitionGroupReplicaInternalService.class.getConstructor(PartitionGroupReplicaDao.class));
            binder.bind(TopicInternalService.class).toConstructor(IgniteTopicInternalService.class.getConstructor(TopicDao.class));
            binder.bind(AppTokenInternalService.class).toConstructor(IgniteAppTokenInternalService.class.getConstructor(AppTokenDao.class));
            binder.bind(DataCenterInternalService.class).toConstructor(IgniteDataCenterInternalService.class.getConstructor(DataCenterDao.class));
            binder.bind(NamespaceInternalService.class).toConstructor(IgniteNamespaceInternalService.class.getConstructor(NamespaceDao.class));
            binder.bind(TransactionInternalService.class).toConstructor(IgniteTransactionInternalService.class.getConstructor());

        } catch (Throwable e) {
            throw new RuntimeException("init ignite bean error.", e);
        }
    }


    private Ignite startIgnite() throws Exception {
        //加载配置文件

        IgniteConfiguration cfg = new IgniteConfiguration();

        cfg.setIncludeEventTypes(EVT_CACHE_OBJECT_PUT, EVT_CACHE_OBJECT_REMOVED);
        //设为false表示服务端模式
        cfg.setClientMode(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.CLIENT_MODE));
        //分布式计算class传播
        cfg.setPeerClassLoadingEnabled(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.PEER_CLASS_LOADING_ENABLED));
        //部署模式，控制类加载
        cfg.setDeploymentMode(DeploymentMode.valueOf(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.DEPLOYMENT_MODE)));
        //禁用丢失资源缓存
        cfg.setPeerClassLoadingMissedResourcesCacheSize(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.PEER_CLASS_LOADING_MISSED_RESOURCE_CACHE_SIZE));
        //连接超时时间
        cfg.setNetworkTimeout(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.NETWORK_TIMEOUT));
        cfg.setFailureHandler(new RestartProcessFailureHandler());
        //公共线程池大小
        cfg.setPublicThreadPoolSize(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.PUBLIC_THREAD_POOL_SIZE));
        //系统线程池大小
        cfg.setSystemThreadPoolSize(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.SYSTEM_THREAD_POOL_SIZE));


        BinaryConfiguration bc = new BinaryConfiguration();
        bc.setCompactFooter(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.BINARY_COMPACT_FOOTER));
        cfg.setBinaryConfiguration(bc);

        cfg.setGridLogger(new org.apache.ignite.logger.slf4j.Slf4jLogger());
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        // 缓存名
        cacheConfiguration.setName(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.CACHE_NAME));
        //原子模式类型，ATOMIC:原子型，保证性能; TRANSACTIONAL:事务型,分布式锁
        cacheConfiguration.setAtomicityMode(CacheAtomicityMode.valueOf(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.CACHE_ATOMICITY_MODE)));
        //PARTITIONED:分区; REPLICATED:复制；LOCAL：本地
        cacheConfiguration.setCacheMode(CacheMode.valueOf(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.CACHE_MODE)));
        //备份数量
        cacheConfiguration.setBackups(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.CACHE_BACKUPS));
        //禁用jcache标准中缓存读取获取的是副本的机制
        cacheConfiguration.setCopyOnRead(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.CACHE_COPY_READ));
        //内存区名
        cacheConfiguration.setDataRegionName(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.CACHE_DATA_REGION_NAME));
        //是否以二进制形式存储
        cacheConfiguration.setStoreKeepBinary(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.CACHE_STORE_KEEP_BINARY));
        cfg.setCacheConfiguration(cacheConfiguration);


        DataStorageConfiguration dataStorageConfiguration = new DataStorageConfiguration();
        // 预写日志模式
        dataStorageConfiguration.setWalMode(WALMode.valueOf(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.STORAGE_WAL_MODE)));
        //检查点频率
        dataStorageConfiguration.setCheckpointFrequency(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.STORAGE_CHECKPOINT_FREQUENCY));
        // 检查点线程数
        dataStorageConfiguration.setCheckpointThreads(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.STORAGE_CHECKPOINT_THREADS));
        // 在检查点同步完成后预写日志历史保留数量
        dataStorageConfiguration.setWalHistorySize(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.STORAGE_WAL_HISTORY_SIZE));
        // 持久化文件路径
        Property property = propertySupplier.getProperty(Property.APPLICATION_DATA_PATH);
        String dataRoot = property == null ? "" : property.getString();
        dataStorageConfiguration.setStoragePath(dataRoot + IgniteConfigKey.STORAGE_STORE_PATH);
        dataStorageConfiguration.setWalPath(dataRoot + IgniteConfigKey.STORAGE_WAL_PATH);
        dataStorageConfiguration.setWalArchivePath(dataRoot + IgniteConfigKey.STORAGE_WAL__ARCHIVE_PATH);


        //默认region配置
        DataRegionConfiguration defaultRegionConfiguration = new DataRegionConfiguration();
        //存储区名
        defaultRegionConfiguration.setName(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.STORAGE_DEFAULT_DATA_REGION_NAME));
        //存储区大小
        defaultRegionConfiguration.setMaxSize(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.STORAGE_DEFAULT_DATA_REGION_MAX_SIZE));
        defaultRegionConfiguration.setInitialSize(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.STORAGE_DEFAULT_DATA_REGION_INITIAL_SIZE));
        defaultRegionConfiguration.setPersistenceEnabled(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.STORAGE_DEFAULT_DATA_REGION_PERSISTENCE_ENABLED));

        dataStorageConfiguration.setDefaultDataRegionConfiguration(defaultRegionConfiguration);

        //自定义region配置
        DataRegionConfiguration udfRegionConfiguration = new DataRegionConfiguration();
        //存储区名
        udfRegionConfiguration.setName(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.UDF_REGION_NAME));
        //存储区大小
        udfRegionConfiguration.setMaxSize(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.UDF_REGION_MAX_SIZE));
        udfRegionConfiguration.setInitialSize(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.UDF_REGION_INITIAL_SIZE));
        udfRegionConfiguration.setPersistenceEnabled(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.UDF_REGION_PERSISTENCE_ENABLED));
        dataStorageConfiguration.setDataRegionConfigurations(udfRegionConfiguration);


        cfg.setDataStorageConfiguration(dataStorageConfiguration);


        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
        discoverySpi.setLocalPort(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.DISCOVERY_SPI_LOCAL_PORT));
        discoverySpi.setLocalPortRange(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.DISCOVERY_SPI_LOCAL_PORT_RANGE));
        discoverySpi.setNetworkTimeout(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.DISCOVERY_SPI_NETWORK_TIMEOUT));
        discoverySpi.setJoinTimeout(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.DISCOVERY_SPI_JOIN_TIMEOUT));

        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        String ipFinderStr = PropertySupplier.getValue(propertySupplier, IgniteConfigKey.DISCOVERY_SPI_IP_FINDER_ADDRESS);
        if (ipFinderStr != null) {
            ipFinder.setAddresses(Arrays.asList(ipFinderStr.split(";,")));
        }

        discoverySpi.setIpFinder(ipFinder);


        cfg.setDiscoverySpi(discoverySpi);


        TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();

        communicationSpi.setLocalPort(PropertySupplier.getValue(propertySupplier, IgniteConfigKey.DISCOVERY_SPI_COMMUNICATION_SPI_LOCAL_PORT));
        cfg.setCommunicationSpi(communicationSpi);

        return Ignition.start(cfg);
    }

    @Override
    public <T> T getService(Class<T> clazz) {
        Preconditions.checkState(isStarted(), "provider not start yet.");
        return injector.getInstance(clazz);
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.propertySupplier = supplier;
    }

    @Override
    protected void doStop() {
        super.doStop();
        IgnitionEx.stop(name, true, true);
    }

    @Override
    public String type() {
        return "type";
    }
}
