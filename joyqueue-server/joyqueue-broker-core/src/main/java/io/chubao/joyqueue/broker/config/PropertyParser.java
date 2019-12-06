package io.chubao.joyqueue.broker.config;

import io.chubao.joyqueue.toolkit.config.Property;

/**
 * Property parser
 *
 **/
public interface PropertyParser {

    /**
     * Parser property from string
     * @param name property name
     *
     **/
    Property parse(String property) throws Exception;
}
