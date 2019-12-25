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
package org.joyqueue.broker.consumer;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * Created by chengzhiliang on 2018/10/24.
 */
public class ConsumePositionManagerTest {

    static String key = "test_key";


    private static ConcurrentHashMap<String, Position> cache = new ConcurrentHashMap();


    public static void main(String[] args) throws InterruptedException {
        cache.put(key, new Position(1));
        new Thread(new ReadThread(cache)).start();

        Thread.sleep(10000);

        new Thread(new WriteThread(cache)).start();
    }






    static class ReadThread implements Runnable {
        private ConcurrentHashMap<String, Position> cache = null;

        ReadThread(ConcurrentHashMap<String, Position> cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            while(true) {
                Position position = cache.get(key);
                if (position.getIndex() > 1) {
                    System.out.println("change.");
                }
            }
        }
    }

    static class WriteThread implements Runnable {
        private ConcurrentHashMap<String, Position> cache = null;

        WriteThread(ConcurrentHashMap<String, Position> cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            Position position = cache.get(key);
            position.setIndex(2);
        }
    }

    private static class Position{
        private long index;

        public Position(long index) {
            this.index = index;
        }

        public long getIndex() {
            return index;
        }

        public void setIndex(long index) {
            this.index = index;
        }
    }

}