package com.jd.journalq.network.command;

import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.Collections;
import java.util.List;

/**
 * FetchProduceFeedbackResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class FetchProduceFeedbackResponse extends JMQPayload {

    private List<FetchProduceFeedbackAckData> data;
    private JMQCode code;

    public FetchProduceFeedbackResponse() {

    }

    public FetchProduceFeedbackResponse(JMQCode code) {
        this.data = Collections.emptyList();
        this.code = code;
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_PRODUCE_FEEDBACK_RESPONSE.getCode();
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }

    public JMQCode getCode() {
        return code;
    }

    public void setData(List<FetchProduceFeedbackAckData> data) {
        this.data = data;
    }

    public List<FetchProduceFeedbackAckData> getData() {
        return data;
    }
}