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
package org.joyqueue.handler.util;

import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.handler.error.IdentifierException;
import org.joyqueue.model.domain.Identifier;

/**
 * Created by bjliuyong on 2018/10/29.
 */
public class ExceptionUtils {

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     * @param expression a boolean expression
     * @param errorCode the errorCode to use if the check fails
     * @throws ConfigException if {@code expression} is false
     * @see ErrorCode
     */
    public static void checkArgument(boolean expression , ErrorCode errorCode) {
        if( !expression ) {
            throw new ConfigException(errorCode) ;
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     * @param expression a boolean expression
     * @param errorCode  the errorCode to use if the check fails
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *      string using {@link String#valueOf(Object)}
     * @throws ConfigException if {@code expression} is false
     * @see ErrorCode
     */
    public static void checkArgument(boolean expression , ErrorCode errorCode , Object errorMessage) {
        if( !expression ) {
            throw new ConfigException(errorCode , String.valueOf(errorMessage)) ;
        }
    }

    /**
     * Ensures the truth of an expression involving the state of the calling instance, but not
     * involving any parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalStateException if {@code expression} is false
     */
    public static void checkState(boolean expression , Object errorMessage) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }

    /**
     * Ensures input is an identifier and its length less than or equal maxLength
     *
     * @param input
     * @param maxLength
     * @throws IdentifierException if {@code input }  is not identifier
     */
    public static void checkIdentifier(String input , int maxLength ,String field ) {
        if( !Identifier.isIdentifier(input , maxLength) ) {
            throw new ConfigException(ErrorCode.BadRequest , field + " 必须为标识符，以英文字母开头，英文字母、阿拉伯数字、下划线、横线或小数点组成，最大长度为" + maxLength) ;
        }
    }

    /**
     * Ensures input is an identifier and its length less than or equal maxLength
     *
     * @param input
     * @param maxLength
     * @throws IdentifierException if {@code input }  is not identifier
     */
    public static void checkIdentifier(String input , int maxLength) {
        if( !Identifier.isIdentifier(input , maxLength) ) {
            throw new IdentifierException(maxLength) ;
        }
    }

    /**
     * Ensures input is an identifier and its length less than or equal maxLength
     *
     * @param identifier
     * @param maxLength
     * @throws IdentifierException if {@code input }  is not identifier
     */
    public static void checkIdentifier(Identifier identifier , int maxLength) {
        checkIdentifier(identifier.getCode() , maxLength);
    }
}
