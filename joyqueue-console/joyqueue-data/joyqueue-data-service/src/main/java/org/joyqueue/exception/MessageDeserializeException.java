package org.joyqueue.exception;

public class MessageDeserializeException extends RuntimeException {
    /**
     * Constructs a {@code MessageDeserializeException} with no detail message.
     */
    public MessageDeserializeException() {
        super();
    }

    /**
     * Constructs a {@code MessageDeserializeException} with the specified
     * detail message.
     *
     * @param   s   the detail message.
     */
    public MessageDeserializeException(String s) {
        super(s);
    }
}
