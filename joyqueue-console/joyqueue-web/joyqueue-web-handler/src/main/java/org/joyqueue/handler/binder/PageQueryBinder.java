/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.handler.binder;

import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.Query;
import com.jd.laf.binding.Plugin;
import com.jd.laf.binding.binder.Binder;
import com.jd.laf.binding.converter.Scope;
import com.jd.laf.binding.marshaller.Marshaller;
import com.jd.laf.binding.marshaller.Unmarshaller;
import com.jd.laf.binding.reflect.exception.ReflectionException;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * page query binder
 * Created by chenyanying3 on 19-3-3.
 */
public class PageQueryBinder<Q extends Query> implements Binder {
    private static final String pagination = "pagination";
    private static final String query = "query";
    private static final Logger logger = LoggerFactory.getLogger(PageQueryBinder.class);
    @Override
    public boolean bind(final Context context) throws ReflectionException {
        if (context == null) {
            return false;
        }
        Object source = context.getSource();
        if (!(source instanceof RoutingContext)) {
            return false;
        }
        RoutingContext routingContext = (RoutingContext)source;
        Scope.ParameterScope scope = (Scope.ParameterScope) context.getScope();

        QPageQuery pageQuery = new QPageQuery();
        try {
            Unmarshaller unmarshaller = Plugin.JSON.get().getUnmarshaller();
            Marshaller marshaller = Plugin.JSON.get().getMarshaller();
            Map<String, Object> map = Plugin.JSON.get().getUnmarshaller().unmarshall(routingContext.getBodyAsString(), Map.class, null);
            if (map != null && !map.isEmpty()) {
                pageQuery.setPagination(unmarshaller.unmarshall(marshaller.marshall(map.get(pagination)), Pagination.class, null));
                pageQuery.setQuery(unmarshaller.unmarshall(marshaller.marshall(map.get(query)), scope.getGenericType(), null));
            }

            if (pageQuery.getPagination() == null) {
                pageQuery.setPagination(Pagination.newPagination(null));
            }

            if (pageQuery.getQuery() == null) {
                pageQuery.setQuery(scope.getGenericType().newInstance());
            }

            context.bind(pageQuery);
        } catch (ReflectionException e) {
            logger.error("", e);
            throw e;
        } catch (Exception e) {
            logger.error("", e);
            throw new ReflectionException(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public Class<?> annotation() {
        return PageQuery.class;
    }
}
