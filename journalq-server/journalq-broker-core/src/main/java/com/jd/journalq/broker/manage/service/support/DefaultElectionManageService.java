package com.jd.journalq.broker.manage.service.support;

import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.broker.manage.service.ElectionManageService;

public class DefaultElectionManageService implements ElectionManageService {
    private ElectionService electionService;

    public DefaultElectionManageService(ElectionService electionService) {
        this.electionService = electionService;
    }

    @Override
    public void restoreElectionMetadata() {
        electionService.syncElectionMetadataFromNameService();
    }
}
