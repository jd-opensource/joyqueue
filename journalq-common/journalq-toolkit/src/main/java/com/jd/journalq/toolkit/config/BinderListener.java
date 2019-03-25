package com.jd.journalq.toolkit.config;

/**
 * 绑定监听器
 * Created by hexiaofeng on 16-8-29.
 */
public class BinderListener implements Postman.GroupListener {

    Object target;

    public BinderListener(Object target) {
        this.target = target;
    }

    @Override
    public void onUpdate(final String group, final Context context) {
        if (context == null || target == null) {
            return;
        }
        Binders.bind(context, target);
    }
}
