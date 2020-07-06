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
package org.joyqueue.broker.archive.store.hbase;

/**
 * Created by chengzhiliang on 2018/12/13.
 */
//FIXME: 单元测试未通过

public class HbaseSerializerTest {


//    @Test
//    public void resetPosition() {
//        ByteBuffer allocate = ByteBuffer.allocate(1024);
//        byte[] val = "test string.".getBytes(Charset.forName("utf-8"));
//        allocate.putInt(val.length);
//        allocate.put(val);
//        allocate.flip();
//
//        int len = 4 + val.length;
//
//        int anInt = allocate.getInt();
//        byte[] bytes = new byte[anInt];
//        allocate.get(bytes);
//        String getVal = new String(bytes, Charset.forName("utf-8"));
//        System.out.println(getVal);
//
//        allocate.position(allocate.position() - len);
//
//
//        byte[] bytes2 = new byte[allocate.getInt()];
//        allocate.get(bytes2);
//        String getVal2 = new String(bytes, Charset.forName("utf-8"));
//        System.out.println(getVal2);
//
//
//    }
}