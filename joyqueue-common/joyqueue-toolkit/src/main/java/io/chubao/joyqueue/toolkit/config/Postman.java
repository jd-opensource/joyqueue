package io.chubao.joyqueue.toolkit.config;

/**
 * 动态配置
 * Created by hexiaofeng on 16-8-29.
 */
public interface Postman {

    /**
     * 监听分组配置变更
     *
     * @param group    分组
     * @param listener 监听器
     */
    void addListener(String group, GroupListener listener);

    /**
     * 删除分组监听器
     *
     * @param group    分组
     * @param listener 监听器
     */
    void removeListener(String group, GroupListener listener);

    /**
     * 获取上下文
     *
     * @param group 分组
     * @return 上下文
     */
    Context get(String group);

    /**
     * 分组监听器
     */
    interface GroupListener {

        /**
         * 配置发生变更,每个监听器拿到的上下文是线程安全的
         *
         * @param group   分组
         * @param context 上下文
         */
        void onUpdate(String group, Context context);
    }

}
