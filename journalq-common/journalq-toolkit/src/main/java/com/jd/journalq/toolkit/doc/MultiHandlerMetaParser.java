package com.jd.journalq.toolkit.doc;

public interface MultiHandlerMetaParser<T> extends MetaParser<T> {

    /**
     * fist handler doc entry
     *
     * @return null if not exist
     **/
    DocEntry first(String path);

    /**
     * last handler doc entry
     *
     * @return null if not exist
     **/
    DocEntry last(String path);
}
