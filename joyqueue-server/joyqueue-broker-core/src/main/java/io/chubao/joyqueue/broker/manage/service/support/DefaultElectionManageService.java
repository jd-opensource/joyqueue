package io.chubao.joyqueue.broker.manage.service.support;

import io.chubao.joyqueue.broker.election.ElectionService;
import io.chubao.joyqueue.broker.manage.service.ElectionManageService;

public class DefaultElectionManageService implements ElectionManageService {
    private ElectionService electionService;

    public DefaultElectionManageService(ElectionService electionService) {
        this.electionService = electionService;
    }

    @Override
    public void restoreElectionMetadata() {
        electionService.syncElectionMetadataFromNameService();
    }

    @Override
    public String describe() {
        return electionService.describe();
    }

    @Override
    public String describeTopic(String topic, int partitionGroup) {
        return electionService.describe(topic, partitionGroup);
    }

    @Override
    public void updateTerm(String topic, int partitionGroup, int term) {
        electionService.updateTerm(topic, partitionGroup, term);
    }
}
