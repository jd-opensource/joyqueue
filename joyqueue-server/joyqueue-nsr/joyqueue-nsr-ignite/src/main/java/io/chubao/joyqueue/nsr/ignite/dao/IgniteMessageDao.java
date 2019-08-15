package io.chubao.joyqueue.nsr.ignite.dao;

import com.google.inject.Inject;
import io.chubao.joyqueue.nsr.ignite.model.IgniteMessage;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.QueryIndex;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.Arrays;
import java.util.LinkedHashMap;

import static io.chubao.joyqueue.nsr.ignite.model.IgniteBaseModel.SCHEMA;
import static io.chubao.joyqueue.nsr.ignite.model.IgniteMessage.COLUMN_CONTENT;
import static io.chubao.joyqueue.nsr.ignite.model.IgniteMessage.COLUMN_TYPE;
import static io.chubao.joyqueue.nsr.ignite.model.IgniteMessage.COLUMN_MESSAGE_ID;

public class IgniteMessageDao extends IgniteDao {
    public static CacheConfiguration<String, IgniteMessage> cacheCfg;
    public static final String CACHE_NAME = "notify_message";

    static {
        cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName(CACHE_NAME);
        cacheCfg.setSqlSchema(SCHEMA);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setKeyType(Integer.class.getName());
        queryEntity.setValueType(IgniteMessage.class.getName());

        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put(COLUMN_MESSAGE_ID, Integer.class.getName());
        fields.put(COLUMN_TYPE, String.class.getName());
        fields.put(COLUMN_CONTENT, String.class.getName());

        queryEntity.setFields(fields);
        queryEntity.setTableName(CACHE_NAME);
        queryEntity.setIndexes(Arrays.asList(new QueryIndex(COLUMN_MESSAGE_ID)));
        cacheCfg.setQueryEntities(Arrays.asList(queryEntity));
    }

    @Inject
    public IgniteMessageDao(Ignite ignite) {
        super(ignite,cacheCfg);
    }
}
