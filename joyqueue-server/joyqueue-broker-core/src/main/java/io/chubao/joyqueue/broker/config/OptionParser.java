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
