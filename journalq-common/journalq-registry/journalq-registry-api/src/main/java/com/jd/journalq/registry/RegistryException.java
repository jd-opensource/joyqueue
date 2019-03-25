package com.jd.journalq.registry;

/**
 * 注册中心异常
 */
public class RegistryException extends Exception {

    public RegistryException() {
        super();
    }

    public RegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistryException(String message) {
        super(message);
    }

    public RegistryException(Throwable cause) {
        super(cause);
    }

    public static class ConnectionLossException extends RegistryException {
        public ConnectionLossException() {
            super("ConnectionLoss");
        }
    }

    public static class OperationTimeoutException extends RegistryException {
        public OperationTimeoutException() {
            super("OperationTimeout");
        }
    }

    public static class DataInconsistencyException extends RegistryException {
        public DataInconsistencyException() {
            super("DataInconsistency");
        }
    }

    public static class NoNodeException extends RegistryException {
        public NoNodeException() {
            super("NoNode");
        }
    }

    public static class NodeExistsException extends RegistryException {
        public NodeExistsException() {
            super("NodeExists");
        }
    }

    public static class NotEmptyException extends RegistryException {
        public NotEmptyException() {
            super("Directory not empty");
        }
    }

    public static class SessionExpiredException extends RegistryException {
        public SessionExpiredException() {
            super("Session expired");
        }
    }

    /**
     * 版本号冲突异常
     */
    public static class BadVersionException extends RegistryException {

        public BadVersionException() {
            super("BadVersion");
        }

        public BadVersionException(String message, Throwable cause) {
            super(message, cause);
        }

        public BadVersionException(String message) {
            super(message);
        }
    }

}
