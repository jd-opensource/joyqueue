package io.chubao.joyqueue.model.domain;

public class TopicMirror extends BaseModel {


    /**
     * 源主题的版本
     **/
    private String srcVersion;
    private String source;
    private String consumer;

    /**
     * 目的版本
     **/
    private String destVersion;
    private String destination;
    private String producer;

    public String getSrcVersion() {
        return srcVersion;
    }

    public void setSrcVersion(String srcVersion) {
        this.srcVersion = srcVersion;
    }


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getDestVersion() {
        return destVersion;
    }

    public void setDestVersion(String destVersion) {
        this.destVersion = destVersion;
    }



    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }
}
