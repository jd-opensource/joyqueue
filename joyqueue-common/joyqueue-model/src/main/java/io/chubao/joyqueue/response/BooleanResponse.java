package io.chubao.joyqueue.response;

import io.chubao.joyqueue.exception.JoyQueueCode;

/**
 * @author wylixiaobin
 * Date: 2019/1/3
 */
public class BooleanResponse implements Response{
    private JoyQueueCode joyQueueCode;
    public BooleanResponse(JoyQueueCode code){
        this.joyQueueCode = code;
    }
    public static BooleanResponse success(){
        return new BooleanResponse(JoyQueueCode.SUCCESS);
    }

    public static BooleanResponse failed(JoyQueueCode joyQueueCode){
        return new BooleanResponse(joyQueueCode);
    }

    public JoyQueueCode getJoyQueueCode() {
        return joyQueueCode;
    }

    @Override
    public boolean isSuccess(){
        return joyQueueCode == JoyQueueCode.SUCCESS;
    }
}
