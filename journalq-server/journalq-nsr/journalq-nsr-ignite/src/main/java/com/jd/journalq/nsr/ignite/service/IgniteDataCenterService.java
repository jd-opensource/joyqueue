/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.nsr.ignite.service;

import com.google.inject.Inject;
import com.jd.journalq.domain.DataCenter;
import com.jd.journalq.event.DataCenterEvent;
import com.jd.journalq.event.MetaEvent;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.nsr.ignite.dao.DataCenterDao;
import com.jd.journalq.nsr.ignite.model.IgniteDataCenter;
import com.jd.journalq.nsr.message.Messenger;
import com.jd.journalq.nsr.model.DataCenterQuery;
import com.jd.journalq.nsr.service.DataCenterService;
import org.apache.ignite.Ignition;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IgniteDataCenterService implements DataCenterService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private DataCenterDao dataCenterDao;
    @Inject
    protected Messenger messenger;

    public IgniteDataCenterService(DataCenterDao dataCenterDao) {
        this.dataCenterDao = dataCenterDao;
    }

    @Override
    public DataCenter getById(String id) {
        return dataCenterDao.findById(id);
    }

    @Override
    public DataCenter get(DataCenter model) {
        return this.getById(new IgniteDataCenter(model).getId());
    }

    @Override
    public void addOrUpdate(DataCenter dataCenter) {
        try (Transaction tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED)) {
            dataCenterDao.addOrUpdate(new IgniteDataCenter(dataCenter));
            this.publishEvent(DataCenterEvent.add(dataCenter.getRegion(),dataCenter.getCode(),dataCenter.getUrl()));
            tx.commit();
        } catch (Exception e) {
            String message = String.format("add data center.", dataCenter.toString());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }

    }
    public void publishEvent(MetaEvent event) {
        messenger.publish(event);
    }

    @Override
    public void deleteById(String id) {
        dataCenterDao.deleteById(id);
    }

    @Override
    public void delete(DataCenter dataCenter) {

        try (Transaction tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED)) {
            this.deleteById(new IgniteDataCenter(dataCenter).getId());
            this.publishEvent(DataCenterEvent.remove(dataCenter.getRegion(),dataCenter.getCode(),dataCenter.getUrl()));
            tx.commit();
        } catch (Exception e) {
            String message = String.format("delete data center.", dataCenter.toString());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }

    }

    @Override
    public List<DataCenter> list() {
        return this.list(null);
    }

    @Override
    public List<DataCenter> list(DataCenterQuery query) {
        return convert(dataCenterDao.list(query));
    }

    @Override
    public PageResult<DataCenter> pageQuery(QPageQuery<DataCenterQuery> pageQuery) {
        PageResult<IgniteDataCenter> iDatacenters = dataCenterDao.pageQuery(pageQuery);
        return new PageResult<>(iDatacenters.getPagination(), convert(iDatacenters.getResult()));
    }

    private List<DataCenter> convert(List<IgniteDataCenter> dataCenters) {
        if (dataCenters == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(dataCenters);
    }

}
