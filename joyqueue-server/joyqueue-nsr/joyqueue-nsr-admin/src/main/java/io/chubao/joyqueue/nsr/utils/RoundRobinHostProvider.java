package io.chubao.joyqueue.nsr.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * 127.0.0.1:50091,192.168.0.1:60091
 *
 **/
public class RoundRobinHostProvider implements HostProvider {
    private Logger logger=LoggerFactory.getLogger(RoundRobinHostProvider.class);
    private final String HTTP="http://";
    private List<String> nameServers;
    private int currentIndex=-1; // current round robin index
    private int lastIndex=-1; // last connected server
    public RoundRobinHostProvider(String connectionStr){
      this.nameServers=parseHosts(connectionStr);
    }

    public List<String> parseHosts(String connectionStr){
        List<String> server=new ArrayList<>();
        String[] hosts=connectionStr.split(",");
        for(String host:hosts){
            server.add(HTTP+host);
        }
        return server;
    }
    @Override
    public int size() {
        return nameServers.size();
    }

    @Override
    public String next(long spinDelayMs) {
        this.currentIndex=++currentIndex%size();
        if(currentIndex==lastIndex&&spinDelayMs>0){
            try {
                Thread.sleep(spinDelayMs);
            }catch (InterruptedException e){
                logger.info("spin delay exception" ,e);
            }
        }else if(lastIndex==-1){
            lastIndex=0;
        }
        return this.nameServers.get(currentIndex);
    }

    @Override
    public void onConnected() {
        this.lastIndex=currentIndex;
    }
}
