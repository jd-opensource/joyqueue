package com.jd.journalq.service.impl;


import com.alibaba.fastjson.JSON;
import com.jd.journalq.domain.*;
import com.jd.journalq.monitor.RestResponse;
import com.jd.journalq.convert.CodeConverter;
import com.jd.journalq.service.CoordinatorMonitorService;
import com.jd.journalq.model.domain.CoordinatorBroker;
import com.jd.journalq.model.domain.Subscribe;
import com.jd.journalq.service.LeaderService;
import com.jd.journalq.other.HttpRestService;
import com.jd.journalq.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
//todo 待移走
@Service("coordinatorMonitorService")
public class CoordinatorMonitorServiceImpl implements CoordinatorMonitorService {
    private static final Logger logger= LoggerFactory.getLogger(CoordinatorMonitorServiceImpl.class);
    @Autowired
    HttpRestService httpRestService;
    @Autowired
    LeaderService leaderService;

    @Override
    public CoordinatorGroup findCoordinatorGroup(Subscribe subscribe) {
        CoordinatorDetail coordinatorInfo=findCoordinatorDetail(subscribe);
        Broker coordinator=coordinatorInfo.getCurrent();
        String pathKey="partitionGroupCoordinatorDetailMonitor";
        String[] args=new String[5];
        args[0]=coordinator.getIp();
        args[1]=String.valueOf(coordinator.getMonitorPort());
        args[2]= ClientType.valueOf(subscribe.getClientType()).getName();
        args[3]= CodeConverter.convertApp(subscribe.getApp(),subscribe.getSubscribeGroup());
        args[4]=CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName();
        RestResponse<CoordinatorGroup>  restCoordinatorGroup=httpRestService.get(pathKey,CoordinatorGroup.class,false,args);
        return restCoordinatorGroup.getData();
    }

    @Override
    public List<CoordinatorBroker> findCoordinatorInfo( Subscribe subscribe) {
        CoordinatorDetail coordinatorDetail=findCoordinatorDetail(subscribe);
        List<CoordinatorBroker> coordinatorBrokers=new ArrayList<>();
        CoordinatorBroker coordinatorBroker=new CoordinatorBroker();
        coordinatorBroker.setBroker(coordinatorDetail.getCurrent());
        coordinatorBroker.setCoordinator(true);
        coordinatorBrokers.add(coordinatorBroker);
        if(!NullUtil.isEmpty(coordinatorDetail.getReplicas())){
            // CoordinatorBroker coordinatorBroker;
            for(Broker b:coordinatorDetail.getReplicas()){
                coordinatorBroker=new CoordinatorBroker();
                coordinatorBroker.setBroker(b);
               // coordinatorBroker.setCoordinator(b.equals(coordinatorDetail.getCurrent()));
                coordinatorBrokers.add(coordinatorBroker);
            }
        }
        return coordinatorBrokers;
    }

    /**
     *
     * @return coordinator info
     *
     **/
    public CoordinatorDetail findCoordinatorDetail(Subscribe subscribe){
        String[] args=new String[3];
        List<Map.Entry<PartitionGroup, com.jd.journalq.model.domain.Broker>> partitionGroupBrokers=leaderService.findPartitionGroupLeaderBrokerDetail(subscribe.getTopic().getCode(),subscribe.getNamespace().getCode());// default
        if(NullUtil.isEmpty(partitionGroupBrokers)) {
            logger.info("partition group broker not found for {}",JSON.toJSONString(subscribe));
            return new CoordinatorDetail();
        }
        com.jd.journalq.model.domain.Broker broker= partitionGroupBrokers.get(0).getValue();
        args[0]=broker.getIp();
        args[1]=String.valueOf(broker.getMonitorPort());
        args[2]=CodeConverter.convertApp(subscribe.getApp(),subscribe.getSubscribeGroup());
        String pathKey="partitionGroupCoordinatorInfoMonitor";
        RestResponse<CoordinatorDetail> restCoordinatorDetail=httpRestService.get(pathKey,CoordinatorDetail.class,false,args);
        return restCoordinatorDetail.getData();
    }

    @Override
    public CoordinatorGroupMemberExtension findCoordinatorGroupMember(Subscribe subscribe){
       List<CoordinatorGroupMember> members= new ArrayList();
        CoordinatorGroupMemberExtension extension=new CoordinatorGroupMemberExtension();
        CoordinatorGroup group=findCoordinatorGroup(subscribe);
        if(!NullUtil.isEmpty(group)&&!NullUtil.isEmpty(group.getMembers())) {
            members.addAll(group.getMembers().values());
        }
        extension.setMembers(members);
        extension.setExtension(group.getExtension());
        return extension;
    }

    @Override
    public List<CoordinatorGroupExpiredMember> findExpiredCoordinatorGroupMember(Subscribe subscribe) {
        List<CoordinatorGroupExpiredMember> result= new ArrayList();
        CoordinatorGroup group=findCoordinatorGroup(subscribe);
        if(!NullUtil.isEmpty(group)&&!NullUtil.isEmpty(group.getExpiredMembers())) {
            result.addAll(group.getExpiredMembers().values());
        }
        return  result;
    }
}
