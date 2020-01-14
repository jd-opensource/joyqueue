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
package org.joyqueue.handler.error;

/**
 * Identifier exception conversion
 * Created by chenyanying3 on 18-11-16.
 */
public class IdentifierException extends ConfigException {

    public IdentifierException(int maxLength) {
        super(ErrorCode.BadRequest, "标识符必须以英文字母开头，英文字母、阿拉伯数字、下划线和小数点组成，最大长度为" + maxLength);
    }
}
