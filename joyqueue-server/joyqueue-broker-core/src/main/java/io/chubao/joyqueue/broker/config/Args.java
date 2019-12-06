package io.chubao.joyqueue.broker.config;

import java.util.Map;
import java.util.Properties;

/**
 * Args seperate by =
 *
 **/
public class Args {

    private Properties properties=new Properties();

    /**
     *  Appende arg
     **/
    public void append(String key,String value){
        this.properties.put(key,value);
    }


    /**
     * build args line
     **/
    public String[] build(){
        String[] args=new String[properties.size()];
        int index=0;
        for(Map.Entry<Object,Object> e: properties.entrySet()){
            args[index++] =String.format("%s=%s",e.getKey(),e.getValue());
        }
        return args;
    }
}
