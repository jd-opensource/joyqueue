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
package org.joyqueue.broker.consumer.filter;

import com.google.common.collect.Lists;
import com.jd.laf.extension.Extension;
import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
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
    public List<ByteBuffer> filter(List<ByteBuffer> byteBufferList, FilterCallback filterCallback) throws JoyQueueException {
        FilterResult filterResult = doFilter(byteBufferList, pattern);
        List<ByteBuffer> inValidList = filterResult.getInValidList();
        if (inValidList != null && !inValidList.isEmpty() && filterCallback != null) {
            filterCallback.callback(inValidList);
        }
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
    private FilterResult doFilter(List<ByteBuffer> messages, Pattern pattern) throws JoyQueueException {
        List<ByteBuffer> validList = Lists.newLinkedList(); // 有效队列
        List<ByteBuffer> inValidList = Lists.newLinkedList(); // 无效队列
        boolean /* 有效到无效 */ valid2InvalidFlag = false,
                /* 无效到有效 */ invalid2ValidFlag = false;

        for (int i = 0; i < messages.size(); i++) {
            ByteBuffer buffer = messages.get(i);
            short flag;
            try {
                flag = Serializer.readFlag(buffer);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new JoyQueueException(JoyQueueCode.SE_SERIALIZER_ERROR, e);
            }

            // 是否匹配
            boolean isMatch = (flag == 0 || pattern.matcher("" + flag).matches());

            if (isMatch) {
                if (i == 0) {
                    valid2InvalidFlag = true;
                }
                validList.add(buffer);
                if (invalid2ValidFlag) {
                    break;
                }
            } else {
                if (i == 0) {
                    invalid2ValidFlag = true;
                }
                if (valid2InvalidFlag) {
                    break;
                }
                inValidList.add(buffer);
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

        FilterResult(List<ByteBuffer> validList, List<ByteBuffer> inValidList) {
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
