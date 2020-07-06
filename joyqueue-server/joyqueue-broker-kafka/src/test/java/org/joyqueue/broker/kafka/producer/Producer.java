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
package org.joyqueue.broker.kafka.producer;

import org.joyqueue.broker.kafka.conf.KafkaConfigs;
import org.joyqueue.toolkit.time.SystemClock;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.record.CompressionType;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Producer
 *
 * @author luoruiheng
 * @since 2/24/17
 */
public class Producer extends Thread {

    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final Boolean isAsync;

    public Producer(String topic, Boolean isAsync) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigs.BOOTSTRAP);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaConfigs.CLIENT_ID);
        // if u want to know offset in callback, setting acks=-1
        //props.put("acks", "-1");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.GZIP.name);
        producer = new KafkaProducer<>(props);
        this.topic = topic;
        this.isAsync = isAsync;
    }

    public void sendAsync(ProducerRecord<String, String> record) throws ExecutionException, InterruptedException {
        producer.send(record);
    }

    public RecordMetadata send(ProducerRecord<String, String> record) throws ExecutionException, InterruptedException {
        return producer.send(record).get();
    }

    public void run() {
        int messageNo = 1;
        while (messageNo <= 100) {
            String messageStr = "Message_" + messageNo + "_" + System.nanoTime();
            long startTime = SystemClock.now();
            if (isAsync) { // Send asynchronously
                producer.send(new ProducerRecord<String, String>(topic,
                        messageNo+"",
                        messageStr), new DemoCallBack(startTime, messageNo, messageStr));
            } else { // Send synchronously
                try {
                    producer.send(new ProducerRecord<String, String>(topic,
                            messageNo+"",
                            messageStr)).get();
                    System.out.println("Sent message: (" + messageNo + ", " + messageStr + ")");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            ++messageNo;
        }
    }
}

class DemoCallBack implements Callback {

    private long startTime;
    private int key;
    private String message;

    public DemoCallBack(long startTime, int key, String message) {
        this.startTime = startTime;
        this.key = key;
        this.message = message;
    }

    /**
     * A callback method the user can implement to provide asynchronous handling of request completion. This method will
     * be called when the record sent to the server has been acknowledged. Exactly one of the arguments will be
     * non-null.
     *
     * @param metadata  The metadata for the record that was sent (i.e. the partition and offset). Null if an error
     *                  occurred.
     * @param exception The exception thrown during processing of this record. Null if no error occurred.
     */
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        long elapsedTime = SystemClock.now() - startTime;
        if (metadata != null) {
            System.out.println(
                    "message(" + key + ", " + message + ") sent to partition(" + metadata.partition() +
                            "), " +
                            "offset(" + metadata.offset() + ") in " + elapsedTime + " ms");
        } else {
            exception.printStackTrace();
        }
    }

}
