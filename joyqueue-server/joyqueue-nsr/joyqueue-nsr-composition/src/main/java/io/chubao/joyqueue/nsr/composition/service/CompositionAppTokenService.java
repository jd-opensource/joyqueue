package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.model.AppTokenQuery;
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
    public AppToken getByAppAndToken(String app, String token) {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.getByAppAndToken(app, token);
        } else {
            return journalkeeperAppTokenService.getByAppAndToken(app, token);
        }
    }

    @Override
    public AppToken getById(Long id) {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.getById(id);
        } else {
            return journalkeeperAppTokenService.getById(id);
        }
    }

    @Override
    public AppToken get(AppToken model) {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.get(model);
        } else {
            return journalkeeperAppTokenService.get(model);
        }
    }

    @Override
    public void addOrUpdate(AppToken appToken) {
        if (config.isWriteIgnite()) {
            igniteAppTokenService.addOrUpdate(appToken);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperAppTokenService.addOrUpdate(appToken);
            } catch (Exception e) {
                logger.error("addOrUpdate journalkeeper exception, params: {}", appToken, e);
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        if (config.isWriteIgnite()) {
            igniteAppTokenService.deleteById(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperAppTokenService.deleteById(id);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", id, e);
            }
        }
    }

    @Override
    public void delete(AppToken model) {
        if (config.isWriteIgnite()) {
            igniteAppTokenService.delete(model);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperAppTokenService.delete(model);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", model, e);
            }
        }
    }

    @Override
    public List<AppToken> list() {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.list();
        } else {
            return journalkeeperAppTokenService.list();
        }
    }

    @Override
    public List<AppToken> list(AppTokenQuery query) {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.list(query);
        } else {
            return journalkeeperAppTokenService.list(query);
        }
    }

    @Override
    public PageResult<AppToken> pageQuery(QPageQuery<AppTokenQuery> pageQuery) {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.pageQuery(pageQuery);
        } else {
            return journalkeeperAppTokenService.pageQuery(pageQuery);
        }
    }
}
