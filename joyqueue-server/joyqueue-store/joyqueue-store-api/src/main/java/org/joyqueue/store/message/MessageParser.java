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
package org.joyqueue.store.message;

import org.joyqueue.toolkit.time.SystemClock;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 消息解析器
 * @author liyue25
 * Date: 2019-01-15
 */
public class MessageParser {

    public static final int VARIABLE_LENGTH_1 = -1;
    public static final int VARIABLE_LENGTH_2 = -2;
    public static final int VARIABLE_LENGTH_4 = -4;
    public static final int FIXED_LENGTH_1 = 1;
    public static final int FIXED_LENGTH_2 = 2;
    public static final int FIXED_LENGTH_4 = 4;
    public static final int FIXED_LENGTH_8 = 8;
    public static final int FIXED_LENGTH_16 = 16;
    private static int offset = 0;
    // 第一个变长属性的偏移量
    private static int firstVarOffset = -1;
    // 第一个变长属性在attributes数组中的索引IgniteServiceProvider
    private static int firstVarIndex = -1;
    private static final List<Attribute> attributeList= new LinkedList<>();


    public static final int LENGTH = createAttribute("LENGTH",FIXED_LENGTH_4);

    // 如果消息格式调整，只需变更如下部分
    // 定长属性必须放在变长属性前面

    public static final int PARTITION = createAttribute("PARTITION",FIXED_LENGTH_2);
    public static final int INDEX = createAttribute("INDEX",FIXED_LENGTH_8);
    public static final int TERM = createAttribute("TERM",FIXED_LENGTH_4);
    public static final int MAGIC = createAttribute("MAGIC",FIXED_LENGTH_2);
    public static final int SYS = createAttribute("SYS",FIXED_LENGTH_2);
    public static final int PRIORITY = createAttribute("PRIORITY",FIXED_LENGTH_1);
    public static final int CLIENT_IP = createAttribute("CLIENT_IP",FIXED_LENGTH_16);
    public static final int CLIENT_TIMESTAMP = createAttribute("CLIENT_TIMESTAMP",FIXED_LENGTH_8);
    public static final int STORAGE_TIMESTAMP = createAttribute("STORAGE_TIMESTAMP",FIXED_LENGTH_4);
    public static final int CRC = createAttribute("CRC",FIXED_LENGTH_8);
    public static final int FLAG = createAttribute("FLAG",FIXED_LENGTH_2);

    public static final int BODY = createAttribute("BODY",VARIABLE_LENGTH_4);
    public static final int BIZ_ID = createAttribute("BIZ_ID",VARIABLE_LENGTH_1);
    public static final int PROPERTY = createAttribute("PROPERTY",VARIABLE_LENGTH_2);
    public static final int EXPAND = createAttribute("EXPAND",VARIABLE_LENGTH_4);
    public static final int APP = createAttribute("APP",VARIABLE_LENGTH_1);

    // 如果消息格式调整，只需变更以上部分

    private static final Attribute [] attributes = attributeList.toArray(new Attribute[0]);

    public static int getFixedAttributesLength(){return firstVarOffset;}

    public static byte getByte(ByteBuffer messageBuffer, int offset){
        return messageBuffer.get(messageBuffer.position() + offset);
    }

    public static void setByte(ByteBuffer messageBuffer, int offset, byte value){
        messageBuffer.put(messageBuffer.position() + offset,value);
    }

    public static short getShort(ByteBuffer messageBuffer, int offset){
        return messageBuffer.getShort(messageBuffer.position() + offset);
    }

    public static void setShort(ByteBuffer messageBuffer, int offset, short value){
        messageBuffer.putShort(messageBuffer.position() + offset,value);
    }

    public static int getBit(ByteBuffer messageBuffer, int byteOffset, int bitOffset){
        byte b = getByte(messageBuffer, byteOffset);
        return (b >> bitOffset) & 1;
    }

    public static void setBit(ByteBuffer messageBuffer, int byteOffset, int bitOffset, boolean bitValue){

        byte b = getByte(messageBuffer, byteOffset);
        if(bitValue) {
            b |= 1 << bitOffset;
        } else {
            b &= ~(1 << bitOffset);
        }
        setByte(messageBuffer, byteOffset, b);
    }

    public static int getInt(ByteBuffer messageBuffer, int offset){
        return messageBuffer.getInt(messageBuffer.position() + offset);
    }

    public static void setInt(ByteBuffer messageBuffer, int offset, int value){
        messageBuffer.putInt(messageBuffer.position() + offset,value);
    }

    public static long getLong(ByteBuffer messageBuffer, int offset){
        return messageBuffer.getLong(messageBuffer.position() + offset);
    }

    public static void setLong(ByteBuffer messageBuffer, int offset, long value){
        messageBuffer.putLong(messageBuffer.position() + offset,value);
    }

    public static ByteBuffer getByteBuffer(ByteBuffer messageBuffer, int relativeOffset) {
        int offset = firstVarOffset;
        // 计算偏移量

        int length;
        for(int index = firstVarIndex; index < firstVarIndex - relativeOffset; index++) {
            int varLength = attributes[index].getLength();
            offset += getVariableAttributeLength(messageBuffer, varLength, offset);
            offset -= varLength;
        }

        int varLength = attributes[firstVarIndex - relativeOffset].getLength();
        length = getVariableAttributeLength(messageBuffer, varLength, offset);
        offset -= varLength;
        if(length < 0) throw new ParseAttributeException("Invalid offset: " + relativeOffset);

        ByteBuffer byteBuffer = messageBuffer.slice();
        byteBuffer.position(offset);
        byteBuffer.limit(offset + length);
        return byteBuffer;

    }

    public static byte [] getBytes(ByteBuffer messageBuffer, int relativeOffset) {
        ByteBuffer byteBuffer = getByteBuffer(messageBuffer, relativeOffset);

        ByteBuffer arrayBuffer = ByteBuffer.allocate(byteBuffer.remaining());
        arrayBuffer.put(byteBuffer);
        return arrayBuffer.array();
    }

    public static ByteBuffer build(byte [][] variableAttributes) {
        if(variableAttributes.length != attributes.length - firstVarIndex) {
            throw new ParseAttributeException("Length of parameter variableAttributes should be equals the count of variable attributes : " + (attributes.length - firstVarIndex));
        }
        int length = firstVarOffset;
        for (int i = 0; i < variableAttributes.length; i++) {
            byte [] attributeValue = variableAttributes[i];
            Attribute attribute  = attributes[firstVarIndex + i];
            length += attributeValue.length - attribute.getLength();
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        setInt(byteBuffer, LENGTH, length);

        byteBuffer.position(firstVarOffset);
        for (int i = 0; i < variableAttributes.length; i++) {
            byte [] attributeValue = variableAttributes[i];
            Attribute attribute  = attributes[firstVarIndex + i];
            switch (attribute.getLength()) {
                case VARIABLE_LENGTH_1:
                    byteBuffer.put((byte) attributeValue.length);
                    break;
                case VARIABLE_LENGTH_2:
                    byteBuffer.putShort((short) attributeValue.length);
                    break;
                case VARIABLE_LENGTH_4:
                    byteBuffer.putInt(attributeValue.length);
                    break;
                default:
                    throw new ParseAttributeException("Invalid length: " + length);
            }

            byteBuffer.put(attributeValue);

        }
        byteBuffer.flip();
        return byteBuffer;
    }

    public static String getString(ByteBuffer messageBuffer) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        StringBuilder stringBuilder = new StringBuilder();
        long clientDate = 0L;
        for (Attribute attribute : attributes) {
            stringBuilder.append(attribute.getName()).append("(");
            if (attribute.getLength() >= 0) {
                stringBuilder.append(attribute.getLength()).append("): ");
            }
            switch (attribute.getLength()) {
                case FIXED_LENGTH_1:
                    try {
                        byte b = getByte(messageBuffer, attribute.getOffset());
                        stringBuilder.append(String.format("%02X", b)).append(String.format("(%d)", new Byte(b).intValue())).append("\n");
                    } catch (Exception e) {
                        stringBuilder.append("Exception:").append(e).append("\n");
                    }

                    break;
                case FIXED_LENGTH_2:
                    try {
                        short s = getShort(messageBuffer, attribute.getOffset());
                        stringBuilder.append(s).append("\n");
                    } catch (Exception e) {
                        stringBuilder.append("Exception:").append(e).append("\n");
                    }
                    break;
                case FIXED_LENGTH_4:
                    try {
                        int ii = getInt(messageBuffer, attribute.getOffset());
                        stringBuilder.append(ii);
                        if ("STORAGE_TIMESTAMP".equals(attribute.getName())) {
                            stringBuilder.append(" (").append(sdf.format(new Date(clientDate + ii))).append(")");
                        }
                        stringBuilder.append("\n");
                    } catch (Exception e) {
                        stringBuilder.append("Exception:").append(e).append("\n");
                    }
                    break;
                case FIXED_LENGTH_8:
                    try {
                        long l = getLong(messageBuffer, attribute.getOffset());
                        stringBuilder.append(l);
                        if ("CLIENT_TIMESTAMP".equals(attribute.getName())) {
                            clientDate = l;
                            stringBuilder.append(" (").append(sdf.format(new Date(l))).append(")");
                        }
                        stringBuilder.append("\n");
                    } catch (Exception e) {
                        stringBuilder.append("Exception:").append(e).append("\n");
                    }
                    break;
                default:
                    appendBytes(messageBuffer, stringBuilder, attribute);
            }


        }


        return stringBuilder.toString();
    }

    private static void appendBytes(ByteBuffer messageBuffer, StringBuilder stringBuilder, Attribute attribute) {
        try {
            byte [] bytes;

            if(attribute.getLength() < 0) {
                bytes = getBytes(messageBuffer, attribute.getOffset());
                stringBuilder.append(bytes.length);
                stringBuilder.append("): ");
            } else {
                bytes = getBytes(messageBuffer, attribute);
            }
            stringBuilder.append("\n\tHex: ");
            for (int j = 0; j < bytes.length && j < 32; j++) {
                stringBuilder.append(String.format("0x%02X ", bytes[j]));
            }
            if (bytes.length > 32) stringBuilder.append("...");
            stringBuilder.append("\n\tString: ").append(new String(bytes, StandardCharsets.UTF_8));

        } catch (Exception e) {
            stringBuilder.append("Exception:").append(e);
        }
        stringBuilder.append("\n");

    }

    private static byte[] getBytes(ByteBuffer messageBuffer, Attribute attribute) {
        byte[] bytes;
        ByteBuffer byteBuffer = messageBuffer.slice();
        byteBuffer.position(byteBuffer.position() + attribute.getOffset());
        byteBuffer.limit(byteBuffer.position() + attribute.getLength());

        ByteBuffer buffer = ByteBuffer.allocate(attribute.length);
        buffer.put(byteBuffer);
        buffer.flip();
        bytes = buffer.array();
        return bytes;
    }


    private static int getVariableAttributeLength(ByteBuffer messageBuffer, int length, int offset) {
        switch (length) {
            case VARIABLE_LENGTH_1:
                return messageBuffer.get(offset);
            case VARIABLE_LENGTH_2:
                return messageBuffer.getShort(offset);
            case VARIABLE_LENGTH_4:
                return messageBuffer.getInt(offset);
            default:
                throw new ParseAttributeException("Invalid length: " + length);
        }
    }


    /**
     * 定长消息直接返回offset
     * 变长消息返回属性相对于第一个变长属性的索引值的偏移量的负值：第一个变长属性在attributes中的索引值 - 属性在attributes中的索引值
     * @param length 属性长度，定长消息为正值，负值表示变长消息。
     */
    private static int createAttribute(String name, int length){

        Attribute attribute = new Attribute(name, length);


        if(attribute.length >= 0) {
            // 定长属性
            if(offset < 0)
                throw new ParseAttributeException(
                        "Can not add a fixed length attribute after any variable length attribute!");
            attribute.setOffset(offset);
            offset += length;
        } else {
            // 变长属性
            if(firstVarOffset < 0 ) { // 第一个变长属性
                firstVarOffset = offset;
                firstVarIndex = attributeList.size();
                offset = -1;
            }
            attribute.setOffset(firstVarIndex - attributeList.size());
        }


        attributeList.add(attribute);
        return attribute.getOffset();
    }

    static class Attribute {
        private final int length;
        private final String name;
        private int offset = -1;

        /**
         * 定长属性
         * @param length 长度
         */
        Attribute(String name, int length) {
            this.name = name;
            this.length = length;
        }

        public int getLength() {

            return length;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public String getName() {
            return name;
        }
    }


    public static void main(String [] args){
        byte [] body = "This is body!".getBytes(StandardCharsets.UTF_8);
        byte [] biz_id = new byte[8];
        Arrays.fill(biz_id,(byte) 0x25);
        byte [] property = "This is property!".getBytes(StandardCharsets.UTF_8);


        byte [] expand = "This is expand!".getBytes(StandardCharsets.UTF_8);

        byte [] app = new byte[8];
        Arrays.fill(app,(byte) 0x21);

        byte [][] varAtts = {body, biz_id, property, expand, app};
        ByteBuffer byteBuffer = build(varAtts);

        setLong(byteBuffer,CLIENT_TIMESTAMP, SystemClock.now());
        setLong(byteBuffer, INDEX, 23L);
        setByte(byteBuffer, PRIORITY, (byte) 0x06);
        setShort(byteBuffer, PARTITION, (short) 26);
        setInt(byteBuffer, TERM, 127);
        System.out.println(getString(byteBuffer));
    }
}
