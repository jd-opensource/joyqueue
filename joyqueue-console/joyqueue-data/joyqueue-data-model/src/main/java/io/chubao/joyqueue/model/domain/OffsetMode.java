package io.chubao.joyqueue.model.domain;

public enum OffsetMode {
    SERVER,
    CLIENT;

    public static OffsetMode valueOf(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}