package com.jd.journalq.response;

import com.jd.journalq.exception.JMQCode;

/**
 * @author wylixiaobin
 * Date: 2019/1/3
 */
public class BooleanResponse implements Response{
    private JMQCode jmqCode;
    public BooleanResponse(JMQCode code){
        this.jmqCode = code;
    }
    public static BooleanResponse success(){
        return new BooleanResponse(JMQCode.SUCCESS);
    }

    public static BooleanResponse failed(JMQCode jmqCode){
        return new BooleanResponse(jmqCode);
    }

    public JMQCode getJmqCode() {
        return jmqCode;
    }

    @Override
    public boolean isSuccess(){
        return jmqCode == JMQCode.SUCCESS;
    }
}
