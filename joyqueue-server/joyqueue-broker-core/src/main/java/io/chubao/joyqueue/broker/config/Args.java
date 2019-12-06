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
