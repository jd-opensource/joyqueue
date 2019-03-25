package com.jd.journalq.util;

import com.jd.journalq.model.domain.BaseModel;

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
