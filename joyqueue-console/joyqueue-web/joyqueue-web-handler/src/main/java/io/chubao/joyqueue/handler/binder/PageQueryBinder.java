package io.chubao.joyqueue.handler.binder;

import io.chubao.joyqueue.handler.annotation.PageQuery;
import io.chubao.joyqueue.model.Pagination;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.Query;
import com.jd.laf.binding.Plugin;
import com.jd.laf.binding.binder.Binder;
import com.jd.laf.binding.converter.Scope;
import com.jd.laf.binding.marshaller.Marshaller;
import com.jd.laf.binding.marshaller.Unmarshaller;
import com.jd.laf.binding.reflect.exception.ReflectionException;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

/**
 * page query binder
 * Created by chenyanying3 on 19-3-3.
 */
public class PageQueryBinder<Q extends Query> implements Binder {
    private static final String pagination = "pagination";
    private static final String query = "query";
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
            throw e;
        } catch (Exception e) {
            throw new ReflectionException(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public Class<?> annotation() {
        return PageQuery.class;
    }
}
