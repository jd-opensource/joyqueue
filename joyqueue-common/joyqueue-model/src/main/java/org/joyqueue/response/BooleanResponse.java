/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.response;

import org.joyqueue.exception.JoyQueueCode;

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
