package io.chubao.joyqueue.broker.coordinator.transaction.domain;

/**
 * TransactionMetadata
 *
 * author: gaohaoxiang
 * date: 2019/4/10
 */
public class TransactionMetadata {

    private String id;
    private String extension;

    public TransactionMetadata() {

    }

    public TransactionMetadata(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}