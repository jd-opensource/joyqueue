package com.jd.journalq.nsr.ignite;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.jd.journalq.nsr.ServiceProvider;
import com.jd.journalq.nsr.ignite.config.IgniteConfigKey;
import com.jd.journalq.nsr.ignite.dao.BrokerDao;
import com.jd.journalq.nsr.ignite.dao.ConfigDao;
import com.jd.journalq.nsr.ignite.dao.ConsumerConfigDao;
import com.jd.journalq.nsr.ignite.dao.ConsumerDao;
import com.jd.journalq.nsr.ignite.dao.TopicDao;
import com.jd.journalq.nsr.ignite.dao.PartitionGroupDao;
import com.jd.journalq.nsr.ignite.dao.PartitionGroupReplicaDao;
import com.jd.journalq.nsr.ignite.dao.DataCenterDao;
import com.jd.journalq.nsr.ignite.dao.NamespaceDao;
import com.jd.journalq.nsr.ignite.dao.ProducerDao;
import com.jd.journalq.nsr.ignite.dao.ProducerConfigDao;
import com.jd.journalq.nsr.ignite.dao.AppTokenDao;

import com.jd.journalq.nsr.ignite.dao.impl.IgniteBrokerDao;
import com.jd.journalq.nsr.ignite.dao.impl.IgniteConfigDao;
import com.jd.journalq.nsr.ignite.dao.impl.IgniteConsumerConfigDao;
import com.jd.journalq.nsr.ignite.dao.impl.IgniteConsumerDao;
import com.jd.journalq.nsr.ignite.dao.impl.IgniteTopicDao;
import com.jd.journalq.nsr.ignite.dao.impl.IgnitePartitionGroupDao;
import com.jd.journalq.nsr.ignite.dao.impl.IgnitePartitionGroupReplicaDao;
import com.jd.journalq.nsr.ignite.dao.impl.IgniteDataCenterDao;
import com.jd.journalq.nsr.ignite.dao.impl.IgniteNamespaceDao;
import com.jd.journalq.nsr.ignite.dao.impl.IgniteProducerDao;
import com.jd.journalq.nsr.ignite.dao.impl.IgniteProducerConfigDao;
import com.jd.journalq.nsr.ignite.dao.impl.IgniteAppTokenDao;

import com.jd.journalq.nsr.ignite.message.IgniteMessenger;
import com.jd.journalq.nsr.message.Messenger;
import com.jd.journalq.nsr.service.BrokerService;
import com.jd.journalq.nsr.service.ConfigService;
import com.jd.journalq.nsr.service.ConsumerService;
import com.jd.journalq.nsr.service.ProducerService;
import com.jd.journalq.nsr.service.PartitionGroupService;
import com.jd.journalq.nsr.service.PartitionGroupReplicaService;
import com.jd.journalq.nsr.service.TopicService;
import com.jd.journalq.nsr.service.AppTokenService;
import com.jd.journalq.nsr.service.DataCenterService;
import com.jd.journalq.nsr.service.NamespaceService;

import com.jd.journalq.nsr.ignite.service.IgniteBrokerService;
import com.jd.journalq.nsr.ignite.service.IgniteConfigService;
import com.jd.journalq.nsr.ignite.service.IgniteConsumerService;
import com.jd.journalq.nsr.ignite.service.IgniteProducerService;
import com.jd.journalq.nsr.ignite.service.IgnitePartitionGroupService;
import com.jd.journalq.nsr.ignite.service.IgnitePartitionGroupReplicaService;
import com.jd.journalq.nsr.ignite.service.IgniteTopicService;
import com.jd.journalq.nsr.ignite.service.IgniteAppTokenService;
import com.jd.journalq.nsr.ignite.service.IgniteDataCenterService;
import com.jd.journalq.nsr.ignite.service.IgniteNamespaceService;

import com.jd.journalq.toolkit.config.Property;
import com.jd.journalq.toolkit.config.PropertySupplier;
import com.jd.journalq.toolkit.config.PropertySupplierAware;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.service.Service;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.*;
import org.apache.ignite.failure.RestartProcessFailureHandler;
import org.apache.ignite.internal.IgnitionEx;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;

import java.util.Arrays;

import static org.apache.ignite.events.EventType.EVT_CACHE_OBJECT_PUT;
import static org.apache.ignite.events.EventType.EVT_CACHE_OBJECT_REMOVED;

public class IgniteServiceProvider extends Service implements Module, ServiceProvider, PropertySupplierAware {
    private static Injector injector;
    private PropertySupplier propertySupplier;

    public IgniteServiceProvider() {
    }

    public IgniteServiceProvider(PropertySupplier propertySupplier) {
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

            binder.bind(Messenger.class).toConstructor(IgniteMessenger.class.getConstructor(Ignite.class));
            binder.bind(BrokerService.class).toConstructor(IgniteBrokerService.class.getConstructor(BrokerDao.class));
            binder.bind(ConfigService.class).toConstructor(IgniteConfigService.class.getConstructor(ConfigDao.class));
            binder.bind(ConsumerService.class).toConstructor(IgniteConsumerService.class.getConstructor(ConsumerDao.class, ConsumerConfigDao.class));
            binder.bind(ProducerService.class).toConstructor(IgniteProducerService.class.getConstructor(ProducerDao.class, ProducerConfigDao.class));
            binder.bind(PartitionGroupService.class).toConstructor(IgnitePartitionGroupService.class.getConstructor(PartitionGroupDao.class));
            binder.bind(PartitionGroupReplicaService.class).toConstructor(IgnitePartitionGroupReplicaService.class.getConstructor(PartitionGroupReplicaDao.class));
            binder.bind(TopicService.class).toConstructor(IgniteTopicService.class.getConstructor(TopicDao.class));
            binder.bind(AppTokenService.class).toConstructor(IgniteAppTokenService.class.getConstructor(AppTokenDao.class));
            binder.bind(DataCenterService.class).toConstructor(IgniteDataCenterService.class.getConstructor(DataCenterDao.class));
            binder.bind(NamespaceService.class).toConstructor(IgniteNamespaceService.class.getConstructor(NamespaceDao.class));

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
        return IgnitionEx.start(cfg);
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

}
