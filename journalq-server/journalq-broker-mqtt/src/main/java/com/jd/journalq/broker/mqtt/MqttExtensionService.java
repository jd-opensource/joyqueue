//package com.jd.journalq.server.broker.mqtt;
//
//import BrokerContext;
//import ExtensionService;
//import MqttOverWebsocketProtocol;
//import MqttProtocol;
//import Service;
//
///**
// * MqttExtensionService
// * author: gaohaoxiang
// * email: gaohaoxiang@jd.com
// * date: 2018/11/13
// */
//@Deprecated
//public class MqttExtensionService extends Service implements ExtensionService {
//
//    @Override
//    public void init(BrokerContext brokerContext) {
//        brokerContext.getProtocolManager().register(new MqttProtocol(brokerContext));
//        brokerContext.getProtocolManager().register(new MqttOverWebsocketProtocol(brokerContext));
//    }
//}