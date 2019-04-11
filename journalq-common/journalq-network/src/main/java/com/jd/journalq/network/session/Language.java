/**
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
package com.jd.journalq.network.session;

/**
 * 客户端语言
 */
public enum Language {
    /**
     * Java客户端
     */
    JAVA,
    /**
     * C++客户端
     */
    CPP,
    /**
     * Python客户端
     */
    PYTHON,
    /**
     * Ruby客户端
     */
    RUBY,
    /**
     * .NET客户端
     */
    DOTNET,
    /**
     * Erlang客户端
     */
    ERLANG,
    /**
     * 其它客户端
     */
    OTHER;

    public static Language valueOf(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }

}