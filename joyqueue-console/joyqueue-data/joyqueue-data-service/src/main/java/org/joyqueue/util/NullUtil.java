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
package org.joyqueue.util;

import org.joyqueue.model.domain.BaseModel;

import java.util.Collection;


public class NullUtil {

    public static boolean isEmpty(String value){
        if(null==value ||value.length() <=0 )
            return true;
        return false;
    }

    public static boolean isEmpty(Object value){
        if(null==value)
            return true;
        return false;
    }
    public static boolean isEmpty(Object... objs){
        if(null==objs||objs.length==0)
            return true;
        return false;
    }
    public static boolean isEmpty(Collection collection){
        if(null==collection ||collection.size()<=0)
            return true;
        return false;
    }

    public static boolean isBlank(String value){
        if(null==value ||value.length() <=0 || "".equals(value.trim()))
            return true;
        return false;
    }

    public static boolean isNotEmpty(String value){
        if(null==value ||value.length() <=0 )
            return false;
        return true;
    }

    public static boolean isNotBlank(String value){
        if(null==value ||value.length() <=0 || "".equals(value.trim()))
            return false;
        return true;
    }

    public static boolean isNotEmpty(Object value){
        if(null==value)
            return false;
        return true;
    }

    public static boolean isNotEmpty(Collection collection){
        if(null==collection ||collection.size()<=0)
            return false;
        return true;
    }

    public static void checkArgument(final BaseModel model) {
        if (null==model || model.getId()<=0L) {
            throw new IllegalArgumentException("illegal args.");
        }
    }

    public static void checkArgumentForUpdate(final BaseModel model) {
        if (null==model || model.getId()<=0L || null==model.getUpdateBy()) {
            throw new IllegalArgumentException("illegal args.");
        }
    }

}
