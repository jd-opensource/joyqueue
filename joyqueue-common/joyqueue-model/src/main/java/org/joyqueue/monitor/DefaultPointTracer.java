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
package org.joyqueue.monitor;

/**
 * @author lining11
 * Date: 2020/1/6
 */
public class DefaultPointTracer implements PointTracer {

    private TraceStat stat = new TraceStat();


    @Override
    public String type() {
        return "default";
    }

    @Override
    public TraceStat begin(String start) {
        return stat;
    }

    @Override
    public void end(TraceStat end) {

    }

    @Override
    public void error(TraceStat end) {

    }
}
