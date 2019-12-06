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
