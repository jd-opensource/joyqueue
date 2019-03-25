package com.jd.journalq.broker.consumer.filter;

import com.jd.journalq.broker.buffer.Serializer;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.message.BrokerMessage;
import com.jd.laf.extension.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 消息标签过滤器
 * <p>
 * Created by chengzhiliang on 2019/2/20.
 */
@Extension(value = "flag", singleton = false)
public class FlagFilter implements MessageFilter {

    private final Logger logger = LoggerFactory.getLogger(FlagFilter.class);
    // 已编译好的正则表达式
    private Pattern pattern;

    public FlagFilter() {

    }

    @Override
    public void setRule(String rule) {
        this.pattern = Pattern.compile(rule);
    }

    @Override
    public List<ByteBuffer> filter(List<ByteBuffer> byteBufferList, FilterCallback filterCallback) throws JMQException {
        FilterResult filterResult = doFilter(byteBufferList, pattern);
        List<ByteBuffer> inValidList = filterResult.getInValidList();

        filterCallback.callback(inValidList);

        return filterResult.getValidList();
    }

    /**
     * 过滤处理
     * <br/>
     * 顺序向后查找
     * 1.先找到不符合条件的，再找到符合条件的 --> 将不符合条件应答掉，符合条件的返回
     * 2.先找到符合条件的，再找不到不符合条件的 --> 将符合条件返回
     * 3.全部符合条件 --> 全部返回
     * 4.全部不符合条件 --> 返回空集合
     *
     * @param messages
     * @param pattern
     * @return
     */
    private FilterResult doFilter(List<ByteBuffer> messages, Pattern pattern) throws JMQException {
        List<ByteBuffer> validList = new ArrayList<>(); // 有效队列
        List<ByteBuffer> inValidList = null; // 无效队列
        boolean /* 有效到无效 */ valid2InvalidFlag = false,
                /* 无效到有效 */ invaild2ValidFlag = false;

        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer buffer = messages.get(i);
            BrokerMessage message = null;
            short flag;
            try {
                flag = Serializer.readFlag(buffer);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new JMQException(JMQCode.SE_SERIALIZER_ERROR, e);
            }

            // 是否匹配
            boolean matcher = pattern.matcher("" + flag).matches();

            if (i == 0 && !matcher) {
                // 不是有效标签开头
                inValidList = new ArrayList<>();
            }

            if (matcher && !valid2InvalidFlag) {
                validList.add(buffer);
                if (inValidList != null && inValidList.size() > 0) {
                    invaild2ValidFlag = true;
                }
            } else if (inValidList != null && !invaild2ValidFlag) {
                inValidList.add(buffer);
                if (validList.size() > 0) {
                    valid2InvalidFlag = true;
                }
            }
        }

        return new FilterResult(validList, inValidList);
    }


    /**
     * 过滤结果
     */
    static class FilterResult {
        List<ByteBuffer> validList; // 有效队列
        List<ByteBuffer> inValidList; // 无效队列

        public FilterResult(List<ByteBuffer> validList, List<ByteBuffer> inValidList) {
            this.validList = validList;
            this.inValidList = inValidList;
        }

        public List<ByteBuffer> getValidList() {
            return validList;
        }

        public void setValidList(List<ByteBuffer> validList) {
            this.validList = validList;
        }

        public List<ByteBuffer> getInValidList() {
            return inValidList;
        }

        public void setInValidList(List<ByteBuffer> inValidList) {
            this.inValidList = inValidList;
        }
    }

}
