package io.chubao.joyqueue.broker.config;

import io.chubao.joyqueue.toolkit.config.Property;

import java.util.ArrayList;
import java.util.List;

/**
 *  Option
 *
 **/
public class OptionParser {

    /**
     * Parse properties form args using default parser
     **/
    public static List<Property> parse(String[] args) throws Exception{
         return parse(args,null);
    }

    /**
     * Parse properties using parser
     **/
    public static List<Property> parse(String[] args,PropertyParser parser) throws Exception{
        List<Property> properties=new ArrayList();
        if(parser==null) {
            parser=new EqualSplitPropertyParser();
        }
        for(String arg:args){
            properties.add(parser.parse(arg));
        }
        return properties;
    }
}
