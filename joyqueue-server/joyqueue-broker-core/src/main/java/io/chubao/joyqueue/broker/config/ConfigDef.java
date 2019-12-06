package io.chubao.joyqueue.broker.config;

/**
 *
 * Broker config key
 *
 **/
public enum ConfigDef {

    APPLICATION_DATA_PATH(null,"application.data.path","message store root path "),
    TRANSPORT_SERVER_PORT("broker.frontend-server.","transport.server.port","broker port"),
    NAME_SERVICE_MESSAGE_PORT("nameservice.","messenger.port", "name service message port"),
    NAME_SERVICE_CACHE_PATH("nameservice.","allmetadata.cache.file","name service cache path"),
    NAME_SERVER_JOURNALKEEPER_PORT("nameserver.","journalkeeper.port","bookeeper name server port "),
    NAME_SERVER_JOURNALKEEPER_WORKING_DIR("nameserver.","journalkeeper.working.dir"," journalkeeper working dir ");
    private  String region;
    private String name;
    private String desc;

    ConfigDef(String region,String name,String desc){
        this.region=region;
        this.name= name;
        this.desc=desc;
    }

    /**
     * property key
     *
     **/
    public String key(){
        return region==null?name:region+name;
    }
}
