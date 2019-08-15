package io.chubao.joyqueue.toolkit.doc;

import java.util.Map;

/**
 * parse meta data from file
 **/
public interface MetaParser<T> {

    /**
     * meta resource location,eg. a package or file name
     **/
    Map<String, Map<String, T>> parse();
}
