package io.chubao.joyqueue.model.domain;

import java.util.Calendar;
import java.util.Date;

/**
 * 持续时间
 */
public interface DurationTime {

    /**
     * 生效时间
     *
     * @return
     */
    Date getEffectiveTime();

    /**
     * 设置生效时间
     *
     * @param time
     */
    void setEffectiveTime(Date time);

    /**
     * 过期时间
     *
     * @return
     */
    Date getExpirationTime();

    /**
     * 设置过期时间
     *
     * @param time
     */
    void setExpirationTime(Date time);

    /**
     * 时间是否有效
     *
     * @param time 时间
     * @return
     */
    default boolean isEffective(final Date time) {
        if (time == null) {
            return false;
        }
        Date effectiveTime = getEffectiveTime();
        Date expirationTime = getExpirationTime();
        if (effectiveTime != null) {
            if (effectiveTime.after(time)) {
                return false;
            }
        }
        if (expirationTime != null) {
            if (expirationTime.before(time)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 初始化时间
     */
    default void initializeTime() {
        initializeTime(10);
    }

    /**
     * 初始化时间
     *
     * @param expireYears 在几年以后过期
     */
    default void initializeTime(int expireYears) {
        Date now = new Date();
        if (getEffectiveTime() == null) {
            setEffectiveTime(now);
        }
        if (getExpirationTime() == null) {
            Calendar cd = Calendar.getInstance();
            cd.setTime(now);
            cd.add(Calendar.YEAR, expireYears);
            setExpirationTime(cd.getTime());
        }
    }
}
