package io.openmessaging.spring.support;

/**
 * Container for the interceptor.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class InterceptorContainer {

    private String accessPoint;
    private Object interceptor;

    public InterceptorContainer(Object interceptor) {
        this.interceptor = interceptor;
    }

    public InterceptorContainer(String accessPoint, Object interceptor) {
        this.accessPoint = accessPoint;
        this.interceptor = interceptor;
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public Object getInterceptor() {
        return interceptor;
    }
}