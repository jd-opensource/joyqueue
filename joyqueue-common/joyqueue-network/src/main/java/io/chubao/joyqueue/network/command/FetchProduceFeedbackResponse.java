package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Collections;
import java.util.List;

/**
 * FetchProduceFeedbackResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/18
 */
public class FetchProduceFeedbackResponse extends JoyQueuePayload {

    private List<FetchProduceFeedbackAckData> data;
    private JoyQueueCode code;

    public FetchProduceFeedbackResponse() {

    }

    public FetchProduceFeedbackResponse(JoyQueueCode code) {
        this.data = Collections.emptyList();
        this.code = code;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_PRODUCE_FEEDBACK_RESPONSE.getCode();
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public JoyQueueCode getCode() {
        return code;
    }

    public void setData(List<FetchProduceFeedbackAckData> data) {
        this.data = data;
    }

    public List<FetchProduceFeedbackAckData> getData() {
        return data;
    }
}