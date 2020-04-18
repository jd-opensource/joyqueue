package org.joyqueue.store.transaction;

import java.util.Map;

public class JoyqueueStoreTansactionContext implements StoreTransactionContext {

    private int txId;
    private Map<String,String> txContext;
    public JoyqueueStoreTansactionContext(int id,Map<String,String> context){
        this.txId=id;
        this.txContext=context;
    }
    @Override
    public StoreTransactionId transactionId() {
        return new StoreTransactionId() {
            @Override
            public int hashCode() {
                return txId;
            }

            @Override
            public boolean equals(Object obj) {
                if(obj==null){return false;}
                if(obj instanceof Integer) {
                    Integer that=(Integer) obj;
                    return that.equals(txId);
                }
                return false;
            }

            @Override
            public String toString() {
                return String.valueOf(txId);
            }
        };
    }

    @Override
    public Map<String, String> context() {
        return txContext;
    }

    @Override
    public long timestamp() {
        return 0;
    }
}
