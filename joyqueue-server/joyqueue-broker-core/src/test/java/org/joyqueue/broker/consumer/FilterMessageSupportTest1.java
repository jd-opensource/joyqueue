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
package org.joyqueue.broker.consumer;

import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.BrokerMessage;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chengzhiliang on 2019/1/2.
 */
public class FilterMessageSupportTest1 {

    @Test
    public void doFilterTestAckAndReturn() throws JoyQueueException {
        List<BrokerMessage> messages = new ArrayList<>();
        BrokerMessage brokerMessage1 = new BrokerMessage();
        brokerMessage1.setFlag((short) 1);
        brokerMessage1.setMsgIndexNo(1l);

        BrokerMessage brokerMessage2 = new BrokerMessage();
        brokerMessage2.setFlag((short) 2);
        brokerMessage2.setMsgIndexNo(2l);

        messages.add(brokerMessage2);
        messages.add(brokerMessage1);

        String pattern = "(1)";

        FilterResult filterResult = doFilter(messages, pattern);

        // 无效数据待内部ACK
        List<BrokerMessage> inValidList = filterResult.getInValidList();
        Assert.assertEquals(inValidList.get(0).getMsgIndexNo(), 2);

        // 有效数据
        List<BrokerMessage> validList = filterResult.getValidList();
        Assert.assertEquals(validList.get(0).getMsgIndexNo(), 1);
    }


    @Test
    public void doFilterTestAckAndReturnByFalse() throws JoyQueueException {
        List<BrokerMessage> messages = new ArrayList<>();
        BrokerMessage brokerMessage1 = new BrokerMessage();
        brokerMessage1.setFlag((short) 1);
        brokerMessage1.setMsgIndexNo(1l);

        BrokerMessage brokerMessage2 = new BrokerMessage();
        brokerMessage2.setFlag((short) 2);
        brokerMessage2.setMsgIndexNo(2l);

        messages.add(brokerMessage1);
        messages.add(brokerMessage2);

        String pattern  = "[^(1)]";

        FilterResult filterResult = doFilter(messages, pattern);

        // 无效数据待内部ACK
        List<BrokerMessage> inValidList = filterResult.getInValidList();
        Assert.assertEquals(inValidList.get(0).getMsgIndexNo(), 1);

        // 有效数据
        List<BrokerMessage> validList = filterResult.getValidList();
        Assert.assertEquals(validList.get(0).getMsgIndexNo(), 2);
    }

    @Test
    public void doFilterTestNoAckAndReturn() throws JoyQueueException {
        List<BrokerMessage> messages = new ArrayList<>();
        BrokerMessage brokerMessage1 = new BrokerMessage();
        brokerMessage1.setFlag((short) 1);
        brokerMessage1.setMsgIndexNo(1l);

        BrokerMessage brokerMessage2 = new BrokerMessage();
        brokerMessage2.setFlag((short) 2);
        brokerMessage2.setMsgIndexNo(2l);

        messages.add(brokerMessage1);
        messages.add(brokerMessage2);

        String pattern = "(1)";

        FilterResult filterResult = doFilter(messages, pattern);

        // 无效数据待内部ACK
        List<BrokerMessage> inValidList = filterResult.getInValidList();
        Assert.assertEquals(inValidList, null);

        // 有效数据
        List<BrokerMessage> validList = filterResult.getValidList();
        Assert.assertEquals(validList.get(0).getMsgIndexNo(), 1);
    }

    @Test
    public void doFilterTestNoAckAndReturnByFalse() throws JoyQueueException {
        List<BrokerMessage> messages = new ArrayList<>();
        BrokerMessage brokerMessage1 = new BrokerMessage();
        brokerMessage1.setFlag((short) 1);
        brokerMessage1.setMsgIndexNo(1l);

        BrokerMessage brokerMessage2 = new BrokerMessage();
        brokerMessage2.setFlag((short) 2);
        brokerMessage2.setMsgIndexNo(2l);

        messages.add(brokerMessage2);
        messages.add(brokerMessage1);

        String pattern  = "[^(1)]";

        FilterResult filterResult = doFilter(messages, pattern);

        // 无效数据待内部ACK
        List<BrokerMessage> inValidList = filterResult.getInValidList();
        Assert.assertEquals(inValidList, null);

        // 有效数据
        List<BrokerMessage> validList = filterResult.getValidList();
        Assert.assertEquals(validList.get(0).getMsgIndexNo(), 2);
    }

    @Test
    public void doFilterTestAllReturn() throws JoyQueueException {
        List<BrokerMessage> messages = new ArrayList<>();
        BrokerMessage brokerMessage1 = new BrokerMessage();
        brokerMessage1.setFlag((short) 1);
        brokerMessage1.setMsgIndexNo(1l);

        BrokerMessage brokerMessage2 = new BrokerMessage();
        brokerMessage2.setFlag((short) 2);
        brokerMessage2.setMsgIndexNo(2l);

        messages.add(brokerMessage1);
        messages.add(brokerMessage2);

        String pattern  = "[(1)]|[(2)]";

        FilterResult filterResult = doFilter(messages, pattern);

        // 无效数据待内部ACK
        List<BrokerMessage> inValidList = filterResult.getInValidList();
        Assert.assertEquals(inValidList, null);

        // 有效数据
        List<BrokerMessage> validList = filterResult.getValidList();
        Assert.assertEquals(validList.get(0).getMsgIndexNo(), 1);
        Assert.assertEquals(validList.get(1).getMsgIndexNo(), 2);
    }

    @Test
    public void doFilterTestAllAck() throws JoyQueueException {
        List<BrokerMessage> messages = new ArrayList<>();
        BrokerMessage brokerMessage1 = new BrokerMessage();
        brokerMessage1.setFlag((short) 1);
        brokerMessage1.setMsgIndexNo(1l);

        BrokerMessage brokerMessage2 = new BrokerMessage();
        brokerMessage2.setFlag((short) 2);
        brokerMessage2.setMsgIndexNo(2l);

        messages.add(brokerMessage1);
        messages.add(brokerMessage2);

        String pattern  = "[^(1,2)]";

        FilterResult filterResult = doFilter(messages, pattern);

        // 无效数据待内部ACK
        List<BrokerMessage> validList = filterResult.getValidList();
        Assert.assertEquals(validList.size(), 0);

        // 有效数据
        List<BrokerMessage> inValidList = filterResult.getInValidList();
        Assert.assertEquals(inValidList.get(0).getMsgIndexNo(), 1);
        Assert.assertEquals(inValidList.get(1).getMsgIndexNo(), 2);
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
    private FilterResult doFilter(List<BrokerMessage> messages, String pattern) throws JoyQueueException {
        List<BrokerMessage> validList = new ArrayList<>(); // 有效队列
        List<BrokerMessage> inValidList = null; // 无效队列
        boolean /* 有效到无效 */ valid2InvalidFlag = false,
                /* 无效到有效 */ invaild2ValidFlag = false;

        for (int i = 0; i < messages.size(); i++) {
            BrokerMessage message = messages.get(i);
            if (i == 0 && !Pattern.matches(pattern, ""+message.getFlag())) {
                // 不是有效标签开头
                inValidList = new ArrayList<>();
            }

            if (Pattern.matches(pattern, ""+message.getFlag()) && !valid2InvalidFlag) {
                validList.add(message);
                if (inValidList != null && inValidList.size() > 0) {
                    invaild2ValidFlag = true;
                }
            } else if (inValidList != null && !invaild2ValidFlag) {
                inValidList.add(message);
                if (validList.size() > 0) {
                    valid2InvalidFlag = true;
                }
            }
        }

        return new FilterResult(validList, inValidList);
    }

    public static void main(String[] args) {
        Pattern compile = Pattern.compile("[1,2]");
        for (int i = 0; i < 100; i++) {
            Matcher matcher = compile.matcher(""+i);
            if (matcher.matches()) {
                System.out.println(i);
            }
        }

    }



    /**
     * 过滤结果
     */
    static class FilterResult {
        List<BrokerMessage> validList; // 有效队列
        List<BrokerMessage> inValidList; // 无效队列

        public FilterResult() {
        }

        public FilterResult(List<BrokerMessage> validList, List<BrokerMessage> inValidList) {
            this.validList = validList;
            this.inValidList = inValidList;
        }

        public List<BrokerMessage> getValidList() {
            return validList;
        }

        public void setValidList(List<BrokerMessage> validList) {
            this.validList = validList;
        }

        public List<BrokerMessage> getInValidList() {
            return inValidList;
        }

        public void setInValidList(List<BrokerMessage> inValidList) {
            this.inValidList = inValidList;
        }
    }

}