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
package com.jd.journalq.broker.archive;

import com.jd.journalq.server.archive.store.model.ConsumeLog;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by chengzhiliang on 2018/12/19.
 */
public class ConsumeArchiveServiceTest {

    @Test
    public void writeConsumeLog() {
        String baseDir = "/export/Data/test/";
        ConsumeArchiveService.ArchiveMappedFileRepository archiveMappedFileRepository = new ConsumeArchiveService.ArchiveMappedFileRepository(baseDir);
        String[] appArr = {"test", "app_test", "app_app_test"};
        for (int i = 0; i < 1000000; i++) {
            ConsumeLog consumeLog = new ConsumeLog();
            consumeLog.setBrokerId(Integer.MAX_VALUE);
            consumeLog.setBytesMessageId(new byte[16]);
            consumeLog.setClientIp(new byte[16]);
            consumeLog.setConsumeTime(Long.MAX_VALUE);
            consumeLog.setApp(appArr[i % 3]);
            ByteBuffer buffer = ArchiveSerializer.write(consumeLog);
            archiveMappedFileRepository.append(buffer);
            System.out.println(i);
        }

    }


}