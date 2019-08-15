package io.chubao.joyqueue.client.internal.producer.feedback.config;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

/**
 * TxFeedbackConfigChecker
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class TxFeedbackConfigChecker {

    public static void check(TxFeedbackConfig config) {
        Preconditions.checkArgument(config != null, "txFeedback can not be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(config.getApp()), "txFeedback.app can not be null");
    }
}