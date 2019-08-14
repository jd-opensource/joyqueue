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
package io.chubao.joyqueue.broker.kafka.coordinator.transaction.synchronizer;

import io.chubao.joyqueue.broker.coordinator.session.CoordinatorSessionManager;
import io.chubao.joyqueue.broker.kafka.config.KafkaConfig;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionIdManager;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionMarker;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionOffset;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionState;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.log.TransactionLog;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.toolkit.service.Service;
import io.chubao.joyqueue.toolkit.time.SystemClock;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * TransactionSynchronizer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/12
 */
public class TransactionSynchronizer extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionSynchronizer.class);

    private KafkaConfig config;
    private TransactionIdManager transactionIdManager;
    private TransactionLog transactionLog;
    private CoordinatorSessionManager sessionManager;
    private NameService nameService;

    private TransactionCommitSynchronizer transactionCommitSynchronizer;
    private TransactionAbortSynchronizer transactionAbortSynchronizer;

    public TransactionSynchronizer(KafkaConfig config, TransactionIdManager transactionIdManager, TransactionLog transactionLog, CoordinatorSessionManager sessionManager, NameService nameService) {
        this.config = config;
        this.transactionIdManager = transactionIdManager;
        this.transactionLog = transactionLog;
        this.sessionManager = sessionManager;
        this.nameService = nameService;
    }

    @Override
    protected void validate() throws Exception {
        transactionCommitSynchronizer = new TransactionCommitSynchronizer(config, sessionManager, transactionIdManager, nameService);
        transactionAbortSynchronizer = new TransactionAbortSynchronizer(config, sessionManager, transactionIdManager);
    }

    @Override
    protected void doStart() throws Exception {
        transactionCommitSynchronizer.start();
        transactionAbortSynchronizer.start();
    }

    @Override
    protected void doStop() {
        if (transactionCommitSynchronizer != null) {
            transactionCommitSynchronizer.stop();
        }
        if (transactionAbortSynchronizer != null) {
            transactionAbortSynchronizer.stop();
        }
    }

    public boolean prepare(TransactionMetadata transactionMetadata, Set<TransactionPrepare> prepare) throws Exception {
        return transactionLog.batchWrite(transactionMetadata.getApp(), transactionMetadata.getId(), prepare);
    }

    public boolean prepareCommit(TransactionMetadata transactionMetadata, Set<TransactionPrepare> prepare) throws Exception {
        return writeMarker(transactionMetadata, TransactionState.PREPARE_COMMIT);
    }

    public boolean commit(TransactionMetadata transactionMetadata, Set<TransactionPrepare> prepare, Set<TransactionOffset> offsets) throws Exception {
        if (!tryCommit(transactionMetadata, prepare, offsets)) {
            return false;
        }
        return writeMarker(transactionMetadata, TransactionState.COMPLETE_COMMIT);
    }

    public boolean tryCommit(TransactionMetadata transactionMetadata, Set<TransactionPrepare> prepare, Set<TransactionOffset> offsets) throws Exception {
        boolean isSuccess = true;
        if (CollectionUtils.isNotEmpty(prepare)) {
            isSuccess = transactionCommitSynchronizer.commitPrepare(transactionMetadata, prepare);
        }
        if (isSuccess && CollectionUtils.isNotEmpty(offsets)) {
            isSuccess = transactionCommitSynchronizer.commitOffsets(transactionMetadata, offsets);
        }
        return isSuccess;
    }

    public boolean prepareAbort(TransactionMetadata transactionMetadata, Set<TransactionPrepare> prepare) throws Exception {
        return writeMarker(transactionMetadata, TransactionState.PREPARE_ABORT);
    }

    public boolean abort(TransactionMetadata transactionMetadata, Set<TransactionPrepare> prepare) throws Exception {
        return tryAbort(transactionMetadata, prepare) && writeMarker(transactionMetadata, TransactionState.COMPLETE_ABORT);
    }

    public boolean tryAbort(TransactionMetadata transactionMetadata, Set<TransactionPrepare> prepare) throws Exception {
        return transactionAbortSynchronizer.abort(transactionMetadata, prepare);
    }

    public boolean commitOffset(TransactionMetadata transactionMetadata, Set<TransactionOffset> offsets) throws Exception {
        return transactionLog.batchWrite(transactionMetadata.getApp(), transactionMetadata.getId(), offsets);
    }

    protected boolean writeMarker(TransactionMetadata transactionMetadata, TransactionState transactionState) throws Exception {
        TransactionMarker marker = convertMarker(transactionMetadata, transactionState);
        return transactionLog.write(transactionMetadata.getApp(), transactionMetadata.getId(), marker);
    }

    protected TransactionMarker convertMarker(TransactionMetadata transactionMetadata, TransactionState transactionState) {
        return new TransactionMarker(transactionMetadata.getApp(), transactionMetadata.getId(), transactionMetadata.getProducerId(),
                transactionMetadata.getProducerEpoch(), transactionMetadata.getEpoch(), transactionState, transactionMetadata.getTimeout(), SystemClock.now());
    }
}