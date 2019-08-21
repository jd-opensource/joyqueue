package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.service.AppTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionAppTokenService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionAppTokenService implements AppTokenService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionAppTokenService.class);

    private CompositionConfig config;
    private AppTokenService igniteAppTokenService;
    private AppTokenService journalkeeperAppTokenService;

    public CompositionAppTokenService(CompositionConfig config, AppTokenService igniteAppTokenService,
                                      AppTokenService journalkeeperAppTokenService) {
        this.config = config;
        this.igniteAppTokenService = igniteAppTokenService;
        this.journalkeeperAppTokenService = journalkeeperAppTokenService;
    }

    @Override
    public List<AppToken> getByApp(String app) {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.getByApp(app);
        } else {
            return journalkeeperAppTokenService.getByApp(app);
        }
    }

    @Override
    public AppToken getByAppAndToken(String app, String token) {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.getByAppAndToken(app, token);
        } else {
            return journalkeeperAppTokenService.getByAppAndToken(app, token);
        }
    }

    @Override
    public AppToken getById(long id) {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.getById(id);
        } else {
            return journalkeeperAppTokenService.getById(id);
        }
    }

    @Override
    public AppToken add(AppToken appToken) {
        AppToken result = null;
        if (config.isWriteIgnite()) {
            result = igniteAppTokenService.add(appToken);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                return journalkeeperAppTokenService.add(appToken);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}", appToken, e);
            }
        }
        return result;
    }

    @Override
    public AppToken update(AppToken appToken) {
        AppToken result = null;
        if (config.isWriteIgnite()) {
            result = igniteAppTokenService.update(appToken);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                return journalkeeperAppTokenService.update(appToken);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", appToken, e);
            }
        }
        return result;
    }

    @Override
    public void delete(long id) {
        if (config.isWriteIgnite()) {
            igniteAppTokenService.delete(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperAppTokenService.delete(id);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", id, e);
            }
        }
    }
}
