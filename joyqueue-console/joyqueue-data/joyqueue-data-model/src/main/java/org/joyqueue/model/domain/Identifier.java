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
package org.joyqueue.model.domain;

/**
 * 标识符
 */
public interface Identifier {

    /**
     * 获取ID
     *
     * @return
     */
    long getId();

    /**
     * 设置ID
     *
     * @param id
     */
    void setId(long id);

    /**
     * 获取代码
     *
     * @return
     */
    String getCode();

    /**
     * 设置代码
     *
     * @param code
     */
    void setCode(String code);

    /**
     * 获取名称
     *
     * @return
     */
    String getName();

    /**
     * 设置名称
     *
     * @param name
     */
    void setName(String name);

    /**
     * 判断是否是标识符代码，最大字符数64，以字母开始，字母、数字、下划线和小数点组成
     *
     * @param code 标识符
     * @return
     */
    static boolean isIdentifier(final String code) {
        return isIdentifier(code, Integer.MAX_VALUE);
    }

    /**
     * 判断是否是标识符代码，以字母开始，字母、数字、下划线和小数点组成
     *
     * @param code      标识符
     * @param maxLength 最大长度
     * @return
     */
    static boolean isIdentifier(final String code, final int maxLength) {
        if (code == null) {
            return false;
        } else {
            int length = code.length();
            if (length > maxLength) {
                return false;
            } else {
                char ch;
                for (int i = 0; i < length; i++) {
                    ch = code.charAt(i);
                    if (i == 0) {
                        if (!Character.isLetter(ch)) {
                            return false;
                        }
                    } else if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != '.' && ch != '-') {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    default Identity identity() {
        return new Identity(this);
    }
}
