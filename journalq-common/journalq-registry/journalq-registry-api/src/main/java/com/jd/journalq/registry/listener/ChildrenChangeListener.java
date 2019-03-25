package com.jd.journalq.registry.listener;

import com.jd.journalq.toolkit.concurrent.EventListener;

/**
 * 子节点变化监听器，监听子节点数据变化，节点变化，一次变化只调用一次, 事件内不包含数据
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/6/13 17:38
 */
public interface ChildrenChangeListener extends EventListener<ChildrenEvent> {
}