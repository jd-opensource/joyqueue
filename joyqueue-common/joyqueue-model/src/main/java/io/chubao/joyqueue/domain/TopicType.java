package io.chubao.joyqueue.domain;

public enum TopicType {
    /**
     * 主题
     */
    TOPIC((byte)0, "普通主题"),

    /**
     * 广播
     */
    BROADCAST((byte)1, "广播");
//        /**
//         * 顺序队列
//         */
//        SEQUENTIAL((byte)2, "顺序主题");


    private final byte code;
    private final String name;

    TopicType(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte code() {
        return this.code;
    }


    public String getName() {
        return this.name;
    }


    public static TopicType valueOf(final byte value) {
        for (TopicType type : TopicType.values()) {
            if (value == type.code) {
                return type;
            }
        }
        return TOPIC;
    }
}
