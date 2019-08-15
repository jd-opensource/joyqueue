package io.chubao.joyqueue.broker.consumer.filter;

import io.chubao.joyqueue.exception.JoyQueueException;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 过滤管道
 * <p>
 * Created by chengzhiliang on 2019/2/20.
 */
public class FilterPipeline<T extends MessageFilter> {
    // 基于链表的过滤管道
    private final LinkedList<T> pipeline = new LinkedList<>();
    // 管道里面是否有过滤器
    private volatile boolean hasFilter = false;
    // 管理唯一标示
    private String id;

    public FilterPipeline(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * 注册过滤器到管道
     *
     * @param t
     */
    public void register(T t) {
        pipeline.addLast(t);

        hasFilter = true;
    }

    /**
     * 执行过滤
     *
     * @param byteBuffers
     * @return
     * @throws JoyQueueException
     */
    public List<ByteBuffer> execute(List<ByteBuffer> byteBuffers, FilterCallback filterCallback) throws JoyQueueException {
        if (hasFilter) {
            Iterator<T> iterator = pipeline.iterator();
            while (iterator.hasNext()) {
                T next = iterator.next();
                byteBuffers = next.filter(byteBuffers, filterCallback);
            }
        }

        return byteBuffers;
    }

}