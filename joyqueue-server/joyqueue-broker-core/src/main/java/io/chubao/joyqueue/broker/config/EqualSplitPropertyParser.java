package io.chubao.joyqueue.broker.config;

import io.chubao.joyqueue.toolkit.config.Property;



public class EqualSplitPropertyParser implements PropertyParser {

    private static final String SPLIT="=";
    private static  final int LENGTH= 2;

    @Override
    public Property parse(String property) throws Exception{
        if(property==null){
            return null;
        }
        String[] kv=property.split(SPLIT);
        if(kv.length == LENGTH){
            return  new Property(null,kv[0],kv[1],0,0);
        }else{
            throw new IllegalArgumentException(String.format("override properties with key%value ", SPLIT));
        }
    }
}
