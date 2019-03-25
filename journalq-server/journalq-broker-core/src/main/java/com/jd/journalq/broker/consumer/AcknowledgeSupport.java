package com.jd.journalq.broker.consumer;

import com.jd.journalq.common.message.MessageLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chengzhiliang on 2018/8/24.
 */
class AcknowledgeSupport {

    /**
     * 如果序号是连续顺序则返回最小值和最大值
     * <br>
     * 否则返回空
     *
     * @param locationArr
     * @return
     */
    public static long[] sortMsgLocation(MessageLocation[] locationArr) {
        // 原始长度
        int originalSize = locationArr.length;
        // 主题
        String topic = locationArr[0].getTopic();
        // 分区
        int partition = locationArr[0].getPartition();
        // 最小序号（用数组第一个元素的初始化，后续比较替换）
        long minIndex = locationArr[0].getIndex();
        // 最大序号（用数组第一个元素的初始化，后续比较替换）
        long maxIndex = locationArr[0].getIndex();
        // 用于判重
        Set<Long> set = new HashSet<>(originalSize);
        for (MessageLocation item : locationArr) {
            // 必须是同一个主题、分区，如果不是相同主题或分区，则返回空
            if (!StringUtils.equals(topic, item.getTopic()) || partition != item.getPartition()){
                return null;
            }
            long index = item.getIndex();
            set.add(index);
            if (index <= minIndex) {
                minIndex = index;
            } else if (index > maxIndex){
                maxIndex = index;
            }
        }
        // 去重之后的大小
        int currentSize = set.size();
        // 判断连续（等差数列的最大值 = 最小值 + 数列长度）
        long newMaxIndex = minIndex + set.size() - 1;
        // 同时满足大小相等，并且等差数列的（最大值 = 最小值 + 数列长度），认为连续顺序
        long[] rst = null;
        if (originalSize == currentSize && maxIndex == newMaxIndex) {
            rst = new long[]{minIndex, maxIndex};
        }

        return rst;
    }
}
