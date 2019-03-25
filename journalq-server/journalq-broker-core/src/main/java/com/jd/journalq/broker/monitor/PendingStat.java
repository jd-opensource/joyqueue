package com.jd.journalq.broker.monitor;

import java.util.Map;

public interface PendingStat<K,V> {

   /**
    * 获取当前level的 积压
    **/
   long getPending();
   void setPending(long pending);

   void setPendingStatSubMap(Map<K,V> subMap);

   /**
    *  获取 sub level 的pending stat map
    *
    **/
   Map<K,V> getPendingStatSubMap();


}
