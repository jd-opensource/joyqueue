package com.jd.journalq.registry.listener;


import com.jd.journalq.toolkit.concurrent.EventListener;

/**
 * 孩子监听器，不感知数据变化
 */
public interface ChildrenListener extends EventListener<ChildrenEvent> {
	
}
