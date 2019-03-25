package com.jd.journalq.broker.retry;

import com.jd.journalq.common.network.session.Joint;
import com.jd.journalq.toolkit.service.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 重试概率
 * <br>
 * 概率分布在0~100之间，都是整数
 * <p>
 * Created by chengzhiliang on 2018/9/7.
 */
public class RetryProbability extends Service {

    // 选择重试概率的最大值
    private int maxProbability = 100;
    // 选择重试概率的最小值
    private int minProbability = 2;
    // 选择重试概率的默认值
    private int defaultProbability = 5;

    // 消费者->选择重试的概率; 重试概率缓存
    private final Map<Joint, Integer> probabilityCache = new ConcurrentHashMap<>();

    public RetryProbability() {
    }

    public RetryProbability(int maxProbability, int minProbability, int defaultProbability) {
        this.maxProbability = maxProbability;
        this.minProbability = minProbability;
        this.defaultProbability = defaultProbability;
    }

    /**
     * 增加选择重试消息的概率
     *
     * @param joint
     */
    public void increase(Joint joint) {
        Integer probability = probabilityCache.get(joint);
        if (probability == null) {
            probability = defaultProbability;
        }
        // 增加概率
        probability = probability * probability;
        // 概率不能大于最大值
        if (probability > maxProbability) {
            probability = maxProbability;
        }
        probabilityCache.put(joint, probability);
    }

    /**
     * 减少选择重试消息的概率
     *
     * @param joint
     */
    public void decrease(Joint joint) {
        Integer probability = probabilityCache.get(joint);
        if (probability == null) {
            probability = defaultProbability;
        }
        // 降低概率
        probability = (int) Math.sqrt(probability);
        // 概率不能小于最小值
        if (probability < minProbability) {
            probability = minProbability;
        }
        probabilityCache.put(joint, probability);
    }

    /**
     * 获取选择重试消息的概率
     *
     * @param joint
     * @return
     */
    public int getProbability(Joint joint) {
        Integer integer = probabilityCache.get(joint);
        if (integer == null) {
            integer = defaultProbability;
        }
        return integer;
    }

    public void resetMaxProbability(int maxProbability) {
        this.maxProbability = maxProbability;
    }

}
