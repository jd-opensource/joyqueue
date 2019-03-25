package com.jd.journalq.network.session;

/**
 * 客户端语言
 */
public enum Language {
    /**
     * Java客户端
     */
    JAVA,
    /**
     * C++客户端
     */
    CPP,
    /**
     * Python客户端
     */
    PYTHON,
    /**
     * Ruby客户端
     */
    RUBY,
    /**
     * .NET客户端
     */
    DOTNET,
    /**
     * Erlang客户端
     */
    ERLANG,
    /**
     * 其它客户端
     */
    OTHER;

    public static Language valueOf(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }

}