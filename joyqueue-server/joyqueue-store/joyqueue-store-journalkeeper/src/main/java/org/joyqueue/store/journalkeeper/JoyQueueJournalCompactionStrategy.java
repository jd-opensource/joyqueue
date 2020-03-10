package org.joyqueue.store.journalkeeper;

import io.journalkeeper.core.journal.Journal;
import io.journalkeeper.core.strategy.JournalCompactionStrategy;
import io.journalkeeper.utils.spi.Singleton;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.config.BrokerStoreConfig;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.SortedMap;

/**
 * @author LiYue
 * Date: 2019/11/29
 */
@Singleton
public class JoyQueueJournalCompactionStrategy implements JournalCompactionStrategy, BrokerContextAware {
    private static final Logger logger = LoggerFactory.getLogger(JoyQueueJournalCompactionStrategy.class);
    private BrokerStoreConfig brokerStoreConfig = null;

    @Override
    public long calculateCompactionIndex(SortedMap<Long, Long> snapshotTimestamps, Journal journal) {
        if(null == brokerStoreConfig) {
            logger.warn("BrokerStoreConfig is null!");
            return  -1L;
        }
        long retentionTime = brokerStoreConfig.getMaxStoreTime();
        long retentionSize = brokerStoreConfig.getMaxStoreSize();

        if (retentionSize > 0 && retentionTime > 0) {
            return Math.min(
                    calcIndexByRetentionTime(snapshotTimestamps, retentionTime),
                    calcIndexByStorageSize(snapshotTimestamps, journal, retentionSize)
            );
        } else if (retentionSize > 0) {
            return calcIndexByStorageSize(snapshotTimestamps, journal, retentionSize);
        } else if (retentionTime > 0) {
            return calcIndexByRetentionTime(snapshotTimestamps, retentionTime);
        } else {
            return -1L;
        }
    }

    private long calcIndexByStorageSize(SortedMap<Long, Long> snapshotTimestamps, Journal journal, long maxStoreSize) {
        long index = -1;
        for (Map.Entry<Long, Long> entry : snapshotTimestamps.entrySet()) {
            long snapshotIndex = entry.getKey();
            long offset = journal.readOffset(snapshotIndex);

            if (journal.maxOffset() - offset > maxStoreSize) {
                index = snapshotIndex;
            } else {
                break;
            }

        }
        return index;
    }

    private long calcIndexByRetentionTime(SortedMap<Long, Long> snapshotTimestamps, long retentionTime) {
        long index = -1;
        long now = SystemClock.now();
        long compactTimestamp = now - retentionTime;

        for (Map.Entry<Long, Long> entry : snapshotTimestamps.entrySet()) {
            long snapshotIndex = entry.getKey();
            long snapshotTimestamp = entry.getValue();
            if (snapshotTimestamp <= compactTimestamp) {
                index = snapshotIndex;
            } else {
                break;
            }
        }

        return index;
    }


    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerStoreConfig = new BrokerStoreConfig(brokerContext.getPropertySupplier());
    }
}
