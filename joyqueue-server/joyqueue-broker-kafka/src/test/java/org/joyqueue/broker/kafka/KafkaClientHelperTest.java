package org.joyqueue.broker.kafka;

import org.joyqueue.broker.kafka.helper.KafkaClientHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * KafkaClientHelperTest
 * author: gaohaoxiang
 * date: 2020/4/9
 */
public class KafkaClientHelperTest {

    @Test
    public void simpleTest() {
        Assert.assertEquals("test_app", KafkaClientHelper.parseClient("test_app"));
        Assert.assertEquals("test_app", KafkaClientHelper.parseClient("test_app-0"));
        Assert.assertEquals("test_app", KafkaClientHelper.parseClient("test_app-1"));
        Assert.assertEquals("test_app", KafkaClientHelper.parseClient("test_app-spark-executor-"));
        Assert.assertEquals("test_app", KafkaClientHelper.parseClient("test_app-MetadataFuzzySearch"));
        Assert.assertEquals("test_app", KafkaClientHelper.parseClient("test_app-1-MetadataFuzzySearch"));

        Assert.assertEquals(false, KafkaClientHelper.isMetadataFuzzySearch("test_app-spark-executor-"));
        Assert.assertEquals(true, KafkaClientHelper.isMetadataFuzzySearch("test_app-MetadataFuzzySearch"));
        Assert.assertEquals(true, KafkaClientHelper.isMetadataFuzzySearch("test_app-1-MetadataFuzzySearch"));

        Assert.assertEquals("test_app", KafkaClientHelper.parseClient("test_app@test_token"));
        Assert.assertEquals("test_app", KafkaClientHelper.parseClient("test_app@test_token-0"));
        Assert.assertEquals("test_app", KafkaClientHelper.parseClient("test_app@test_token-spark-executor-"));
        Assert.assertEquals("test_app", KafkaClientHelper.parseClient("test_app@test_token-MetadataFuzzySearch"));
        Assert.assertEquals("test_app", KafkaClientHelper.parseClient("test_app@test_token-1-MetadataFuzzySearch"));

        Assert.assertEquals("test_token", KafkaClientHelper.parseToken("test_app@test_token"));
        Assert.assertEquals("test_token", KafkaClientHelper.parseToken("test_app@test_token-0"));
        Assert.assertEquals("test_token", KafkaClientHelper.parseToken("test_app@test_token-spark-executor-"));
        Assert.assertEquals("test_token", KafkaClientHelper.parseToken("test_app@test_token-MetadataFuzzySearch"));
        Assert.assertEquals("test_token", KafkaClientHelper.parseToken("test_app@test_token-1-MetadataFuzzySearch"));
        Assert.assertEquals(null, KafkaClientHelper.parseToken("test_app-1-MetadataFuzzySearch"));
    }
}
