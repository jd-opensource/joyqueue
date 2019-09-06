package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.service.internal.AppTokenInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionAppTokenInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionAppTokenInternalService implements AppTokenInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionAppTokenInternalService.class);

    private CompositionConfig config;
    private AppTokenInternalService igniteAppTokenService;
    private AppTokenInternalService journalkeeperAppTokenService;

    public CompositionAppTokenInternalService(CompositionConfig config, AppTokenInternalService igniteAppTokenService,
                                              AppTokenInternalService journalkeeperAppTokenService) {
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
    public List<AppToken> getAll() {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.getAll();
        } else {
            return journalkeeperAppTokenService.getAll();
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
